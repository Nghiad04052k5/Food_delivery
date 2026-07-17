package com.delivery.simulator;

import com.delivery.model.*;
import com.delivery.repository.*;
import com.delivery.service.OrderService;
import com.delivery.view.ReportView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class SimulatorController {

    private final MenuItemRepository menuItemRepo;
    private final OrderRepository orderRepo;
    private final DriverRepository driverRepo;
    private final OrderService orderService;
    private final ReportView reportView;

    public SimulatorController(MenuItemRepository menuItemRepo, OrderRepository orderRepo, DriverRepository driverRepo, OrderService orderService, ReportView reportView) {
        this.menuItemRepo = menuItemRepo;
        this.orderRepo = orderRepo;
        this.driverRepo = driverRepo;
        this.orderService = orderService;
        this.reportView = reportView;
    }

    public void runSimulation() {
        System.out.println("\n=== BAT DAU CONG CU BAN TAI MO PHONG 1000 DON HANG ===");
        LockMechanism[] mechanisms = {LockMechanism.NO_LOCK, LockMechanism.SYNCHRONIZED, LockMechanism.FILE_LOCK, LockMechanism.OPTIMISTIC};

        for (LockMechanism mech : mechanisms) {
            System.out.println("\n>> Kich ban: " + mech);
            LockConfig.setMechanism(mech);
            
            resetTestData(); // Khoi tao du lieu mau 

            int TOTAL_CUSTOMERS = 100;
            int TOTAL_DISPATCHERS = 20;

            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch endLatch = new CountDownLatch(TOTAL_CUSTOMERS + TOTAL_DISPATCHERS);

            AtomicInteger successCounter = new AtomicInteger(0);
            AtomicInteger failCounter = new AtomicInteger(0);

            List<Thread> threads = new ArrayList<>();

            // Tao Customer Threads
            for (int i = 0; i < TOTAL_CUSTOMERS; i++) {
                threads.add(new CustomerThread(startLatch, endLatch, successCounter, failCounter));
            }

            // Tao Dispatch Threads
            for (int i = 0; i < TOTAL_DISPATCHERS; i++) {
                threads.add(new DispatchThread(startLatch, endLatch));
            }

            long startTime = System.currentTimeMillis();

            for (Thread t : threads) {
                t.start();
            }

            System.out.println("Chuan bi... 3... 2... 1... START!");
            startLatch.countDown(); // Bat dau cung luc

            try {
                endLatch.await(); // Cho tat ca chay xong
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            long duration = System.currentTimeMillis() - startTime;

            // Hau kiem tra CSV
            int errorOversell = detectOversellItems();
            int errorDriverOverload = detectDriverOverload();
            int errorDoubleAssign = 0; // Hien tai gia dinh cac loi khac the hien qua oversell/overload

            reportView.saveSimulationResult(TOTAL_CUSTOMERS, successCounter.get(), failCounter.get(), duration, mech.name(), errorDoubleAssign, errorOversell, errorDriverOverload);
        }

        reportView.printLockComparisonTableFromFile();
    }

    private void resetTestData() {
        // Reset 1 mon an (ID 1) voi stock 50
        MenuItem m = new MenuItem(1, 1, "Mon An Test", 10000, 50, 0);
        menuItemRepo.saveAll(Collections.singletonList(m));

        // Reset Drivers (20 drivers)
        List<Driver> drivers = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            drivers.add(new Driver(i, "Tai xe " + i, "0123", "tx"+i+"@gmail.com", "pass", 10.0, 106.0, 0, DriverStatus.AVAILABLE, 0));
        }
        driverRepo.saveAll(drivers);

        // Reset Orders
        orderRepo.saveAll(new ArrayList<>());
    }

    private int detectOversellItems() {
        List<MenuItem> items = menuItemRepo.readAll();
        int errors = 0;
        for (MenuItem item : items) {
            if (item.getStockQty() < 0) {
                errors++;
            }
        }
        return errors;
    }

    private int detectDriverOverload() {
        List<Order> orders = orderRepo.readAll();
        Map<Integer, Integer> driverOrderCount = new HashMap<>();
        int errors = 0;

        for (Order o : orders) {
            if (o.getDriverId() != null && o.getStatus() == OrderStatus.CONFIRMED) {
                int count = driverOrderCount.getOrDefault(o.getDriverId(), 0);
                if (count >= 1) {
                    errors++; // Driver nay dang om tu 2 don tro len cung luc
                }
                driverOrderCount.put(o.getDriverId(), count + 1);
            }
        }
        return errors;
    }

    // INNER CLASSES CHO THREADS

    class CustomerThread extends Thread {
        private CountDownLatch startLatch, endLatch;
        private AtomicInteger successCounter, failCounter;

        public CustomerThread(CountDownLatch startLatch, CountDownLatch endLatch, AtomicInteger successCounter, AtomicInteger failCounter) {
            this.startLatch = startLatch;
            this.endLatch = endLatch;
            this.successCounter = successCounter;
            this.failCounter = failCounter;
        }

        @Override
        public void run() {
            try {
                startLatch.await();
                // Khach hang dat mua mon 1
                boolean success = false;
                try {
                    success = menuItemRepo.validateAndDeductStockAtomically(1, 1);
                } catch (OptimisticLockException e) {
                    success = false;
                }

                if (success) {
                    // Neu mua thanh cong, tao Order (De DispatchThread xu ly)
                    Order o = new Order();
                    o.setCustomerId(1);
                    o.setTotalPrice(10000);
                    o.setStatus(OrderStatus.PENDING);
                    o.setPaymentMethod(PaymentMethod.CASH);
                    o.setVersion(0);
                    
                    synchronized (orderService) {
                        // De don gian hoa ID, tao id moi an toan
                        o.setId(orderRepo.readAll().size() + 1);
                        orderService.createOrder(o);
                    }
                    successCounter.incrementAndGet();
                } else {
                    failCounter.incrementAndGet();
                }
            } catch (Exception e) {
                failCounter.incrementAndGet();
            } finally {
                endLatch.countDown();
            }
        }
    }

    class DispatchThread extends Thread {
        private CountDownLatch startLatch, endLatch;

        public DispatchThread(CountDownLatch startLatch, CountDownLatch endLatch) {
            this.startLatch = startLatch;
            this.endLatch = endLatch;
        }

        @Override
        public void run() {
            try {
                startLatch.await();
                // Dispatcher quet don hang PENDING de giao cho tai xe
                for (int i = 0; i < 5; i++) { // Thu 5 lan
                    List<Order> orders = orderRepo.readAll();
                    for (Order o : orders) {
                        if (o.getStatus() == OrderStatus.PENDING) {
                            try {
                                orderService.dispatchOrder(o.getId(), 10.0, 106.0);
                            } catch (OptimisticLockException ignored) {
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // Ignore
            } finally {
                endLatch.countDown();
            }
        }
    }
}
