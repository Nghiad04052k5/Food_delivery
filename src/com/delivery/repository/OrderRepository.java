package com.delivery.repository;

import com.delivery.model.Order;
import com.delivery.model.OrderStatus;
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
            order.setPaymentMethod(parts[4].trim());
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
                entity.getPaymentMethod(),
                entity.getStatus().name(),
                entity.getVersion()
        );
    }

    @Override
    protected String getHeader() {
        return "order_id,customer_id,driver_id,total_price,payment_method,status,version";
    }

    // Cơ chế Khóa Lạc Quan (Optimistic Locking)
    public synchronized boolean assignDriverWithOptimistic(int orderId, int driverId, int expectedVersion) {
        List<Order> orders = findAll();
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
