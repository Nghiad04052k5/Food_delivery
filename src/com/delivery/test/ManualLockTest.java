package com.delivery.test;

import com.delivery.repository.LockConfig;
import com.delivery.repository.LockMechanism;
import com.delivery.repository.MenuItemRepository;
import com.delivery.repository.OptimisticLockException;
import com.delivery.model.MenuItem;
import java.util.Collections;

public class ManualLockTest {
    public static void main(String[] args) {
        System.out.println("=== BAT DAU TEST THU CONG 10 THREADS ===");
        
        String testFile = "data/menu_items_test.csv";
        MenuItemRepository repo = new MenuItemRepository(testFile);
        
        // Tao 1 mon an mau co stock = 1
        MenuItem item = new MenuItem(1, 1, "Pho Bo", 50000, 1, 0);
        repo.saveAll(Collections.singletonList(item));
        
        LockConfig.setMechanism(LockMechanism.OPTIMISTIC);
        System.out.println("Co che hien tai: " + LockConfig.getMechanism());

        Runnable task = () -> {
            try {
                boolean result = repo.validateAndDeductStockAtomically(1, 1);
                if (result) {
                    System.out.println(Thread.currentThread().getName() + " -> Mua THÀNH CÔNG!");
                } else {
                    System.out.println(Thread.currentThread().getName() + " -> Mua THẤT BẠI (Hết hàng).");
                }
            } catch (OptimisticLockException e) {
                System.out.println(Thread.currentThread().getName() + " -> Exception: Món ăn vừa có người mua hết (" + e.getMessage() + ")");
            } catch (Exception e) {
                System.out.println(Thread.currentThread().getName() + " -> Exception: " + e.getMessage());
            }
        };

        for (int i = 1; i <= 10; i++) {
            new Thread(task, "Thread-" + i).start();
        }
    }
}
