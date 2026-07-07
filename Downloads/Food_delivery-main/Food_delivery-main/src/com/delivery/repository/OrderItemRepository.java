package com.delivery.repository;

import com.delivery.model.OrderItem;

import java.util.Locale;

/**
 * OrderItemRepository - Đọc/ghi file order_items.csv.
 * Cấu trúc: order_item_id,order_id,menu_item_id,quantity,price_at_time
 */
public class OrderItemRepository extends CsvRepository<OrderItem> {

    public OrderItemRepository(String filePath) {
        super(filePath);
    }

    @Override
    protected OrderItem parseLine(String line) {
        String[] parts = line.split(",");
        if (parts.length >= 5) {
            OrderItem oi = new OrderItem();
            oi.setId(Integer.parseInt(parts[0].trim()));
            oi.setOrderId(Integer.parseInt(parts[1].trim()));
            oi.setMenuItemId(Integer.parseInt(parts[2].trim()));
            oi.setQuantity(Integer.parseInt(parts[3].trim()));
            oi.setPriceAtTime(Double.parseDouble(parts[4].trim()));
            return oi;
        }
        return null;
    }

    @Override
    protected String toCsvRow(OrderItem oi) {
        return String.format(Locale.US, "%d,%d,%d,%d,%.1f",
                oi.getId(), oi.getOrderId(), oi.getMenuItemId(),
                oi.getQuantity(), oi.getPriceAtTime());
    }

    @Override
    protected String getHeader() {
        return "order_item_id,order_id,menu_item_id,quantity,price_at_time";
    }
}
