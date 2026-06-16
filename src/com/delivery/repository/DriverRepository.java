package com.delivery.repository;

import com.delivery.model.Driver;
import com.delivery.model.DriverStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DriverRepository extends CsvRepository<Driver> {

    public DriverRepository(String filePath) {
        super(filePath);
    }

    // =========================================================
    // CÁC HÀM ABSTRACT BẮT BUỘC IMPLEMENT TỪ CsvRepository
    // Cấu trúc file: driver_id,name,phone,email,password,latitude,longitude,collected_qr_money,status,version
    // =========================================================

    @Override
    protected Driver parseLine(String line) {
        String[] parts = line.split(",");
        if (parts.length >= 10) {
            Driver driver = new Driver();
            driver.setId(Integer.parseInt(parts[0].trim()));
            driver.setName(parts[1].trim());
            driver.setPhone(parts[2].trim());
            driver.setEmail(parts[3].trim());
            driver.setPassword(parts[4].trim());
            driver.setLatitude(Double.parseDouble(parts[5].trim()));
            driver.setLongitude(Double.parseDouble(parts[6].trim()));
            driver.setCollectedQrMoney(Double.parseDouble(parts[7].trim()));
            driver.setStatus(DriverStatus.valueOf(parts[8].trim()));
            driver.setVersion(Integer.parseInt(parts[9].trim()));
            return driver;
        }
        return null;
    }

    @Override
    protected String toCsvRow(Driver driver) {
        return String.format(Locale.US, "%d,%s,%s,%s,%s,%.6f,%.6f,%.2f,%s,%d",
                driver.getId(),
                driver.getName(),
                driver.getPhone(),
                driver.getEmail(),
                driver.getPassword(),
                driver.getLatitude(),
                driver.getLongitude(),
                driver.getCollectedQrMoney(),
                driver.getStatus().name(),
                driver.getVersion()
        );
    }

    @Override
    protected String getHeader() {
        return "driver_id,name,phone,email,password,latitude,longitude,collected_qr_money,status,version";
    }

    // =========================================================
    // CÁC HÀM NGHIỆP VỤ CỦA TUẤN
    // =========================================================

    /**
     * CHỨC NĂNG 1: Bộ lọc nghiêm ngặt trạng thái Tài xế (Chỉ lấy người đang AVAILABLE)
     */
    public List<Driver> filterDriverAvailabilityStrictly() {
        List<Driver> allDrivers = findAll();
        List<Driver> availableDrivers = new ArrayList<>();

        for (Driver driver : allDrivers) {
            if (driver.getStatus() == DriverStatus.AVAILABLE) {
                availableDrivers.add(driver);
            }
        }
        return availableDrivers;
    }

    /**
     * CHỨC NĂNG 2: Vòng lặp quét tìm tài xế rảnh rỗi ở GẦN NHÀ HÀNG NHẤT
     */
    public Driver findNearestAvailableDriver(double restaurantLat, double restaurantLon) {
        List<Driver> availableDrivers = filterDriverAvailabilityStrictly();

        if (availableDrivers.isEmpty()) {
            return null;
        }

        Driver nearestDriver = null;
        double minDistance = Double.MAX_VALUE;

        for (Driver driver : availableDrivers) {
            double distance = GeoUtils.calculateDistance(
                    driver.getLatitude(), driver.getLongitude(),
                    restaurantLat, restaurantLon
            );

            if (distance < minDistance) {
                minDistance = distance;
                nearestDriver = driver;
            }
        }
        return nearestDriver;
    }

    /**
     * CHỨC NĂNG 3: Chuyển trạng thái tài xế sang bận một cách đồng bộ
     * Từ khóa 'synchronized' giúp chặn đứng tình trạng 2 luồng đơn hàng gán cho cùng 1 tài xế
     */
    public synchronized boolean markBusyWithSync(int driverId) {
        List<Driver> allDrivers = findAll();
        for (Driver driver : allDrivers) {
            if (driver.getId() == driverId) {
                // Kiểm tra lại một lần nữa xem tài xế có thực sự còn rảnh không (Double-check)
                if (driver.getStatus() == DriverStatus.AVAILABLE) {
                    driver.setStatus(DriverStatus.BUSY); // Đánh dấu bận ngay lập tức
                    update(driver); // Lưu thay đổi xuống file thông qua lớp Repository cha
                    return true; // Gán tài xế thành công
                }
                return false; // Tài xế đã bị luồng khác giật mất trước đó vài mili giây
            }
        }
        return false; // Không tìm thấy ID tài xế
    }
}