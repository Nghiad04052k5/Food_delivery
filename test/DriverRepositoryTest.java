package com.delivery.test;

import com.delivery.model.Driver;
import com.delivery.model.DriverStatus;
import com.delivery.repository.DriverRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class DriverRepositoryTest {

    private List<Driver> mockDatabase;
    private DriverRepository repo;

    @BeforeEach
    public void setUp() {
        mockDatabase = new ArrayList<>();
        mockDatabase.add(
                new Driver(1, "Tai xe xa", "01", "a@test.com", "pass1", 21.05, 105.80, 0, DriverStatus.AVAILABLE, 1));
        mockDatabase.add(
                new Driver(2, "Tai xe gan", "02", "b@test.com", "pass2", 21.03, 105.84, 0, DriverStatus.AVAILABLE, 1));
        mockDatabase
                .add(new Driver(3, "Tai xe ban", "03", "c@test.com", "pass3", 21.02, 105.85, 0, DriverStatus.BUSY, 1));
        mockDatabase.add(new Driver(4, "Tai xe ngoai tuyen", "04", "d@test.com", "pass4", 21.01, 105.83, 0,
                DriverStatus.OFFLINE, 1));

        repo = new DriverRepository("") {
            @Override
            public List<Driver> readAll() {
                return mockDatabase;
            }

            @Override
            public void update(Driver d) {
                for (int i = 0; i < mockDatabase.size(); i++) {
                    if (mockDatabase.get(i).getId() == d.getId()) {
                        mockDatabase.set(i, d);
                        break;
                    }
                }
            }
        };
    }

    @Test
    public void testFilterDriverAvailabilityStrictly() {
        long startTime = System.nanoTime();
        List<Driver> available = repo.filterDriverAvailabilityStrictly();
        long endTime = System.nanoTime();

        long durationNs = endTime - startTime;
        double durationMs = durationNs / 1_000_000.0;
        System.out.println("Thoi gian chay filterDriverAvailabilityStrictly: " + durationMs + " ms");

        assertEquals(2, available.size(), "Phai co dung 2 tai xe AVAILABLE (id=1 va id=2)!");
        assertTrue(available.stream().allMatch(d -> d.getStatus() == DriverStatus.AVAILABLE),
                "Tat ca tai xe trong danh sach phai co trang thai AVAILABLE!");

        assertTrue((endTime - startTime) < 1000000, "Thoi gian chay loc tai xe phai duoi 1 giay!");
    }

    @Test
    public void testFindNearestAvailableDriver() {
        // Nhà hàng tại tọa độ gần tài xế id=2 nhất
        double restaurantLat = 21.028;
        double restaurantLon = 105.85;

        Driver result = repo.findNearestAvailableDriver(restaurantLat, restaurantLon);

        assertNotNull(result, "Phai tim duoc tai xe AVAILABLE gan nhat!");
        assertEquals(2, result.getId(), "Phai chon dung tai xe id=2 (gan nha hang nhat va dang AVAILABLE)!");
    }

    @Test
    public void testFindNearestAvailableDriver_ReturnsNullWhenNoneAvailable() {
        // Đặt tất cả tài xế sang BUSY

        for (Driver d : mockDatabase) {
            d.setStatus(DriverStatus.BUSY);
        }

        Driver result = repo.findNearestAvailableDriver(21.0, 105.8);

        assertNull(result, "Phai tra ve null khi khong con tai xe AVAILABLE nao!");
    }

    @Test
    public void testMarkBusyWithSyncSuccessAndFailure() {
        boolean firstCall = repo.markBusyWithSync(1);
        assertTrue(firstCall, "Lan dau gan don khi tai xe dang AVAILABLE phai thanh cong!");

        // Kiểm tra trạng thái đã đổi sang BUSY
        Driver updatedDriver = mockDatabase.stream().filter(d -> d.getId() == 1).findFirst().orElse(null);
        assertNotNull(updatedDriver);
        assertEquals(DriverStatus.BUSY, updatedDriver.getStatus(),
                "Sau khi gan, trang thai tai xe phai chuyen sang BUSY!");

        boolean secondCall = repo.markBusyWithSync(1);
        assertFalse(secondCall, "Tai xe da BUSY, he thong phai tu choi gan them don!");
    }

    @Test
    public void testMarkBusyWithSync_IdNotFound() {
        boolean result = repo.markBusyWithSync(999);

        assertFalse(result, "ID tai xe khong ton tai phai tra ve false!");
    }
}