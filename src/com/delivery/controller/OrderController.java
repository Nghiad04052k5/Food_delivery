package com.delivery.controller;

import com.delivery.model.Order;
import com.delivery.model.OrderStatus;
import com.delivery.repository.OptimisticLockException;
import com.delivery.repository.OrderRepository;
import com.delivery.service.OrderService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * OrderController - Controller xử lý nghiệp vụ Đơn hàng.
 *
 * Nhiệm vụ:
 * - Đặt đơn hàng mới (placeOrder)
 * - Điều phối tài xế (dispatchOrder)
 * - Xác nhận giao hàng thành công (deliverOrder)
 * - Hủy đơn hàng (cancelOrder) - Chưa dùng trong tuần này
 * - Lấy danh sách đơn hàng theo khách hàng (getOrdersByCustomer) - Chưa dùng trong tuần này
 * - Lấy một đơn hàng theo ID (getOrderById) - Chưa dùng trong tuần này
 */
public class OrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;

    public OrderController(OrderService orderService, OrderRepository orderRepository) {
        this.orderService    = orderService;
        this.orderRepository = orderRepository;
    }

    // Constructor cũ (tương thích ngược)
    public OrderController(OrderService orderService) {
        this.orderService    = orderService;
        this.orderRepository = null;
    }

    /**
     * Đặt đơn hàng mới.
     * Bắt OptimisticLockException nếu có xung đột đa luồng khi lưu đơn.
     */
    public void placeOrder(Order order) {
        try {
            orderService.createOrder(order);
            System.out.println("Da dat don hang: " + order.getId());
        } catch (OptimisticLockException e) {
            System.out.println("[!] Xung dot du lieu khi dat hang (co nhieu luong dang chay cung luc). Vui long thu lai!");
        }
    }

    /**
     * Điều phối tài xế cho một đơn hàng.
     */
    public void dispatchOrder(int orderId, double restaurantLat, double restaurantLon) {
        try {
            boolean success = orderService.dispatchOrder(orderId, restaurantLat, restaurantLon);
            if (success) {
                if (orderRepository != null) {
                    Order updatedOrder = orderRepository.findById(orderId);
                    System.out.println("Đã điều phối đơn hàng " + orderId + " thành công cho tài xế mang ID số: " + updatedOrder.getDriverId());
                } else {
                    System.out.println("Da dieu phoi don hang " + orderId + " thanh cong cho tai xe.");
                }
            } else {
                System.out.println("Loi dieu phoi don hang " + orderId
                        + " (Khong tim thay don hang hoac khong co tai xe).");
            }
        } catch (OptimisticLockException e) {
            System.out.println("Tài xế vừa bị giành mất");
        }
    }

    /**
     * Xác nhận giao hàng thành công.
     * Bắt OptimisticLockException nếu có xung đột đa luồng khi cập nhật trạng thái.
     */
    public void deliverOrder(int orderId) {
        try {
            boolean success = orderService.deliverOrder(orderId);
            if (success) {
                System.out.println("Don hang " + orderId + " da duoc giao thanh cong.");
            } else {
                System.out.println("Loi xac nhan giao hang cho don hang " + orderId + ".");
            }
        } catch (OptimisticLockException e) {
            System.out.println("[!] Trang thai don hang " + orderId + " da bi thay doi boi luong khac. Vui long lam moi!");
        }
    }

    /**
     * Tài xế chấp nhận đơn hàng
     */
    public boolean acceptOrder(int orderId, int driverId) {
        return orderService.acceptOrder(orderId, driverId);
    }

    /**
     * Tài xế từ chối đơn hàng
     */
    public boolean rejectOrder(int orderId, int driverId, double restaurantLat, double restaurantLon) {
        return orderService.rejectOrder(orderId, driverId, restaurantLat, restaurantLon);
    }

    /**
     * Lấy thông tin một đơn hàng theo ID.
     * CHƯA DÙNG TRONG TUẦN NÀY - Dành cho các tuần tiếp theo.
     */
    public Order getOrderById(int orderId) {
        if (orderRepository == null) return null;
        return orderRepository.findById(orderId);
    }

    /**
     * Hủy một đơn hàng (chỉ hủy được đơn PENDING).
     * CHƯA DÙNG TRONG TUẦN NÀY - Dành cho các tuần tiếp theo.
     */
    public boolean cancelOrder(int orderId) {
        if (orderRepository == null) return false;
        Order order = orderRepository.findById(orderId);
        if (order == null || order.getStatus() != OrderStatus.PENDING) {
            System.out.println("==> [OrderController] Khong the huy don hang #" + orderId
                    + " (khong ton tai hoac da qua trang thai PENDING).");
            return false;
        }
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.update(order);
        System.out.println("==> [OrderController] Da huy don hang #" + orderId + " thanh cong.");
        return true;
    }

    /**
     * Lấy danh sách các đơn hàng đang active (chưa DELIVERED, chưa CANCELLED) của một khách hàng.
     * CHƯA DÙNG TRONG TUẦN NÀY - Dành cho các tuần tiếp theo.
     */
    public List<Order> getActiveOrdersByCustomer(int customerId) {
        if (orderRepository == null) return java.util.Collections.emptyList();
        return orderRepository.readAll().stream()
                .filter(o -> o.getCustomerId() == customerId
                        && o.getStatus() != OrderStatus.DELIVERED
                        && o.getStatus() != OrderStatus.CANCELLED)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách lịch sử đơn hàng (DELIVERED hoặc CANCELLED) của một khách hàng.
     * CHƯA DÙNG TRONG TUẦN NÀY - Dành cho các tuần tiếp theo.
     */
    public List<Order> getPastOrdersByCustomer(int customerId) {
        if (orderRepository == null) return java.util.Collections.emptyList();
        return orderRepository.readAll().stream()
                .filter(o -> o.getCustomerId() == customerId
                        && (o.getStatus() == OrderStatus.DELIVERED
                            || o.getStatus() == OrderStatus.CANCELLED))
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách đơn hàng đang giao (DELIVERING) của một tài xế.
     * CHƯA DÙNG TRONG TUẦN NÀY - Dành cho các tuần tiếp theo.
     */
    public List<Order> getDeliveringOrdersByDriver(int driverId) {
        if (orderRepository == null) return java.util.Collections.emptyList();
        return orderRepository.readAll().stream()
                .filter(o -> o.getDriverId() != null
                        && o.getDriverId() == driverId
                        && o.getStatus() == OrderStatus.DELIVERING)
                .collect(Collectors.toList());
    }

    /**
     * Lưu thay đổi một đơn hàng (trạng thái, driver ID...) xuống file CSV.
     * CHƯA DÙNG TRONG TUẦN NÀY - Dành cho các tuần tiếp theo.
     */
    public void saveOrder(Order order) {
        if (orderRepository != null) {
            orderRepository.update(order);
        }
    }
}
