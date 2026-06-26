package com.delivery.controller;

import com.delivery.model.Order;
import com.delivery.service.OrderService;

public class OrderController {
    private OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    public void placeOrder(Order order) {
        orderService.createOrder(order);
        System.out.println("Đã đặt đơn hàng: " + order.getId());
    }

    public void dispatchOrder(int orderId, double restaurantLat, double restaurantLon) {
        boolean success = orderService.dispatchOrder(orderId, restaurantLat, restaurantLon);
        if (success) {
            System.out.println("Đã điều phối đơn hàng " + orderId + " thành công cho tài xế.");
        } else {
            System.out.println("Lỗi điều phối đơn hàng " + orderId + " (Không tìm thấy đơn hàng hoặc không có tài xế).");
        }
    }

    public void deliverOrder(int orderId) {
        boolean success = orderService.deliverOrder(orderId);
        if (success) {
            System.out.println("Đơn hàng " + orderId + " đã được giao thành công.");
        } else {
            System.out.println("Lỗi xác nhận giao hàng cho đơn hàng " + orderId + ".");
        }
    }
}
