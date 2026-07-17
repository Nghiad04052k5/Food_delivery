package com.delivery.repository;

import com.delivery.model.Order;
import com.delivery.model.OrderStatus;
import com.delivery.model.PaymentMethod;

import java.util.List;
import java.util.Locale;

public class OrderRepository extends CsvRepository<Order> {

    public OrderRepository(String filePath) {
        super(filePath);
    }

    @Override
    protected Order parseLine(String line) {
        String[] parts = line.split(",");
        if (parts.length >= 7) {
            Order order = new Order();
            order.setId(Integer.parseInt(parts[0].trim()));
            order.setCustomerId(Integer.parseInt(parts[1].trim()));

            String driverIdStr = parts[2].trim();
            order.setDriverId(driverIdStr.isEmpty() ? null : Integer.parseInt(driverIdStr));

            order.setTotalPrice(Double.parseDouble(parts[3].trim()));
            order.setPaymentMethod(PaymentMethod.valueOf(parts[4].trim()));
            order.setStatus(OrderStatus.valueOf(parts[5].trim()));
            order.setVersion(Integer.parseInt(parts[6].trim()));
            return order;
        }
        return null;
    }

    @Override
    protected String toCsvRow(Order entity) {
        return String.format(Locale.US, "%d,%d,%s,%.1f,%s,%s,%d",
                entity.getId(),
                entity.getCustomerId(),
                entity.getDriverId() == null ? "" : String.valueOf(entity.getDriverId()),
                entity.getTotalPrice(),
                entity.getPaymentMethod().name(),
                entity.getStatus().name(),
                entity.getVersion());
    }

    @Override
    protected String getHeader() {
        return "order_id,customer_id,driver_id,total_price,payment_method,status,version";
    }

    public boolean validateAndAssignDriverAtomically(int orderId, int driverId) {
        LockMechanism mechanism = LockConfig.getMechanism();

        if (mechanism == LockMechanism.NO_LOCK) {
            return assignWithoutLock(orderId, driverId);
        } else if (mechanism == LockMechanism.SYNCHRONIZED) {
            synchronized (LockManager.getLock("Order_" + orderId)) {
                return assignWithoutLock(orderId, driverId);
            }
        } else if (mechanism == LockMechanism.FILE_LOCK) {
            boolean[] result = new boolean[1];
            LockManager.executeWithFileLock(this.filePath, () -> {
                result[0] = assignWithoutLock(orderId, driverId);
            });
            return result[0];
        } else if (mechanism == LockMechanism.OPTIMISTIC) {
            return assignOptimistic(orderId, driverId);
        }
        return false;
    }

    private boolean assignWithoutLock(int orderId, int driverId) {
        List<Order> allOrders = readAll();
        Order targetOrder = null;
        for (Order o : allOrders) {
            if (o.getId() == orderId) {
                targetOrder = o;
                break;
            }
        }

        if (targetOrder == null || targetOrder.getDriverId() != null) return false;

        // Giả lập trễ
        try { Thread.sleep(5); } catch (InterruptedException ignored) {}

        targetOrder.setDriverId(driverId);
        targetOrder.setStatus(OrderStatus.CONFIRMED);
        saveAll(allOrders);
        return true;
    }

    private boolean assignOptimistic(int orderId, int driverId) {
        Order initialOrder = findById(orderId);
        if (initialOrder == null || initialOrder.getDriverId() != null) return false;
        
        int expectedVersion = initialOrder.getVersion();
        
        try { Thread.sleep(5); } catch (InterruptedException ignored) {}

        boolean[] result = new boolean[1];
        synchronized (LockManager.getLock("Order_DB_" + orderId)) {
            List<Order> allOrders = readAll();
            Order targetOrder = null;
            for (Order o : allOrders) {
                if (o.getId() == orderId) {
                    targetOrder = o;
                    break;
                }
            }
            
            if (targetOrder != null) {
                if (targetOrder.getVersion() != expectedVersion) {
                    throw new OptimisticLockException("Version mismatch for Order " + orderId);
                }
                if (targetOrder.getDriverId() == null) {
                    targetOrder.setDriverId(driverId);
                    targetOrder.setStatus(OrderStatus.CONFIRMED);
                    targetOrder.setVersion(expectedVersion + 1);
                    saveAll(allOrders);
                    result[0] = true;
                }
            }
        }
        return result[0];
    }

    // Cơ chế Khóa Lạc Quan (Optimistic Locking) cũ để tương thích với test
    public synchronized boolean allocateOrderWithOptimisticLocking(int orderId, Integer driverId) {
        List<Order> orders = readAll();
        for (Order o : orders) {
            if (o.getId() == orderId) {
                o.setDriverId(driverId);
                o.setStatus(OrderStatus.CONFIRMED);
                o.setVersion(o.getVersion() + 1);
                saveAll(orders);
                return true;
            }
        }
        return false;
    }

    // Giữ lại hàm cũ để Test của Nguyên không bị lỗi do thiếu tham số expectedVersion
    public synchronized boolean assignDriverWithOptimistic(int orderId, Integer driverId, int expectedVersion) {
        List<Order> orders = readAll();
        for (Order o : orders) {
            if (o.getId() == orderId) {
                if (o.getVersion() != expectedVersion) {
                    throw new OptimisticLockException("Đơn hàng đã được gán bởi luồng khác! Phiên bản mong đợi: "
                            + expectedVersion + ", Phiên bản thực tế: " + o.getVersion());
                }
                o.setDriverId(driverId);
                o.setStatus(OrderStatus.CONFIRMED);
                o.setVersion(o.getVersion() + 1);
                saveAll(orders);
                return true;
            }
        }
        return false;
    }
}
