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
        System.out.println("=== STARTING ORDER REPOSITORY & OPTIMISTIC LOCKING TEST ===");

        // 1. Chuẩn bị dữ liệu mẫu
        prepareTestData();

        OrderRepository repo = new OrderRepository(TEST_CSV_FILE);

        // Đọc thử dữ liệu ban đầu
        Order initialOrder = repo.findById(1);
        if (initialOrder != null) {
            System.out.println("Successfully read sample order:");
            System.out.println("ID=" + initialOrder.getId() + ", Status=" + initialOrder.getStatus() 
                    + ", DriverId=" + initialOrder.getDriverId() + ", Version=" + initialOrder.getVersion());
        } else {
            System.err.println("Error: Cannot read sample order data.");
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

        System.out.println("\n--- Starting driver assignment contention threads ---");

        for (int i = 1; i <= numThreads; i++) {
            final Integer driverId = 100 + i; // Driver IDs: 101, 102, 103, 104, 105
            final String threadName = "Thread-Assign-Driver-" + driverId;

            executorService.execute(() -> {
                Thread.currentThread().setName(threadName);
                try {
                    System.out.println("[" + Thread.currentThread().getName() + "] Trying to assign Driver ID: " + driverId + " with Expected Version: " + expectedVersion);
                    
                    // Thực hiện gán tài xế
                    boolean result = repo.assignDriverWithOptimistic(1, driverId, expectedVersion);
                    
                    if (result) {
                        successCount.incrementAndGet();
                        System.out.println(">>> [" + Thread.currentThread().getName() + "] SUCCESS: Driver ID " + driverId + " assigned successfully");
                    } else {
                        failureCount.incrementAndGet();
                    }
                } catch (OptimisticLockException e) {
                    exceptionCount.incrementAndGet();
                    System.out.println("!!! [" + Thread.currentThread().getName() + "] FAILED (Version conflict): " + e.getMessage());
                } catch (Exception e) {
                    System.err.println("[" + Thread.currentThread().getName() + "] System error: " + e.getMessage());
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
        System.out.println("\n--- CONTENTION SIMULATION RESULTS ---");
        System.out.println("Total threads run: " + numThreads);
        System.out.println("Successful assignments (Success): " + successCount.get());
        System.out.println("Rejected threads due to conflict (OptimisticLockException): " + exceptionCount.get());

        Order finalOrder = repo.findById(1);
        System.out.println("\nCurrent order info in CSV file:");
        System.out.println("ID=" + finalOrder.getId() + ", Status=" + finalOrder.getStatus() 
                + ", Assigned DriverId=" + finalOrder.getDriverId() + ", Version=" + finalOrder.getVersion());

        // Ràng buộc kiểm chứng
        if (successCount.get() == 1 && exceptionCount.get() == (numThreads - 1)) {
            System.out.println("\n=> CONCLUSION: SUCCESS! Only exactly 1 thread successfully assigned the driver.");
            System.out.println("The remaining threads were blocked and properly threw OptimisticLockException.");
            System.out.println("Race Condition 1 (Double Assignment) has been completely eliminated thanks to Optimistic Locking.");
        } else {
            System.err.println("\n=> CONCLUSION: FAILED! Optimistic Locking synchronization error.");
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
            System.err.println("Error creating sample test data: " + e.getMessage());
        }
    }
}

