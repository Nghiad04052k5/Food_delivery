package com.delivery;

import com.delivery.controller.OrderController;
import com.delivery.repository.DriverRepository;
import com.delivery.repository.OrderRepository;
import com.delivery.service.OrderService;

public class MainApplication {
    public static void main(String[] args) {
        System.out.println("Khởi động ứng dụng Food Delivery...");

        // 1. Repositories
        OrderRepository orderRepository = new OrderRepository("data/orders.csv");
        DriverRepository driverRepository = new DriverRepository("data/drivers.csv");
        
        // Cần thêm MenuItemRepository, SimulationRunRepository... sau (Thành viên khác)

        // 2. Services
        OrderService orderService = new OrderService(orderRepository, driverRepository);

        // 3. Controllers
        OrderController orderController = new OrderController(orderService);
        
        // Cần thêm CustomerController, DriverController, RestaurantController... sau (Thành viên 3)

        // 4. Views 
        // Cần thêm MainView, OrderView, MapView... sau (Thành viên 2)
        // Cần thêm SimulatorView, ReportView... sau (Thành viên 3)

        System.out.println("MVC Wiring hoàn tất.");
        System.out.println("Chương trình đã sẵn sàng lắng nghe requests (Chạy dưới dạng Console/Test script).");
    }
}
