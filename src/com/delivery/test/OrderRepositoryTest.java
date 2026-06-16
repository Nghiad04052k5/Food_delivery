package com.delivery.test;

import com.delivery.model.Order;
import com.delivery.model.OrderStatus;
import com.delivery.repository.OrderRepository;
import com.delivery.repository.OptimisticLockException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class OrderRepositoryTest {

    private static final String TEST_CSV_FILE = "test_orders.csv";

    public static void main(String[] args) {
        System.out.println("=== BẮT ĐẦU TEST LỚP ORDER REPOSITORY & KHÓA LẠC QUAN (OPTIMISTIC LOCKING) ===");

        // 1. Chuẩn bị dữ liệu mẫu
        prepareTestData();

        OrderRepository repo = new OrderRepository(TEST_CSV_FILE);

        // Đọc thử dữ liệu ban đầu
        Order initialOrder = repo.findById(1);
        if (initialOrder != null) {
            System.out.println("Đọc đơn hàng mẫu thành công:");
            System.out.println("ID=" + initialOrder.getId() + ", Status=" + initialOrder.getStatus() 
                    + ", DriverId=" + initialOrder.getDriverId() + ", Version=" + initialOrder.getVersion());
        } else {
            System.err.println("Lỗi: Không đọc được dữ liệu đơn hàng mẫu.");
            return;
        }

        // 2. Thiết lập chạy đa luồng để kiểm tra tranh chấp (Optimistic Locking)
        // Chúng ta có 5 luồng cùng cố gắng gán 5 tài xế khác nhau cho duy nhất đơn hàng ID=1 (có version ban đầu là 0)
        int numThreads = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicInteger exceptionCount = new AtomicInteger(0);

        final int expectedVersion = initialOrder.getVersion(); // là 0

        System.out.println("\n--- Bắt đầu kích hoạt luồng tranh chấp gán đơn hàng ---");

        for (int i = 1; i <= numThreads; i++) {
            final Integer driverId = 100 + i; // Driver IDs: 101, 102, 103, 104, 105
            final String threadName = "Thread-Assign-Driver-" + driverId;

            executorService.execute(() -> {
                Thread.currentThread().setName(threadName);
                try {
                    System.out.println("[" + Thread.currentThread().getName() + "] Đang cố gắng gán Driver ID: " + driverId + " với Expected Version: " + expectedVersion);
                    
                    // Thực hiện gán tài xế
                    boolean result = repo.assignDriverWithOptimistic(1, driverId, expectedVersion);
                    
                    if (result) {
                        successCount.incrementAndGet();
                        System.out.println(">>> [" + Thread.currentThread().getName() + "] THÀNH CÔNG: Đã gán thành công Driver ID " + driverId);
                    } else {
                        failureCount.incrementAndGet();
                    }
                } catch (OptimisticLockException e) {
                    exceptionCount.incrementAndGet();
                    System.out.println("!!! [" + Thread.currentThread().getName() + "] THẤT BẠI (Xung đột version): " + e.getMessage());
                } catch (Exception e) {
                    System.err.println("[" + Thread.currentThread().getName() + "] Lỗi hệ thống: " + e.getMessage());
                }
            });
        }

        executorService.shutdown();
        try {
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 3. Kiểm tra kết quả cuối cùng
        System.out.println("\n--- KẾT QUẢ MÔ PHỎNG TRANH CHẤP ---");
        System.out.println("Tổng số luồng chạy: " + numThreads);
        System.out.println("Số luồng gán thành công (Success): " + successCount.get());
        System.out.println("Số luồng bị từ chối do xung đột (OptimisticLockException): " + exceptionCount.get());

        Order finalOrder = repo.findById(1);
        System.out.println("\nThông tin đơn hàng hiện tại trong file CSV:");
        System.out.println("ID=" + finalOrder.getId() + ", Status=" + finalOrder.getStatus() 
                + ", Assigned DriverId=" + finalOrder.getDriverId() + ", Version=" + finalOrder.getVersion());

        // Ràng buộc kiểm chứng
        if (successCount.get() == 1 && exceptionCount.get() == (numThreads - 1)) {
            System.out.println("\n=> KẾT LUẬN: THÀNH CÔNG! Chỉ có duy nhất 1 luồng gán tài xế thành công.");
            System.out.println("Các luồng còn lại đều bị chặn và ném ra OptimisticLockException chính xác.");
            System.out.println("Race Condition 1 (Double Assignment) đã được loại bỏ hoàn toàn nhờ Khóa Lạc Quan.");
        } else {
            System.err.println("\n=> KẾT LUẬN: THẤT BẠI! Lỗi đồng bộ hóa Khóa Lạc Quan.");
        }

        // Dọn dẹp file test
        File file = new File(TEST_CSV_FILE);
        if (file.exists()) {
            file.delete();
        }
    }

    private static void prepareTestData() {
        try {
            File file = new File(TEST_CSV_FILE);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                bw.write("order_id,customer_id,driver_id,total_price,payment_method,status,version");
                bw.newLine();
                bw.write("1,10,,150000.0,QR_CODE,PENDING,0");
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Lỗi tạo dữ liệu mẫu test: " + e.getMessage());
        }
    }
}

