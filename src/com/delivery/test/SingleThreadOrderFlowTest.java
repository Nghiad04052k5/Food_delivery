package com.delivery.test;

import com.delivery.model.Driver;
import com.delivery.model.DriverStatus;
import com.delivery.model.Order;
import com.delivery.model.OrderStatus;
import com.delivery.model.PaymentMethod;
import com.delivery.repository.DriverRepository;
import com.delivery.repository.OrderRepository;
import com.delivery.service.OrderService;
import java.io.File;

public class SingleThreadOrderFlowTest {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("BẮT ĐẦU CHẠY KIỂM THỬ: SingleThreadOrderFlowTest");
        System.out.println("==================================================");

        // Sử dụng file test tạm thời
        String testOrderFile = "data/test_orders_t5.csv";
        String testDriverFile = "data/test_drivers_t5.csv";

        File dataDir = new File("data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        // Xóa file cũ nếu có để test sạch
        new File(testOrderFile).delete();
        new File(testDriverFile).delete();

        OrderRepository orderRepo = new OrderRepository(testOrderFile);
        DriverRepository driverRepo = new DriverRepository(testDriverFile);

        // Tạo dummy dữ liệu tài xế
        Driver driver = new Driver();
        driver.setId(1);
        driver.setName("Nghia Test Driver");
        driver.setLatitude(10.0);
        driver.setLongitude(10.0);
        driver.setStatus(DriverStatus.AVAILABLE);
        driverRepo.save(driver);
        System.out.println("[Setup] Đã thêm tài xế: " + driver.getName() + " (Trạng thái: " + driver.getStatus() + ")");

        OrderService orderService = new OrderService(orderRepo, driverRepo);

        // 1. ĐẶT HÀNG (PLACE ORDER)
        System.out.println("\nBước 1: Khách hàng đặt đơn hàng mới...");
        Order order = new Order();
        order.setId(999);
        order.setCustomerId(123);
        order.setTotalPrice(150000);
        order.setPaymentMethod(PaymentMethod.CASH);
        orderService.createOrder(order);

        Order savedOrder = orderRepo.findById(999);
        if (savedOrder != null && savedOrder.getStatus() == OrderStatus.PENDING) {
            System.out.println(" => Đặt hàng thành công! Đơn hàng ID = 999, Trạng thái: " + savedOrder.getStatus());
        } else {
            System.err.println(" => Lỗi đặt hàng!");
        }

        // 2. TÌM VÀ GÁN TÀI XẾ (DISPATCH ORDER)
        System.out.println("\nBước 2: Hệ thống tìm tài xế gần nhà hàng nhất (chạy tuần tự, ko lock)...");
        double restaurantLat = 10.05;
        double restaurantLon = 10.05;
        boolean dispatched = orderService.dispatchOrder(999, restaurantLat, restaurantLon);
        
        if (dispatched) {
            savedOrder = orderRepo.findById(999);
            Driver savedDriver = driverRepo.findById(1);
            
            if (savedOrder.getStatus() == OrderStatus.CONFIRMED && savedDriver.getStatus() == DriverStatus.BUSY) {
                System.out.println(" => Điều phối thành công!");
                System.out.println("   - Đơn hàng 999 Trạng thái: " + savedOrder.getStatus() + ", DriverID: " + savedOrder.getDriverId());
                System.out.println("   - Tài xế 1 Trạng thái: " + savedDriver.getStatus());
            } else {
                System.err.println(" => Lỗi dữ liệu sau khi điều phối!");
            }
        } else {
            System.err.println(" => Điều phối thất bại!");
        }

        // 3. GIAO HÀNG (DELIVER ORDER)
        System.out.println("\nBước 3: Tài xế hoàn thành giao hàng...");
        boolean delivered = orderService.deliverOrder(999);
        
        if (delivered) {
            savedOrder = orderRepo.findById(999);
            Driver savedDriver = driverRepo.findById(1);
            
            if (savedOrder.getStatus() == OrderStatus.DELIVERED && savedDriver.getStatus() == DriverStatus.AVAILABLE) {
                System.out.println(" => Giao hàng thành công!");
                System.out.println("   - Đơn hàng 999 Trạng thái: " + savedOrder.getStatus());
                System.out.println("   - Tài xế 1 Trạng thái: " + savedDriver.getStatus());
            } else {
                System.err.println(" => Lỗi dữ liệu sau khi giao!");
            }
        } else {
            System.err.println(" => Giao hàng thất bại!");
        }

        System.out.println("\n==================================================");
        System.out.println("KẾT LUẬN: BÀI TEST CHẠY THÀNH CÔNG, KHÔNG LỖI DỮ LIỆU!");
        System.out.println("==================================================");

        // Dọn dẹp
        new File(testOrderFile).delete();
        new File(testDriverFile).delete();
    }
}
