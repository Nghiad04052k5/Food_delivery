package com.delivery.service;

import com.delivery.model.Driver;
import com.delivery.model.DriverStatus;
import com.delivery.model.Order;
import com.delivery.model.OrderStatus;
import com.delivery.repository.DriverRepository;
import com.delivery.repository.OrderRepository;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OrderService {
    private OrderRepository orderRepository;
    private DriverRepository driverRepository;
    
    // Lưu trữ các tài xế đã từ chối đơn hàng (orderId -> set of driverIds)
    private Map<Integer, Set<Integer>> rejectedDriversMap = new HashMap<>();

    public OrderService(OrderRepository orderRepository, DriverRepository driverRepository) {
        this.orderRepository = orderRepository;
        this.driverRepository = driverRepository;
    }

    /**
     * Tạo đơn hàng mới
     */
    public Order createOrder(Order order) {
        order.setStatus(OrderStatus.PENDING);
        orderRepository.save(order);
        return order;
    }

    /**
     * Tách rời thuật toán tìm tài xế và xử lý đặt hàng khỏi Controller.
     */
    public boolean dispatchOrder(int orderId, double restaurantLat, double restaurantLon) {
        Order order = orderRepository.findById(orderId);
        if (order == null || (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED)) {
            return false;
        }
        
        if (order.getDriverId() != null) {
            return false;
        }

        Set<Integer> excludedIds = rejectedDriversMap.getOrDefault(orderId, Collections.emptySet());
        Driver nearestDriver = driverRepository.findNearestAvailableDriverExcluding(restaurantLat, restaurantLon, excludedIds);
        
        if (nearestDriver != null) {
            // 2. Gán tài xế bận (Atomic)
            boolean driverSecured = driverRepository.validateAndSetStatusAtomically(nearestDriver.getId(), DriverStatus.AVAILABLE, DriverStatus.BUSY);
            if (!driverSecured) {
                return false;
            }

            // 3. Cập nhật đơn hàng (Atomic)
            boolean orderSecured = orderRepository.validateAndAssignDriverAtomically(orderId, nearestDriver.getId());
            if (!orderSecured) {
                // Nếu không gán được order, phải nhả tài xế ra
                driverRepository.validateAndSetStatusAtomically(nearestDriver.getId(), DriverStatus.BUSY, DriverStatus.AVAILABLE);
                return false;
            }
            return true;
        }
        return false;
    }
    
    /**
     * Tài xế chấp nhận đơn hàng
     */
    public boolean acceptOrder(int orderId, int driverId) {
        Order order = orderRepository.findById(orderId);
        if (order != null && order.getDriverId() != null && order.getDriverId() == driverId && order.getStatus() == OrderStatus.CONFIRMED) {
            order.setStatus(OrderStatus.DELIVERING);
            orderRepository.update(order);
            
            // Xóa bộ nhớ tạm vì đã có người nhận
            rejectedDriversMap.remove(orderId);
            return true;
        }
        return false;
    }
    
    /**
     * Tài xế từ chối đơn hàng
     */
    public boolean rejectOrder(int orderId, int driverId, double restaurantLat, double restaurantLon) {
        Order order = orderRepository.findById(orderId);
        if (order != null && order.getDriverId() != null && order.getDriverId() == driverId && order.getStatus() == OrderStatus.CONFIRMED) {
            // 1. Lưu tài xế vào danh sách từ chối
            rejectedDriversMap.computeIfAbsent(orderId, k -> new HashSet<>()).add(driverId);
            
            // 2. Giải phóng tài xế (trở lại AVAILABLE)
            Driver driver = driverRepository.findById(driverId);
            if (driver != null) {
                driver.setStatus(DriverStatus.AVAILABLE);
                driverRepository.update(driver);
            }
            
            // 3. Xóa driverId khỏi đơn hàng để chuẩn bị dispatch lại
            order.setDriverId(null);
            orderRepository.update(order);
            
            // 4. Tìm tài xế khác
            dispatchOrder(orderId, restaurantLat, restaurantLon);
            return true;
        }
        return false;
    }

    /**
     * Giao hàng thành công
     */
    public boolean deliverOrder(int orderId) {
        Order order = orderRepository.findById(orderId);
        if (order == null || order.getStatus() != OrderStatus.DELIVERING) {
            return false;
        }

        order.setStatus(OrderStatus.DELIVERED);
        orderRepository.update(order);

        if (order.getDriverId() != null) {
            Driver driver = driverRepository.findById(order.getDriverId());
            if (driver != null) {
                driver.setStatus(DriverStatus.AVAILABLE);
                driverRepository.update(driver);
            }
        }
        return true;
    }
}
