package com.delivery.controller;

import com.delivery.model.Driver;
import com.delivery.model.DriverStatus;
import com.delivery.repository.DriverRepository;
import com.delivery.repository.OptimisticLockException;

import java.util.List;

/**
 * DriverController - Sub-Controller xử lý nghiệp vụ Tài xế.
 *
 * Nhiệm vụ (Thành viên 3 - Tuấn):
 * - Đăng ký tài xế mới
 * - Đăng nhập tài xế (kiểm tra email + password đã hash)
 * - Cập nhật thông tin profile tài xế
 * - Đổi trạng thái tài xế ONLINE (AVAILABLE) / OFFLINE
 * - Tìm tài xế gần nhất (gọi findNearestAvailableDriver từ DriverRepository)
 * - Xem danh sách tài xế theo trạng thái
 * - Dispatch tài xế thủ công (đánh dấu BUSY)
 */
public class DriverController {

    private final DriverRepository driverRepository;

    public DriverController(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    // Giữ lại constructor cũ để không phá vỡ code cũ nếu có nơi khác dùng
    public DriverController(String driverFilePath) {
        this.driverRepository = new DriverRepository(driverFilePath);
    }

    /**
     * Đăng ký tài xế mới vào hệ thống.
     * Tự động sinh ID = (max hiện tại + 1) để không bị trùng.
     */
    public Driver registerDriver(String name, String phone, String email,
                                 String hashedPassword, double latitude, double longitude) {
        int newId = driverRepository.readAll().stream()
                .mapToInt(d -> d.getId()).max().orElse(0) + 1;

        Driver newDriver = new Driver();
        newDriver.setId(newId);
        newDriver.setName(name);
        newDriver.setPhone(phone);
        newDriver.setEmail(email);
        newDriver.setPassword(hashedPassword);
        newDriver.setLatitude(latitude);
        newDriver.setLongitude(longitude);
        newDriver.setStatus(DriverStatus.OFFLINE);
        newDriver.setCollectedQrMoney(0);
        newDriver.setVersion(0);

        driverRepository.save(newDriver);
        System.out.println("==> [DriverController] Dang ky tai xe thanh cong! ID=" + newId + ", Ten=" + name);
        return newDriver;
    }

    /**
     * Đăng nhập tài xế: Dò tìm theo email và password đã băm (SHA-256).
     * @param email          Email tài xế
     * @param hashedPassword Mật khẩu đã được băm SHA-256
     * @return Đối tượng Driver nếu khớp, null nếu không tìm thấy
     */
    public Driver login(String email, String hashedPassword) {
        for (Driver d : driverRepository.readAll()) {
            if (d.getEmail().equalsIgnoreCase(email) && d.getPassword().equals(hashedPassword)) {
                System.out.println("==> [DriverController] Dang nhap thanh cong! Ten=" + d.getName());
                return d;
            }
        }
        System.out.println("==> [DriverController] Dang nhap that bai! Email=" + email);
        return null;
    }

    /**
     * Cập nhật thông tin profile của tài xế (tên, SĐT).
     */
    public boolean updateProfile(Driver driver, String newName, String newPhone) {
        boolean changed = false;
        if (newName != null && !newName.isEmpty()) {
            driver.setName(newName);
            changed = true;
        }
        if (newPhone != null && !newPhone.isEmpty()) {
            driver.setPhone(newPhone);
            changed = true;
        }
        if (changed) {
            driverRepository.update(driver);
            System.out.println("==> [DriverController] Cap nhat profile tai xe thanh cong! ID=" + driver.getId());
        }
        return changed;
    }

    /**
     * Chuyển trạng thái tài xế: toggle giữa AVAILABLE và OFFLINE.
     * Nếu đang BUSY thì không cho đổi.
     * Bắt OptimisticLockException nếu có xung đột đa luồng khi ghi trạng thái.
     * @return true nếu đổi thành công, false nếu đang BUSY hoặc lỗi xung đột
     */
    public boolean toggleOnlineStatus(Driver driver) {
        if (driver.getStatus() == DriverStatus.BUSY) {
            System.out.println("==> [DriverController] Tai xe ID=" + driver.getId()
                    + " dang ban giao hang, khong the doi trang thai!");
            return false;
        }
        try {
            if (driver.getStatus() == DriverStatus.AVAILABLE) {
                driver.setStatus(DriverStatus.OFFLINE);
            } else {
                driver.setStatus(DriverStatus.AVAILABLE);
            }
            driverRepository.update(driver);
            System.out.println("==> [DriverController] Tai xe ID=" + driver.getId()
                    + " doi trang thai -> " + driver.getStatus());
            return true;
        } catch (OptimisticLockException e) {
            System.out.println("[!] Xung dot trang thai tai xe ID=" + driver.getId()
                    + ": Trang thai da bi cap nhat boi tien trinh khac!");
            return false;
        }
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
     * Gọi trực tiếp findNearestAvailableDriver từ DriverRepository (thuật toán Haversine).
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
     * Bắt OptimisticLockException nếu có xung đột đa luồng (Driver Overload protection).
     */
    public boolean dispatchDriver(int driverId) {
        try {
            boolean success = driverRepository.markBusyWithSync(driverId);
            if (success) {
                System.out.println("==> [DriverController] Dispatch thanh cong! Tai xe ID="
                        + driverId + " da chuyen sang BUSY.");
            } else {
                System.out.println("[!] Dispatch THAT BAI: Tai xe ID=" + driverId
                        + " khong con san sang (da BUSY hoac khong ton tai). Co the bi luong khac gian truoc!");
            }
            return success;
        } catch (OptimisticLockException e) {
            System.out.println("[!] Xung dot khi phan cong tai xe (Driver Overload): Tai xe ID=" + driverId
                    + " da duoc nhan boi 2 don hang cung luc!");
            return false;
        }
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

    /**
     * Lưu thay đổi của một đối tượng Driver (trạng thái, thông tin) xuống file CSV.
     */
    public void saveDriver(Driver driver) {
        driverRepository.update(driver);
    }
}
