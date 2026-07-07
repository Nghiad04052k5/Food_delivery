package com.delivery.test;

import com.delivery.model.Driver;
import com.delivery.model.DriverStatus;
import com.delivery.model.Order;
import com.delivery.model.OrderStatus;
import com.delivery.model.PaymentMethod;
import com.delivery.repository.DriverRepository;
import com.delivery.repository.OrderRepository;
import com.delivery.service.OrderService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;

public class SingleThreadOrderFlowTest {

    private OrderRepository orderRepo;
    private DriverRepository driverRepo;
    private OrderService orderService;

    private final String testOrderFile = "data/test_orders_t5.csv";
    private final String testDriverFile = "data/test_drivers_t5.csv";

    @Before
    public void setUp() {
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        // Xóa file cũ để làm sạch môi trường test
        new File(testOrderFile).delete();
        new File(testDriverFile).delete();

        orderRepo = new OrderRepository(testOrderFile);
        driverRepo = new DriverRepository(testDriverFile);

        // Tạo dummy dữ liệu tài xế trước khi test
        Driver driver = new Driver();
        driver.setId(1);
        driver.setName("Nghia Test Driver");
        driver.setLatitude(10.0);
        driver.setLongitude(10.0);
        driver.setStatus(DriverStatus.AVAILABLE);
        driverRepo.save(driver);

        orderService = new OrderService(orderRepo, driverRepo);
    }

    @After
    public void tearDown() {
        // Dọn dẹp file CSV rác sau khi test xong
        new File(testOrderFile).delete();
        new File(testDriverFile).delete();
    }

    @Test
    public void testOrderFlow() {
        System.out.println("==================================================");
        System.out.println("BẮT ĐẦU CHẠY KIỂM THỬ JUNIT: Luồng Đơn Hàng");
        System.out.println("==================================================");

        // ---------------------------------------------------------
        // 1. ĐẶT HÀNG (PLACE ORDER)
        // ---------------------------------------------------------
        System.out.println("\nBước 1: Khách hàng đặt đơn hàng mới...");
        Order order = new Order();
        order.setId(999);
        order.setCustomerId(123);
        order.setTotalPrice(150000);
        order.setPaymentMethod(PaymentMethod.CASH);
        orderService.createOrder(order);

        Order savedOrder = orderRepo.findById(999);
        assertNotNull("Đơn hàng phải được lưu thành công vào file CSV", savedOrder);
        assertEquals("Trạng thái đơn hàng lúc mới tạo phải là PENDING", OrderStatus.PENDING, savedOrder.getStatus());
        System.out.println(" => Đặt hàng thành công! Đơn hàng ID = 999, Trạng thái: " + savedOrder.getStatus());

        // ---------------------------------------------------------
        // 2. TÌM VÀ GÁN TÀI XẾ (DISPATCH ORDER)
        // ---------------------------------------------------------
        System.out.println("\nBước 2: Hệ thống tìm tài xế gần nhà hàng nhất (chạy tuần tự, ko lock)...");
        double restaurantLat = 10.05;
        double restaurantLon = 10.05;
        boolean dispatched = orderService.dispatchOrder(999, restaurantLat, restaurantLon);
        
        assertTrue("Điều phối đơn hàng phải thành công", dispatched);
        savedOrder = orderRepo.findById(999);
        Driver savedDriver = driverRepo.findById(1);
        
        assertEquals("Trạng thái đơn phải chuyển thành CONFIRMED", OrderStatus.CONFIRMED, savedOrder.getStatus());
        assertNotNull("DriverID phải được gán vào đơn hàng", savedOrder.getDriverId());
        assertEquals("Trạng thái tài xế phải chuyển thành BUSY", DriverStatus.BUSY, savedDriver.getStatus());
        System.out.println(" => Điều phối thành công!");

        // ---------------------------------------------------------
        // 3. GIAO HÀNG (DELIVER ORDER)
        // ---------------------------------------------------------
        System.out.println("\nBước 3: Tài xế hoàn thành giao hàng...");
        boolean delivered = orderService.deliverOrder(999);
        
        assertTrue("Xác nhận giao hàng phải thành công", delivered);
        savedOrder = orderRepo.findById(999);
        savedDriver = driverRepo.findById(1);
        
        assertEquals("Trạng thái đơn phải chuyển thành DELIVERED", OrderStatus.DELIVERED, savedOrder.getStatus());
        assertEquals("Trạng thái tài xế phải quay về AVAILABLE", DriverStatus.AVAILABLE, savedDriver.getStatus());
        System.out.println(" => Giao hàng thành công!");
    }
}
