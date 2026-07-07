package com.delivery.test;

import com.delivery.model.Order;
import com.delivery.model.OrderStatus;
import com.delivery.model.PaymentMethod;
import com.delivery.repository.OrderRepository;
import com.delivery.repository.OptimisticLockException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class OrderRepositoryTest {

    private static final String TEST_CSV_FILE = "test_orders_junit.csv";
    private OrderRepository repo;

    @BeforeEach
    public void setUp() {
        prepareTestData();
        repo = new OrderRepository(TEST_CSV_FILE);
    }

    @AfterEach
    public void tearDown() {
        File file = new File(TEST_CSV_FILE);
        if (file.exists()) {
            file.delete();
        }
    }

    private void prepareTestData() {
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
                bw.write("2,11,102,200000.0,CASH,CONFIRMED,1");
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error creating sample test data: " + e.getMessage());
        }
    }

    private void prepareFiveOrdersTestData() {
        try {
            File file = new File(TEST_CSV_FILE);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                bw.write("order_id,customer_id,driver_id,total_price,payment_method,status,version");
                bw.newLine();
                for (int i = 1; i <= 5; i++) {
                    bw.write(String.format("%d,%d,,150000.0,QR_CODE,PENDING,0", i, 10 + i));
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("Error creating 5 orders test data: " + e.getMessage());
        }
    }

    @Test
    public void testFindById() {
        Order order = repo.findById(1);
        assertNotNull(order, "Đơn hàng ID=1 phải tồn tại");
        assertEquals(10, order.getCustomerId());
        assertNull(order.getDriverId());
        assertEquals(150000.0, order.getTotalPrice(), 0.001);
        assertEquals(PaymentMethod.QR_CODE, order.getPaymentMethod());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(0, order.getVersion());
    }

    @Test
    public void testAssignDriverWithOptimistic_Success() {
        boolean result = repo.assignDriverWithOptimistic(1, 101, 0);
        assertTrue(result, "Gán tài xế thành công với version đúng");

        Order updatedOrder = repo.findById(1);
        assertNotNull(updatedOrder);
        assertEquals(101, updatedOrder.getDriverId());
        assertEquals(OrderStatus.CONFIRMED, updatedOrder.getStatus());
        assertEquals(1, updatedOrder.getVersion(), "Version phải tăng lên 1");
    }

    @Test
    public void testAssignDriverWithOptimistic_Conflict() {
        // Version thực tế của ID=1 là 0, truyền expectedVersion = 1 phải gây ra lỗi
        assertThrows(OptimisticLockException.class, () -> {
            repo.assignDriverWithOptimistic(1, 101, 1);
        }, "Phải ném ra ngoại lệ OptimisticLockException do lệch version");
    }

    @Test
    public void testAllocateOrderWithOptimisticLocking() {
        boolean result = repo.allocateOrderWithOptimisticLocking(1, 101);
        assertTrue(result);

        Order updatedOrder = repo.findById(1);
        assertNotNull(updatedOrder);
        assertEquals(101, updatedOrder.getDriverId());
        assertEquals(OrderStatus.CONFIRMED, updatedOrder.getStatus());
        assertEquals(1, updatedOrder.getVersion());
    }

    @Test
    public void testAssignDriverWithOptimistic_Contention() throws InterruptedException {
        int numThreads = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger exceptionCount = new AtomicInteger(0);

        final int expectedVersion = 0; // Version ban đầu của order ID=1 là 0

        for (int i = 1; i <= numThreads; i++) {
            final Integer driverId = 100 + i;
            executorService.execute(() -> {
                try {
                    boolean result = repo.assignDriverWithOptimistic(1, driverId, expectedVersion);
                    if (result) {
                        successCount.incrementAndGet();
                    }
                } catch (OptimisticLockException e) {
                    exceptionCount.incrementAndGet();
                }
            });
        }

        executorService.shutdown();
        boolean finished = executorService.awaitTermination(5, TimeUnit.SECONDS);
        assertTrue(finished, "Các luồng phải kết thúc trong thời gian chờ");

        // Ràng buộc kiểm chứng: Chỉ duy nhất 1 luồng thành công gán tài xế
        assertEquals(1, successCount.get(), "Chỉ duy nhất 1 luồng được gán thành công");
        assertEquals(numThreads - 1, exceptionCount.get(), "Các luồng còn lại phải gặp xung đột version");

        Order finalOrder = repo.findById(1);
        assertNotNull(finalOrder);
        assertEquals(OrderStatus.CONFIRMED, finalOrder.getStatus());
        assertEquals(1, finalOrder.getVersion(), "Phiên bản cuối cùng phải là 1");
        assertNotNull(finalOrder.getDriverId(), "Đơn hàng phải được gán cho một tài xế");
        assertTrue(finalOrder.getDriverId() >= 101 && finalOrder.getDriverId() <= 105);
    }

    @Test
    public void testTenDriversFiveOrdersContention() throws InterruptedException {
        // Chuẩn bị 5 đơn hàng
        prepareFiveOrdersTestData();

        int numDrivers = 10;
        int numOrders = 5;

        ExecutorService executorService = Executors.newFixedThreadPool(numDrivers);

        AtomicInteger successAssignments = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);

        // 10 tài xế đồng thời vào tranh chấp 5 đơn hàng
        for (int i = 1; i <= numDrivers; i++) {
            final int driverId = 100 + i; // Drivers 101 to 110
            executorService.execute(() -> {
                boolean assigned = false;
                int retries = 0;
                // Mỗi tài xế tìm kiếm đơn hàng trống và cố gắng gán, nếu trùng lặp version thì thử lại
                while (!assigned && retries < 15) {
                    java.util.List<Order> orders = repo.readAll();

                    Order targetOrder = null;
                    for (Order o : orders) {
                        if (o.getStatus() == OrderStatus.PENDING && o.getDriverId() == null) {
                            targetOrder = o;
                            break;
                        }
                    }

                    if (targetOrder == null) {
                        // Không còn đơn hàng nào trống
                        break;
                    }

                    try {
                        boolean result = repo.assignDriverWithOptimistic(targetOrder.getId(), driverId, targetOrder.getVersion());
                        if (result) {
                            assigned = true;
                            successAssignments.incrementAndGet();
                        }
                    } catch (OptimisticLockException e) {
                        // Bị luồng khác tranh chấp giành trước -> ghi nhận và thử lại đơn khác
                        conflictCount.incrementAndGet();
                        retries++;
                    }
                }
            });
        }

        executorService.shutdown();
        boolean finished = executorService.awaitTermination(10, TimeUnit.SECONDS);
        assertTrue(finished, "Các luồng tài xế phải kết thúc trong thời gian chờ");

        // Ràng buộc kiểm chứng:
        // 1. Tất cả 5 đơn hàng phải được phân bổ thành công (vì có 10 tài xế)
        assertEquals(numOrders, successAssignments.get(), "Tất cả 5 đơn hàng phải được gán thành công");

        // 2. Đọc lại dữ liệu để đảm bảo không bị trùng lặp tài xế trên một đơn hoặc một tài xế nhận nhiều đơn
        java.util.List<Order> finalOrders = repo.readAll();
        assertEquals(numOrders, finalOrders.size());

        java.util.Set<Integer> assignedDrivers = new java.util.HashSet<>();
        for (Order o : finalOrders) {
            assertEquals(OrderStatus.CONFIRMED, o.getStatus());
            assertNotNull(o.getDriverId());
            assertEquals(1, o.getVersion(), "Mỗi đơn hàng phải nâng version lên 1");

            boolean isUniqueDriver = assignedDrivers.add(o.getDriverId());
            assertTrue(isUniqueDriver, "Tài xế nhận đơn phải duy nhất: " + o.getDriverId());
        }

        System.out.println("-> Test 10 drivers / 5 orders: Success assignments = " + successAssignments.get()
                + ", Conflicts encountered = " + conflictCount.get());
    }
}
