package com.delivery.test;

import com.delivery.model.Driver;
import com.delivery.model.DriverStatus;
import com.delivery.repository.DriverRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class DriverRepositoryTest {

    @Test
    public void testFindNearestAvailableDriver() {
        // ID là int theo BaseEntity (1, 2, 3)
        Driver driverFar  = new Driver(1, "Tài xế xa",  "01", "a@", "1", 21.05, 105.80, 0, DriverStatus.AVAILABLE, 1);
        Driver driverNear = new Driver(2, "Tài xế gần", "02", "b@", "1", 21.03, 105.84, 0, DriverStatus.AVAILABLE, 1);
        Driver driverBusy = new Driver(3, "Tài xế bận", "03", "c@", "1", 21.02, 105.85, 0, DriverStatus.BUSY,      1);

        // Tạo danh sách dữ liệu giả lập trong bộ nhớ
        List<Driver> mockDatabase = new ArrayList<>(List.of(driverFar, driverNear, driverBusy));

        // Anonymous class ghi đè findAll() và update() để không đọc/ghi file thật khi test
        DriverRepository repo = new DriverRepository("") {
            @Override
            public List<Driver> findAll() { return mockDatabase; }
            @Override
            public void update(Driver d) { /* Giả lập, không ghi file thật khi test */ }
        };

        Driver result = repo.findNearestAvailableDriver(21.02, 105.85);
        assertNotNull(result, "Phải tìm được tài xế AVAILABLE gần nhất!");
        assertEquals(2, result.getId(), "Phải chọn đúng tài xế rảnh và ở gần nhất (id=2)!");
    }

    @Test
    public void testMarkBusyWithSyncSuccessAndFailure() {
        Driver driver = new Driver(5, "Tuấn Driver", "090", "tuan@", "1", 21.0, 105.0, 0, DriverStatus.AVAILABLE, 1);
        List<Driver> mockDatabase = new ArrayList<>(List.of(driver));

        DriverRepository repo = new DriverRepository("") {
            @Override
            public List<Driver> findAll() { return mockDatabase; }
            @Override
            public void update(Driver d) { }
        };

        // Lần đầu tiên gọi gán đơn: Phải thành công và chuyển sang BUSY
        boolean firstCall = repo.markBusyWithSync(5);
        assertTrue(firstCall, "Lần đầu gán đơn khi tài xế đang AVAILABLE phải thành công!");
        assertEquals(DriverStatus.BUSY, driver.getStatus());

        // Lần thứ hai gọi gán đơn (giả lập luồng khác nhảy vào cùng lúc): Phải thất bại
        boolean secondCall = repo.markBusyWithSync(5);
        assertFalse(secondCall, "Tài xế đã bận (BUSY), hệ thống phải từ chối không cho gán thêm đơn!");
    }
}