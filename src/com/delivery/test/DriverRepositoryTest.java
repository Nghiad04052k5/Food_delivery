package com.delivery.test;

import com.delivery.model.Driver;
import com.delivery.model.DriverStatus;
import com.delivery.repository.DriverRepository;

import java.util.ArrayList;
import java.util.List;

public class DriverRepositoryTest {

    public static void main(String[] args) {
        testFindNearestAvailableDriver();
        testMarkBusyWithSyncSuccessAndFailure();
    }

    public static void testFindNearestAvailableDriver() {
        // ID là số nguyên
        Driver driverFar  = new Driver(1, "Tài xế xa",  "01", "a@", "1", 21.05, 105.80, 0, DriverStatus.AVAILABLE, 1);
        Driver driverNear = new Driver(2, "Tài xế gần", "02", "b@", "1", 21.03, 105.84, 0, DriverStatus.AVAILABLE, 1);
        Driver driverBusy = new Driver(3, "Tài xế bận", "03", "c@", "1", 21.02, 105.85, 0, DriverStatus.BUSY,      1);

        // Tạo danh sách dữ liệu giả lập trong bộ nhớ
        List<Driver> mockDatabase = new ArrayList<>();
        mockDatabase.add(driverFar);
        mockDatabase.add(driverNear);
        mockDatabase.add(driverBusy);

        // Anonymous class ghi đè readAll() và update() để không đọc/ghi file thật khi test
        DriverRepository repo = new DriverRepository("") {
            @Override
            public List<Driver> readAll() { return mockDatabase; }
            @Override
            public void update(Driver d) { /* Giả lập, không ghi file thật khi test */ }
        };

        Driver result = repo.findNearestAvailableDriver(21.02, 105.85);
        if (result != null && result.getId() == 2) {
            System.out.println("testFindNearestAvailableDriver PASSED");
        } else {
            System.err.println("testFindNearestAvailableDriver FAILED");
        }
    }

    public static void testMarkBusyWithSyncSuccessAndFailure() {
        Driver driver = new Driver(5, "Tuấn Driver", "090", "tuan@", "1", 21.0, 105.0, 0, DriverStatus.AVAILABLE, 1);
        List<Driver> mockDatabase = new ArrayList<>();
        mockDatabase.add(driver);

        DriverRepository repo = new DriverRepository("") {
            @Override
            public List<Driver> readAll() { return mockDatabase; }
            @Override
            public void update(Driver d) { }
        };

        // Lần đầu tiên gọi gán đơn: Phải thành công và chuyển sang BUSY
        boolean firstCall = repo.markBusyWithSync(5);
        
        // Lần thứ hai gọi gán đơn (giả lập luồng khác nhảy vào cùng lúc): Phải thất bại
        boolean secondCall = repo.markBusyWithSync(5);
        
        if (firstCall && driver.getStatus() == DriverStatus.BUSY && !secondCall) {
            System.out.println("testMarkBusyWithSyncSuccessAndFailure PASSED");
        } else {
            System.err.println("testMarkBusyWithSyncSuccessAndFailure FAILED");
        }
    }
}
