package com.delivery;

import com.delivery.controller.CustomerController;
import com.delivery.controller.DriverController;
import com.delivery.controller.OrderController;
import com.delivery.controller.RestaurantController;
import com.delivery.repository.*;
import com.delivery.service.OrderService;
import com.delivery.view.ReportView;
import com.delivery.view.SimulatorView;

/**
 * MainApplication - Điểm khởi động chính của hệ thống Food Delivery.
 *
 * Tuân theo kiến trúc MVC:
 * 1. Khởi tạo toàn bộ Repository (tầng dữ liệu CSV)
 * 2. Khởi tạo Service (tầng nghiệp vụ phức tạp - OrderService)
 * 3. Khởi tạo Controller (tầng điều phối - inject Repository vào)
 * 4. Khởi tạo View (tầng giao diện - inject Controller vào)
 * 5. In báo cáo thống kê lịch sử (ReportView)
 * 6. Khởi chạy giao diện tương tác 4 vai trò (SimulatorView)
 */
public class MainApplication {

    // Đường dẫn tới các file CSV dữ liệu
    private static final String ORDER_CSV      = "data/orders.csv";
    private static final String DRIVER_CSV     = "data/drivers.csv";
    private static final String CUSTOMER_CSV   = "data/customers.csv";
    private static final String RESTAURANT_CSV = "data/restaurants.csv";
    private static final String MENU_ITEM_CSV  = "data/menu_items.csv";
    private static final String ORDER_ITEM_CSV = "data/order_items.csv";
    private static final String SIMULATION_CSV = "data/simulation_runs.csv";

    private static String repeatChar(char c, int count) {
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        System.out.println("Khoi dong ung dung Food Delivery...");

        // =========================================================
        // LAYER 1: REPOSITORIES (Tầng dữ liệu - đọc/ghi CSV)
        // =========================================================
        CustomerRepository   customerRepo   = new CustomerRepository(CUSTOMER_CSV);
        RestaurantRepository restaurantRepo = new RestaurantRepository(RESTAURANT_CSV);
        DriverRepository     driverRepo     = new DriverRepository(DRIVER_CSV);
        MenuItemRepository   menuItemRepo   = new MenuItemRepository(MENU_ITEM_CSV);
        OrderRepository      orderRepo      = new OrderRepository(ORDER_CSV);
        OrderItemRepository  orderItemRepo  = new OrderItemRepository(ORDER_ITEM_CSV);

        System.out.println("Tat ca Repositories da san sang.");
        System.out.println(repeatChar('-', 60));

        // =========================================================
        // LAYER 2: SERVICE (Tầng nghiệp vụ phức tạp)
        // =========================================================
        OrderService orderService = new OrderService(orderRepo, driverRepo);

        // =========================================================
        // LAYER 3: CONTROLLERS (Tầng điều phối - inject Repository)
        // =========================================================
        CustomerController   customerController   = new CustomerController(customerRepo);
        DriverController     driverController     = new DriverController(driverRepo);
        RestaurantController restaurantController = new RestaurantController(
                restaurantRepo, menuItemRepo, orderRepo, orderItemRepo);
        OrderController      orderController      = new OrderController(orderService, orderRepo);

        System.out.println("Tat ca Controllers da san sang.");
        System.out.println(repeatChar('-', 60));

        // =========================================================
        // LAYER 4: VIEWS (Tầng giao diện - inject Controller)
        // =========================================================

        // ReportView: In báo cáo thống kê lịch sử mô phỏng trước khi vào menu
        ReportView reportView = new ReportView(SIMULATION_CSV);
        reportView.printSummaryReport();

        // SimulatorView: Giao diện tương tác chính - nhận Controller + Repository
        SimulatorView simulatorView = new SimulatorView(
                customerController,
                restaurantController,
                driverController,
                orderController,
                customerRepo,
                restaurantRepo,
                driverRepo,
                orderRepo,
                orderItemRepo,
                menuItemRepo
        );

        // =========================================================
        // KHỞI CHẠY: Vào vòng lặp menu 4 vai trò
        // =========================================================
        simulatorView.start();
    }
}
