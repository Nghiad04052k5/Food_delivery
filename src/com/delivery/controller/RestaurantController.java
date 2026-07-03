package com.delivery.controller;

import com.delivery.model.MenuItem;
import com.delivery.model.Order;
import com.delivery.model.OrderItem;
import com.delivery.model.OrderStatus;
import com.delivery.model.PaymentMethod;
import com.delivery.model.Restaurant;
import com.delivery.model.RestaurantStatus;
import com.delivery.repository.MenuItemRepository;
import com.delivery.repository.OrderItemRepository;
import com.delivery.repository.OrderRepository;
import com.delivery.repository.RestaurantRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RestaurantController - Sub-Controller xử lý nghiệp vụ Nhà hàng.
 *
 * Nhiệm vụ (Thành viên 3 - Tuấn):
 * - Xem danh sách nhà hàng đang mở cửa (OPEN), tìm kiếm theo tên
 * - Xem menu (danh sách món ăn) của một nhà hàng theo ID
 * - Thêm món mới vào menu nhà hàng
 * - Cập nhật giá/tồn kho món ăn
 * - Trừ tồn kho an toàn (synchronized) khi đặt hàng
 * - Cập nhật trạng thái mở/đóng cửa của nhà hàng
 * - Lấy danh sách đơn hàng của nhà hàng theo trạng thái
 * - Đặt hàng (tạo Order + OrderItem) (Chưa dùng trong tuần này)
 */
public class RestaurantController {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public RestaurantController(RestaurantRepository restaurantRepository,
                                MenuItemRepository menuItemRepository,
                                OrderRepository orderRepository,
                                OrderItemRepository orderItemRepository) {
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository   = menuItemRepository;
        this.orderRepository      = orderRepository;
        this.orderItemRepository  = orderItemRepository;
    }

    // Constructor cũ (tương thích ngược, không có Order)
    public RestaurantController(String restaurantFilePath, String menuItemFilePath) {
        this.restaurantRepository = new RestaurantRepository(restaurantFilePath);
        this.menuItemRepository   = new MenuItemRepository(menuItemFilePath);
        this.orderRepository      = null;
        this.orderItemRepository  = null;
    }

    /**
     * Lấy một nhà hàng theo ID.
     */
    public Restaurant getRestaurantById(int restaurantId) {
        return restaurantRepository.findById(restaurantId);
    }

    /**
     * Lấy danh sách tất cả nhà hàng đang mở cửa (status = OPEN).
     */
    public List<Restaurant> getOpenRestaurants() {
        List<Restaurant> openList = restaurantRepository.readAll()
                .stream()
                .filter(r -> r.getStatus() == RestaurantStatus.OPEN)
                .collect(Collectors.toList());
        System.out.println("==> [RestaurantController] Nha hang dang mo cua: " + openList.size() + " quan");
        return openList;
    }

    /**
     * Lấy danh sách nhà hàng đang mở, có thể lọc theo tên (keyword).
     * @param keyword Từ khoá tìm kiếm tên nhà hàng; truyền "" để xem tất cả
     */
    public List<Restaurant> searchOpenRestaurants(String keyword) {
        final String kw = (keyword == null) ? "" : keyword.toLowerCase();
        return restaurantRepository.readAll().stream()
                .filter(r -> r.getStatus() == RestaurantStatus.OPEN)
                .filter(r -> kw.isEmpty() || r.getName().toLowerCase().contains(kw))
                .collect(Collectors.toList());
    }

    /**
     * Xem danh sách món ăn của một nhà hàng theo restaurant ID.
     */
    public List<MenuItem> getMenuByRestaurant(int restaurantId) {
        List<MenuItem> menu = menuItemRepository.readAll()
                .stream()
                .filter(item -> item.getRestaurantId() == restaurantId)
                .collect(Collectors.toList());
        System.out.println("==> [RestaurantController] Nha hang ID=" + restaurantId
                + " co " + menu.size() + " mon an trong menu.");
        return menu;
    }

    /**
     * Lấy thông tin một MenuItem theo ID.
     */
    public MenuItem getMenuItemById(int menuItemId) {
        return menuItemRepository.findById(menuItemId);
    }

    /**
     * Thêm món ăn mới vào menu của nhà hàng.
     * Tự động sinh ID = max hiện tại + 1.
     */
    public MenuItem addMenuItem(int restaurantId, String itemName, double price, int stockQty) {
        int newId = menuItemRepository.readAll().stream()
                .mapToInt(m -> m.getId()).max().orElse(0) + 1;
        MenuItem newItem = new MenuItem(newId, restaurantId, itemName, price, stockQty, 0);
        menuItemRepository.save(newItem);
        System.out.println("==> [RestaurantController] Them mon moi thanh cong! MonID=" + newId + ", Ten=" + itemName);
        return newItem;
    }

    /**
     * Cập nhật giá và/hoặc tồn kho của một món ăn.
     * Chỉ cập nhật trường nào khác null.
     */
    public boolean updateMenuItem(int menuItemId, Double newPrice, Integer newStockQty) {
        MenuItem item = menuItemRepository.findById(menuItemId);
        if (item == null) {
            System.out.println("==> [RestaurantController] Khong tim thay mon an ID=" + menuItemId);
            return false;
        }
        if (newPrice != null) item.setPrice(newPrice);
        if (newStockQty != null) item.setStockQty(newStockQty);
        menuItemRepository.update(item);
        System.out.println("==> [RestaurantController] Cap nhat mon an ID=" + menuItemId + " thanh cong.");
        return true;
    }

    /**
     * Trừ số lượng tồn kho của một món ăn (gọi đoạn code 'ăn điểm' synchronized).
     * @return true nếu trừ kho thành công, false nếu không đủ tồn kho
     */
    public boolean deductMenuItemStock(int menuItemId, int quantity) {
        boolean result = menuItemRepository.validateAndDeductStockAtomically(menuItemId, quantity);
        if (result) {
            System.out.println("==> [RestaurantController] Tru kho thanh cong: MonID="
                    + menuItemId + ", SoLuong=" + quantity);
        } else {
            System.out.println("==> [RestaurantController] THAT BAI: Khong du ton kho MonID=" + menuItemId);
        }
        return result;
    }

    /**
     * Cập nhật trạng thái mở/đóng cửa của nhà hàng.
     * Nhận thẳng đối tượng Restaurant để View không cần findById thêm lần nữa.
     */
    public void toggleRestaurantStatus(Restaurant restaurant) {
        if (restaurant.getStatus() == RestaurantStatus.OPEN) {
            restaurant.setStatus(RestaurantStatus.CLOSED);
        } else {
            restaurant.setStatus(RestaurantStatus.OPEN);
        }
        restaurantRepository.update(restaurant);
        System.out.println("==> [RestaurantController] NhaHang ID=" + restaurant.getId()
                + " doi trang thai -> " + restaurant.getStatus());
    }

    /**
     * Cập nhật trạng thái nhà hàng theo ID (phiên bản dùng ID thay vì đối tượng).
     */
    public boolean updateRestaurantStatus(int restaurantId, RestaurantStatus newStatus) {
        Restaurant r = restaurantRepository.findById(restaurantId);
        if (r == null) {
            System.out.println("==> [RestaurantController] Khong tim thay nha hang ID=" + restaurantId);
            return false;
        }
        r.setStatus(newStatus);
        restaurantRepository.update(r);
        System.out.println("==> [RestaurantController] Cap nhat trang thai NhaHang ID="
                + restaurantId + " -> " + newStatus.name());
        return true;
    }

    /**
     * Lấy danh sách đơn hàng của một nhà hàng theo trạng thái.
     * Dùng orderItemRepository để tra cứu đơn nào thuộc nhà hàng nào.
     */
    public List<Order> getOrdersByRestaurantAndStatus(int restaurantId, OrderStatus status) {
        if (orderRepository == null || orderItemRepository == null) return java.util.Collections.emptyList();
        return orderRepository.readAll().stream()
                .filter(o -> o.getStatus() == status && isOrderForRestaurant(o, restaurantId))
                .collect(Collectors.toList());
    }

    /**
     * Lấy đơn hàng bất kể trạng thái nào cho một nhà hàng (nhiều trạng thái).
     */
    public List<Order> getOrdersByRestaurantAndStatuses(int restaurantId, OrderStatus... statuses) {
        if (orderRepository == null || orderItemRepository == null) return java.util.Collections.emptyList();
        return orderRepository.readAll().stream()
                .filter(o -> {
                    for (OrderStatus s : statuses) if (o.getStatus() == s) return true;
                    return false;
                })
                .filter(o -> isOrderForRestaurant(o, restaurantId))
                .collect(Collectors.toList());
    }

    /**
     * Xác nhận hoặc từ chối một đơn hàng PENDING.
     * @param accept true = xác nhận (CONFIRMED), false = từ chối (CANCELLED)
     */
    public boolean processOrder(int orderId, boolean accept) {
        if (orderRepository == null) return false;
        Order order = orderRepository.findById(orderId);
        if (order == null || order.getStatus() != OrderStatus.PENDING) return false;
        order.setStatus(accept ? OrderStatus.CONFIRMED : OrderStatus.CANCELLED);
        orderRepository.update(order);
        System.out.println("==> [RestaurantController] Don hang #" + orderId
                + " -> " + order.getStatus());
        return true;
    }

    /**
     * NHIỆM VỤ BỔ SUNG (Chưa dùng trong tuần này):
     * Chủ động cập nhật số lượng tồn kho (stockQty) cho một món ăn.
     */
    public boolean updateStockQty(int menuItemId, int newQty) {
        return updateMenuItem(menuItemId, null, newQty);
    }

    // =========================================================
    // HELPER PRIVATE
    // =========================================================
    private boolean isOrderForRestaurant(Order order, int restaurantId) {
        if (orderItemRepository == null) return false;
        return orderItemRepository.readAll().stream()
                .filter(oi -> oi.getOrderId() == order.getId())
                .findFirst()
                .map(oi -> {
                    MenuItem mi = menuItemRepository.findById(oi.getMenuItemId());
                    return mi != null && mi.getRestaurantId() == restaurantId;
                })
                .orElse(false);
    }
}
