package com.delivery.controller;

import com.delivery.model.Driver;
import com.delivery.model.DriverStatus;
import com.delivery.repository.DriverRepository;

import java.util.List;

/**
 * DriverController - Sub-Controller xử lý nghiệp vụ Tài xế.
 *
 * Nhiệm vụ (Thành viên 3 - Tuấn):
 * - Đổi trạng thái tài xế ONLINE (AVAILABLE) / OFFLINE
 * - Tìm tài xế gần nhất (gọi findNearestAvailableDriver từ DriverRepository)
 * - Xem danh sách tài xế theo trạng thái
 * - Dispatch tài xế thủ công (đánh dấu BUSY)
 */
public class DriverController {

    private final DriverRepository driverRepository;

    public DriverController(String driverFilePath) {
        this.driverRepository = new DriverRepository(driverFilePath);
    }

    /**
     * Đặt tài xế vào trạng thái ONLINE (AVAILABLE) - Sẵn sàng nhận đơn.
     */
    public boolean goOnline(int driverId) {
        Driver driver = driverRepository.findById(driverId);
        if (driver == null) {
            System.out.println("==> [DriverController] Khong tim thay tai xe ID=" + driverId);
            return false;
        }
        driver.setStatus(DriverStatus.AVAILABLE);
        driverRepository.update(driver);
        System.out.println("==> [DriverController] Tai xe ID=" + driverId
                + " (" + driver.getName() + ") da BAT DAU nhan don (AVAILABLE).");
        return true;
    }

    /**
     * Đặt tài xế vào trạng thái OFFLINE - Ngừng nhận đơn.
     */
    public boolean goOffline(int driverId) {
        Driver driver = driverRepository.findById(driverId);
        if (driver == null) {
            System.out.println("==> [DriverController] Khong tim thay tai xe ID=" + driverId);
            return false;
        }
        driver.setStatus(DriverStatus.OFFLINE);
        driverRepository.update(driver);
        System.out.println("==> [DriverController] Tai xe ID=" + driverId
                + " (" + driver.getName() + ") da NGUNG nhan don (OFFLINE).");
        return true;
    }

    /**
     * Tìm tài xế AVAILABLE gần nhà hàng nhất.
     * Gọi trực tiếp findNearestAvailableDriver từ DriverRepository (đoạn code thuật toán Haversine).
     */
    public Driver findNearest(double restaurantLat, double restaurantLon) {
        Driver nearest = driverRepository.findNearestAvailableDriver(restaurantLat, restaurantLon);
        if (nearest != null) {
            System.out.println("==> [DriverController] Tai xe gan nhat: ID=" + nearest.getId()
                    + ", Ten=" + nearest.getName()
                    + ", Vi tri=(" + nearest.getLatitude() + ", " + nearest.getLongitude() + ")");
        } else {
            System.out.println("==> [DriverController] Khong co tai xe AVAILABLE nao trong he thong!");
        }
        return nearest;
    }

    /**
     * Dispatch thủ công: Đánh dấu tài xế BUSY bằng cơ chế synchronized.
     * Gọi markBusyWithSync để tránh race condition khi 2 luồng cùng nhắm vào 1 tài xế.
     */
    public boolean dispatchDriver(int driverId) {
        boolean success = driverRepository.markBusyWithSync(driverId);
        if (success) {
            System.out.println("==> [DriverController] Dispatch thanh cong! Tai xe ID="
                    + driverId + " da chuyen sang BUSY.");
        } else {
            System.out.println("==> [DriverController] Dispatch THAT BAI! Tai xe ID="
                    + driverId + " khong con san sang (da BUSY hoac khong ton tai).");
        }
        return success;
    }

    /**
     * Lấy danh sách tất cả tài xế đang AVAILABLE.
     */
    public List<Driver> getAvailableDrivers() {
        List<Driver> list = driverRepository.filterDriverAvailabilityStrictly();
        System.out.println("==> [DriverController] So tai xe dang AVAILABLE: " + list.size());
        return list;
    }

    /**
     * Xem thông tin một tài xế cụ thể theo ID.
     */
    public Driver getDriverById(int driverId) {
        Driver d = driverRepository.findById(driverId);
        if (d != null) {
            System.out.println("==> [DriverController] Tai xe: " + d);
        } else {
            System.out.println("==> [DriverController] Khong tim thay tai xe ID=" + driverId);
        }
        return d;
    }
}
