package com.delivery.test;

import com.delivery.model.MenuItem;
import com.delivery.repository.MenuItemRepository;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MenuItemRepositoryTest {

    private static final String TEST_CSV_FILE = "test_menu_items.csv";

    public static void main(String[] args) {
        System.out.println("=== BẮT ĐẦU TEST LỚP GENERIC & ĐỒNG BỘ (SYNCHRONIZED) ===");
        
        // 1. Chuẩn bị dữ liệu mẫu (Tạo file test_menu_items.csv)
        prepareTestData();
        
        MenuItemRepository repo = new MenuItemRepository(TEST_CSV_FILE);
        
        // Đọc thử xem Generic parseLine hoạt động không
        MenuItem item = repo.findById(1);
        if (item != null) {
            System.out.println("Đọc file thành công (Generic hoạt động tốt): " + item.getItemName() + " | Tồn kho ban đầu: " + item.getStockQty());
        } else {
            System.out.println("Lỗi: Không đọc được dữ liệu.");
            return;
        }

        // 2. Test đa luồng (Đoạn mã "ăn điểm")
        System.out.println("\n--- Bắt đầu Test Đa Luồng (Ép hệ thống trừ tồn kho đồng thời) ---");
        // Số lượng tồn kho ban đầu là 10. Ta cho 15 luồng, mỗi luồng mua 1 sản phẩm.
        // Kỳ vọng: 10 luồng đầu mua thành công, 5 luồng sau báo lỗi không đủ kho, Tồn kho cuối cùng = 0 (Không bị số âm)
        
        int numberOfThreads = 15;
        ExecutorService executorService = Executors.newFixedThreadPool(10); // Pool 10 luồng
        
        for (int i = 1; i <= numberOfThreads; i++) {
            executorService.execute(() -> {
                // Các luồng tranh nhau trừ món ăn số 1, mỗi luồng trừ 1 cái
                repo.validateAndDeductStockAtomically(1, 1);
            });
        }
        
        executorService.shutdown();
        try {
            // Chờ các luồng chạy xong
            executorService.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // 3. Kiểm tra lại kết quả cuối cùng
        MenuItem finalItem = repo.findById(1);
        System.out.println("\n--- KẾT QUẢ SAU KHI TEST ---");
        System.out.println("Tồn kho thực tế còn lại trong file CSV: " + finalItem.getStockQty());
        if (finalItem.getStockQty() == 0) {
            System.out.println("=> THÀNH CÔNG: Tồn kho không bị âm. Thuật toán Lock (Synchronized) hoạt động chính xác tuyệt đối!");
        } else {
            System.out.println("=> THẤT BẠI: Thuật toán Lock có lỗi.");
        }
    }

    private static void prepareTestData() {
        try {
            File file = new File(TEST_CSV_FILE);
            if (!file.exists()) {
                file.createNewFile();
            }
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                bw.write("menu_item_id,restaurant_id,item_name,price,stock_qty,version");
                bw.newLine();
                bw.write("1,1,Pho Bo Khong Hanh,50000.0,10,0"); // Tồn kho là 10
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Lỗi khi tạo dữ liệu mẫu: " + e.getMessage());
        }
    }
}
