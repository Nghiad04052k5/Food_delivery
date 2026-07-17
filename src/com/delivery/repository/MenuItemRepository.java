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

    public boolean validateAndDeductStockAtomically(int itemId, int quantity) {
        LockMechanism mechanism = LockConfig.getMechanism();

        if (mechanism == LockMechanism.NO_LOCK) {
            return deductWithoutLock(itemId, quantity);
        } else if (mechanism == LockMechanism.SYNCHRONIZED) {
            synchronized (LockManager.getLock("MenuItem_" + itemId)) {
                return deductWithoutLock(itemId, quantity);
            }
        } else if (mechanism == LockMechanism.FILE_LOCK) {
            boolean[] result = new boolean[1];
            LockManager.executeWithFileLock(this.filePath, () -> {
                result[0] = deductWithoutLock(itemId, quantity);
            });
            return result[0];
        } else if (mechanism == LockMechanism.OPTIMISTIC) {
            return deductOptimistic(itemId, quantity);
        }
        return false;
    }

    private boolean deductWithoutLock(int itemId, int quantity) {
        List<MenuItem> allItems = readAll();
        MenuItem targetItem = null;
        for (MenuItem item : allItems) {
            if (item.getId() == itemId) {
                targetItem = item;
                break;
            }
        }

        if (targetItem == null) return false;

        // Giả lập trễ để dễ tạo Race Condition
        try { Thread.sleep(5); } catch (InterruptedException ignored) {}

        if (targetItem.getStockQty() >= quantity) {
            targetItem.setStockQty(targetItem.getStockQty() - quantity);
            saveAll(allItems);
            return true;
        }
        return false;
    }

    private boolean deductOptimistic(int itemId, int quantity) {
        // 1. Read (không lock)
        MenuItem initialItem = findById(itemId);
        if (initialItem == null || initialItem.getStockQty() < quantity) {
            return false;
        }
        int expectedVersion = initialItem.getVersion();

        // Giả lập trễ để thread khác có cơ hội ghi
        try { Thread.sleep(5); } catch (InterruptedException ignored) {}

        // 2. Atomic check-and-update (giả lập thao tác UPDATE WHERE version = ? của DB)
        boolean[] result = new boolean[1];
        synchronized (LockManager.getLock("MenuItem_DB_" + itemId)) {
            List<MenuItem> allItems = readAll();
            MenuItem targetItem = null;
            for (MenuItem item : allItems) {
                if (item.getId() == itemId) {
                    targetItem = item;
                    break;
                }
            }

            if (targetItem != null) {
                if (targetItem.getVersion() != expectedVersion) {
                    throw new OptimisticLockException("Version mismatch for MenuItem " + itemId);
                }

                if (targetItem.getStockQty() >= quantity) {
                    targetItem.setStockQty(targetItem.getStockQty() - quantity);
                    targetItem.setVersion(expectedVersion + 1);
                    saveAll(allItems);
                    result[0] = true;
                }
            }
        }
        return result[0];
    }
}
