package com.delivery.service;

import com.delivery.model.Driver;
import com.delivery.model.DriverStatus;
import com.delivery.model.Order;
import com.delivery.model.OrderStatus;
import com.delivery.repository.DriverRepository;
import com.delivery.repository.OrderRepository;

public class OrderService {
    private OrderRepository orderRepository;
    private DriverRepository driverRepository;

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
     * Phiên bản T5 chạy tuần tự, không dùng cơ chế Lock.
     */
    public boolean dispatchOrder(int orderId, double restaurantLat, double restaurantLon) {
        Order order = orderRepository.findById(orderId);
        if (order == null || order.getStatus() != OrderStatus.PENDING) {
            return false;
        }

        // 1. Tìm tài xế rảnh rỗi gần nhất
        Driver nearestDriver = driverRepository.findNearestAvailableDriver(restaurantLat, restaurantLon);
        if (nearestDriver != null) {
            // 2. Gán tài xế bận (không lock)
            nearestDriver.setStatus(DriverStatus.BUSY);
            driverRepository.update(nearestDriver);

            // 3. Cập nhật đơn hàng
            order.setDriverId(nearestDriver.getId());
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.update(order);
            return true;
        }
        return false;
    }

    /**
     * Giao hàng thành công
     */
    public boolean deliverOrder(int orderId) {
        Order order = orderRepository.findById(orderId);
        if (order == null || order.getStatus() != OrderStatus.CONFIRMED) {
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
