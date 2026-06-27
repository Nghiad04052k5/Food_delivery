package com.delivery.controller;

import com.delivery.model.MenuItem;
import com.delivery.model.Restaurant;
import com.delivery.model.RestaurantStatus;
import com.delivery.repository.MenuItemRepository;
import com.delivery.repository.CsvRepository;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * RestaurantRepository nội bộ để đọc/ghi restaurants.csv.
 */
class RestaurantRepository extends CsvRepository<Restaurant> {

    public RestaurantRepository(String filePath) {
        super(filePath);
    }

    @Override
    protected Restaurant parseLine(String line) {
        // Cấu trúc: restaurant_id,name,phone,address,latitude,longitude,status,rating
        String[] parts = line.split(",");
        if (parts.length >= 8) {
            Restaurant r = new Restaurant();
            r.setId(Integer.parseInt(parts[0].trim()));
            r.setName(parts[1].trim());
            r.setPhone(parts[2].trim());
            r.setAddress(parts[3].trim());
            r.setLatitude(Double.parseDouble(parts[4].trim()));
            r.setLongitude(Double.parseDouble(parts[5].trim()));
            r.setStatus(RestaurantStatus.valueOf(parts[6].trim()));
            r.setRating(Double.parseDouble(parts[7].trim()));
            return r;
        }
        return null;
    }

    @Override
    protected String toCsvRow(Restaurant r) {
        return String.format(Locale.US, "%d,%s,%s,%s,%.6f,%.6f,%s,%.1f",
                r.getId(), r.getName(), r.getPhone(), r.getAddress(),
                r.getLatitude(), r.getLongitude(), r.getStatus().name(), r.getRating());
    }

    @Override
    protected String getHeader() {
        return "restaurant_id,name,phone,address,latitude,longitude,status,rating";
    }
}

/**
 * RestaurantController - Sub-Controller xử lý nghiệp vụ Nhà hàng.
 *
 * Nhiệm vụ (Thành viên 3 - Tuấn):
 * - Xem danh sách nhà hàng đang mở cửa (OPEN)
 * - Xem menu (danh sách món ăn) của một nhà hàng theo ID
 * - Cập nhật/trừ tồn kho món ăn (gọi MenuItemRepository.deductStock)
 * - Cập nhật trạng thái mở/đóng cửa của nhà hàng
 */
public class RestaurantController {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;

    public RestaurantController(String restaurantFilePath, String menuItemFilePath) {
        this.restaurantRepository = new RestaurantRepository(restaurantFilePath);
        this.menuItemRepository = new MenuItemRepository(menuItemFilePath);
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
     * Trừ số lượng tồn kho của một món ăn (gọi đoạn code 'ăn điểm' synchronized).
     * @return true nếu trừ kho thành công, false nếu không đủ tồn kho.
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
     * NHIỆM VỤ BỔ SUNG: Chủ động cập nhật số lượng tồn kho (stockQty) cho một món ăn
     */
    public boolean updateStockQty(int menuItemId, int newQty) {
        List<com.delivery.model.MenuItem> items = menuItemRepository.readAll();
        for (com.delivery.model.MenuItem item : items) {
            if (item.getId() == menuItemId) {
                item.setStockQty(newQty);
                menuItemRepository.update(item); // Lưu cập nhật vào file CSV ngầm
                System.out.println("==> [RestaurantController] Cập nhật kho thành công! Món ID: " + menuItemId + " | Số lượng mới: " + newQty);
                return true;
            }
        }
        System.out.println("==> [RestaurantController] Không tìm thấy món ăn ID: " + menuItemId);
        return false;
    }
}
