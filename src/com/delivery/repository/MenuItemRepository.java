package com.delivery.repository;

import com.delivery.model.MenuItem;

import java.util.List;
import java.util.Locale;

public class MenuItemRepository extends CsvRepository<MenuItem> {

    public MenuItemRepository(String filePath) {
        super(filePath);
    }

    @Override
    protected MenuItem parseLine(String line) {
        // Cấu trúc file: menu_item_id,restaurant_id,item_name,price,stock_qty,version
        String[] parts = line.split(",");
        if (parts.length >= 6) {
            MenuItem item = new MenuItem();
            item.setId(Integer.parseInt(parts[0].trim()));
            item.setRestaurantId(Integer.parseInt(parts[1].trim()));
            item.setItemName(parts[2].trim());
            item.setPrice(Double.parseDouble(parts[3].trim()));
            item.setStockQty(Integer.parseInt(parts[4].trim()));
            item.setVersion(Integer.parseInt(parts[5].trim()));
            return item;
        }
        return null;
    }

    @Override
    protected String toCsvRow(MenuItem entity) {
        // Đảm bảo định dạng chuẩn kiểu US để xuất hiện dấu '.' thay vì phẩy khi in số thập phân
        return String.format(Locale.US, "%d,%d,%s,%.1f,%d,%d",
                entity.getId(),
                entity.getRestaurantId(),
                entity.getItemName(),
                entity.getPrice(),
                entity.getStockQty(),
                entity.getVersion()
        );
    }

    @Override
    protected String getHeader() {
        return "menu_item_id,restaurant_id,item_name,price,stock_qty,version";
    }

    // ĐOẠN CODE "ĂN ĐIỂM": Thuật toán Lock (Synchronized) trừ số lượng kho
    // Synchronized đảm bảo chỉ 1 thread được chạy qua hàm này tại 1 thời điểm trên cùng 1 instance Repository
    public synchronized boolean deductStock(int menuItemId, int quantity) {
        List<MenuItem> allItems = findAll();
        MenuItem targetItem = null;

        for (MenuItem item : allItems) {
            if (item.getId() == menuItemId) {
                targetItem = item;
                break;
            }
        }

        if (targetItem == null) {
            System.out.println("Thread [" + Thread.currentThread().getName() + "]: Không tìm thấy món ăn với ID: " + menuItemId);
            return false;
        }

        if (targetItem.getStockQty() >= quantity) {
            // Đủ số lượng trong kho -> Trừ số lượng
            targetItem.setStockQty(targetItem.getStockQty() - quantity);
            
            // Cập nhật lại list và lưu xuống CSV
            saveAll(allItems);
            System.out.println("Thread [" + Thread.currentThread().getName() + "]: Đã trừ " + quantity + " tồn kho thành công cho món: " + targetItem.getItemName() + ". Còn lại: " + targetItem.getStockQty());
            return true;
        } else {
            // Không đủ kho -> Báo lỗi số âm
            System.out.println("Thread [" + Thread.currentThread().getName() + "]: Thất bại: Không đủ tồn kho cho món " + targetItem.getItemName() + ". Kho hiện tại: " + targetItem.getStockQty() + ", Yêu cầu: " + quantity);
            return false;
        }
    }
}
