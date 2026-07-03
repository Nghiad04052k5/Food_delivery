package com.delivery.view;

import com.delivery.model.*;
import com.delivery.repository.OrderItemRepository;
import java.util.*;

/**
 * OrderView - Giao diện hiển thị thông tin đơn hàng với bảng dữ liệu căn chỉnh đẹp mắt.
 * 
 * Chức năng:
 * - Format bảng dữ liệu Console với padding và căn chỉnh lề
 * - Hiển thị thông tin chi tiết đơn hàng
 * - Danh sách các món ăn trong đơn với giá
 * - Thông tin tài xế giao hàng
 */
public class OrderView {

    // ANSI color codes
    private static final String RESET  = "\u001B[0m";
    private static final String CYAN   = "\u001B[36m";
    private static final String GREEN  = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED    = "\u001B[31m";
    private static final String BOLD   = "\u001B[1m";

    private final OrderItemRepository orderItemRepository;

    public OrderView(String orderItemCsvPath) {
        this.orderItemRepository = new OrderItemRepository(orderItemCsvPath);
    }

    /**
     * In chi tiết một đơn hàng
     */
    public void printOrderDetail(Order order, Customer customer, Restaurant restaurant, Driver driver) {
        System.out.println();
        System.out.println(CYAN + BOLD + "╔══════════════════════════════════════════════════════════════════════════════╗" + RESET);
        System.out.println(CYAN + "║" + BOLD + " CHI TIET DON HANG #" + padRight(String.valueOf(order.getId()), 58) + CYAN + "║" + RESET);
        System.out.println(CYAN + BOLD + "╠══════════════════════════════════════════════════════════════════════════════╣" + RESET);

        // Thông tin chung
        System.out.println(CYAN + "║" + RESET + " Thong tin chung:");
        System.out.println(CYAN + "║" + RESET + String.format("  - Trang thai: %-68s" + CYAN + "║" + RESET, getStatusColor(order.getStatus()) + order.getStatus() + RESET));
        System.out.println(CYAN + "║" + RESET + String.format("  - Phuong thuc thanh toan: %-58s" + CYAN + "║" + RESET, order.getPaymentMethod()));
        System.out.println(CYAN + "║" + RESET + "");

        // Thông tin khách hàng
        System.out.println(CYAN + "║" + RESET + " Khach hang:");
        System.out.println(CYAN + "║" + RESET + String.format("  - Ten: %-75s" + CYAN + "║" + RESET, truncate(customer.getName(), 75)));
        System.out.println(CYAN + "║" + RESET + String.format("  - Dien thoai: %-71s" + CYAN + "║" + RESET, customer.getPhone()));
        System.out.println(CYAN + "║" + RESET + String.format("  - Dia chi: %-72s" + CYAN + "║" + RESET, truncate(customer.getAddress(), 72)));
        System.out.println(CYAN + "║" + RESET + "");

        // Thông tin nhà hàng
        System.out.println(CYAN + "║" + RESET + " Nha hang:");
        System.out.println(CYAN + "║" + RESET + String.format("  - Ten: %-75s" + CYAN + "║" + RESET, truncate(restaurant.getName(), 75)));
        System.out.println(CYAN + "║" + RESET + String.format("  - Dien thoai: %-71s" + CYAN + "║" + RESET, restaurant.getPhone()));
        System.out.println(CYAN + "║" + RESET + String.format("  - Dia chi: %-72s" + CYAN + "║" + RESET, truncate(restaurant.getAddress(), 72)));
        System.out.println(CYAN + "║" + RESET + "");

        // Thông tin tài xế (nếu có)
        if (driver != null) {
            System.out.println(CYAN + "║" + RESET + " Tai xe giao hang:");
            System.out.println(CYAN + "║" + RESET + String.format("  - Ten: %-75s" + CYAN + "║" + RESET, truncate(driver.getName(), 75)));
            System.out.println(CYAN + "║" + RESET + String.format("  - Dien thoai: %-71s" + CYAN + "║" + RESET, driver.getPhone()));
            System.out.println(CYAN + "║" + RESET + String.format("  - Trang thai: %-70s" + CYAN + "║" + RESET, driver.getStatus()));
            System.out.println(CYAN + "║" + RESET + "");
        }

        System.out.println(CYAN + BOLD + "╚══════════════════════════════════════════════════════════════════════════════╝" + RESET);
    }

    /**
     * In bảng danh sách các món ăn trong đơn
     */
    public void printOrderItems(int orderId, List<MenuItem> items, List<OrderItem> orderItems) {
        System.out.println();
        System.out.println(CYAN + BOLD + "╔════════════════════════════════════════════════════════════════╗" + RESET);
        System.out.println(CYAN + "║" + BOLD + " DANH SACH MON AN TRONG DON" + padRight("", 39) + CYAN + "║" + RESET);
        System.out.println(CYAN + BOLD + "╠════════════════════════════════════════════════════════════════╣" + RESET);

        if (orderItems.isEmpty()) {
            System.out.println(CYAN + "║" + RESET + " Khong co mon an nao trong don hang nay!           " + CYAN + "║" + RESET);
        } else {
            System.out.println(CYAN + "║" + RESET + String.format(" %-4s │ %-30s │ %-8s │ %-12s │",
                    "STT", "TEN MON", "SO LUONG", "GIA"));
            System.out.println(CYAN + BOLD + "╠════════════════════════════════════════════════════════════════╣" + RESET);

            int stt = 1;
            for (OrderItem oi : orderItems) {
                String itemName = "";
                for (MenuItem item : items) {
                    if (item.getId() == oi.getMenuItemId()) {
                        itemName = item.getItemName();
                        break;
                    }
                }

                System.out.println(CYAN + "║" + RESET + String.format(" %-4d │ %-30s │ %-8d │ %10.0f VND │",
                        stt,
                        truncate(itemName, 30),
                        oi.getQuantity(),
                        oi.getPriceAtTime() * oi.getQuantity()));
                stt++;
            }
        }

        System.out.println(CYAN + BOLD + "╚════════════════════════════════════════════════════════════════╝" + RESET);
    }

    /**
     * In tóm tắt giá tiền
     */
    public void printPriceSummary(Order order) {
        System.out.println();
        System.out.println(CYAN + BOLD + "╔════════════════════════════════════════════════════════════════╗" + RESET);
        System.out.println(CYAN + "║" + BOLD + " TONG KET GIA TIEN" + padRight("", 48) + CYAN + "║" + RESET);
        System.out.println(CYAN + BOLD + "╠════════════════════════════════════════════════════════════════╣" + RESET);
        System.out.println(CYAN + "║" + RESET + String.format(" %-40s %15.0f VND " + CYAN + "║" + RESET,
                "Tong tien hang:", order.getTotalPrice()));
        System.out.println(CYAN + BOLD + "╠════════════════════════════════════════════════════════════════╣" + RESET);
        System.out.println(CYAN + "║" + RESET + String.format(" " + BOLD + "%-40s %15.0f VND" + RESET + " " + CYAN + "║" + RESET,
                "TONG CONG:", order.getTotalPrice()));
        System.out.println(CYAN + BOLD + "╚════════════════════════════════════════════════════════════════╝" + RESET);
        System.out.println();
    }

    /**
     * In bảng danh sách đơn hàng ngắn gọn
     */
    public void printOrdersList(List<Order> orders) {
        System.out.println();
        System.out.println(CYAN + BOLD + "╔════════════════════════════════════════════════════════════════════════════════╗" + RESET);
        System.out.println(CYAN + "║" + BOLD + " DANH SACH DON HANG" + padRight("", 58) + CYAN + "║" + RESET);
        System.out.println(CYAN + BOLD + "╠════════════════════════════════════════════════════════════════════════════════╣" + RESET);

        if (orders.isEmpty()) {
            System.out.println(CYAN + "║" + RESET + " Khong co don hang nao!                                                    " + CYAN + "║" + RESET);
        } else {
            System.out.println(CYAN + "║" + RESET + String.format(" %-4s │ %-8s │ %-8s │ %-10s │ %-15s │ %-15s │",
                    "ID", "KHACH", "NHA HANG", "TAI XE", "TIEN", "TRANG THAI"));
            System.out.println(CYAN + BOLD + "╠════════════════════════════════════════════════════════════════════════════════╣" + RESET);

            for (Order order : orders) {
                String statusColor = getStatusColor(order.getStatus());
                System.out.println(CYAN + "║" + RESET + String.format(" %-4d │ %-8d │ %-8s │ %-10s │ %13.0f VND │ %-15s │",
                        order.getId(),
                        order.getCustomerId(),
                        "-",
                        order.getDriverId() != null ? order.getDriverId() : "-",
                        order.getTotalPrice(),
                        statusColor + order.getStatus() + RESET));
            }
        }

        System.out.println(CYAN + BOLD + "╚════════════════════════════════════════════════════════════════════════════════╝" + RESET);
        System.out.println();
    }

    /**
     * In thông tin trạng thái đơn hàng
     */
    public void printOrderStatus(Order order) {
        String statusMessage;
        String statusIcon;

        switch (order.getStatus()) {
            case PENDING:
                statusMessage = "Dang cho xac nhan";
                statusIcon = "⏳";
                break;
            case CONFIRMED:
                statusMessage = "Da xac nhan - Dang chu nhan tai xe";
                statusIcon = "✓";
                break;
            case DELIVERING:
                statusMessage = "Dang giao hang";
                statusIcon = "🚗";
                break;
            case DELIVERED:
                statusMessage = "Da giao thanh cong";
                statusIcon = "✓✓";
                break;
            case CANCELLED:
                statusMessage = "Da huy";
                statusIcon = "✗";
                break;
            default:
                statusMessage = "Khong xac dinh";
                statusIcon = "?";
        }

        System.out.println();
        System.out.println(getStatusColor(order.getStatus()) + BOLD + "╔════════════════════════════════════════════════════════════════╗" + RESET);
        System.out.println(getStatusColor(order.getStatus()) + "║" + BOLD + String.format("  %s %-57s" + RESET + getStatusColor(order.getStatus()) + "║" + RESET,
                statusIcon, statusMessage));
        System.out.println(getStatusColor(order.getStatus()) + BOLD + "╚════════════════════════════════════════════════════════════════╝" + RESET);
        System.out.println();
    }

    // =========================================================
    // HELPER METHODS
    // =========================================================

    private String getStatusColor(OrderStatus status) {
        switch (status) {
            case PENDING:
                return YELLOW;
            case CONFIRMED:
                return CYAN;
            case DELIVERING:
                return BLUE;
            case DELIVERED:
                return GREEN;
            case CANCELLED:
                return RED;
            default:
                return RESET;
        }
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 2) + "..";
    }

    private String padRight(String str, int length) {
        if (str.length() >= length) return str;
        StringBuilder sb = new StringBuilder(str);
        while (sb.length() < length) {
            sb.append(" ");
        }
        return sb.toString();
    }

    private static final String BLUE = "\u001B[34m";
}
