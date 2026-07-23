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
    // Cấu trúc file:
    // driver_id,name,phone,email,password,latitude,longitude,collected_qr_money,status,version
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
                driver.getVersion());
    }

    @Override
    protected String getHeader() {
        return "driver_id,name,phone,email,password,latitude,longitude,collected_qr_money,status,version";
    }

    // =========================================================
    // CÁC HÀM NGHIỆP VỤ CỦA TUẤN
    // =========================================================

    /**
     * CHỨC NĂNG 1: Bộ lọc nghiêm ngặt trạng thái Tài xế (Chỉ lấy người đang
     * AVAILABLE)
     */
    public List<Driver> filterDriverAvailabilityStrictly() {
        List<Driver> allDrivers = readAll();
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
                    restaurantLat, restaurantLon);

            if (distance < minDistance) {
                minDistance = distance;
                nearestDriver = driver;
            }
        }
        return nearestDriver;
    }

    /**
     * CHỨC NĂNG 2.5: Tìm tài xế rảnh rỗi ở GẦN NHÀ HÀNG NHẤT nhưng loại trừ danh sách ID đã biết
     */
    public Driver findNearestAvailableDriverExcluding(double restaurantLat, double restaurantLon, java.util.Set<Integer> excludedIds) {
        List<Driver> availableDrivers = filterDriverAvailabilityStrictly();

        if (availableDrivers.isEmpty()) {
            return null;
        }

        Driver nearestDriver = null;
        double minDistance = Double.MAX_VALUE;

        for (Driver driver : availableDrivers) {
            if (excludedIds != null && excludedIds.contains(driver.getId())) {
                continue; // Bỏ qua tài xế này vì họ đã từ chối đơn
            }

            double distance = GeoUtils.calculateDistance(
                    driver.getLatitude(), driver.getLongitude(),
                    restaurantLat, restaurantLon);

            if (distance < minDistance) {
                minDistance = distance;
                nearestDriver = driver;
            }
        }
        return nearestDriver;
    }

    public boolean validateAndSetStatusAtomically(int driverId, DriverStatus expectedStatus, DriverStatus newStatus) {
        LockMechanism mechanism = LockConfig.getMechanism();

        if (mechanism == LockMechanism.NO_LOCK) {
            return updateStatusWithoutLock(driverId, expectedStatus, newStatus);
        } else if (mechanism == LockMechanism.SYNCHRONIZED) {
            synchronized (LockManager.getLock("Driver_" + driverId)) {
                return updateStatusWithoutLock(driverId, expectedStatus, newStatus);
            }
        } else if (mechanism == LockMechanism.FILE_LOCK) {
            boolean[] result = new boolean[1];
            LockManager.executeWithFileLock(this.filePath, () -> {
                result[0] = updateStatusWithoutLock(driverId, expectedStatus, newStatus);
            });
            return result[0];
        } else if (mechanism == LockMechanism.OPTIMISTIC) {
            return updateStatusOptimistic(driverId, expectedStatus, newStatus);
        }
        return false;
    }

    private boolean updateStatusWithoutLock(int driverId, DriverStatus expectedStatus, DriverStatus newStatus) {
        List<Driver> allDrivers = readAll();
        Driver targetDriver = null;
        for (Driver d : allDrivers) {
            if (d.getId() == driverId) {
                targetDriver = d;
                break;
            }
        }

        if (targetDriver == null || targetDriver.getStatus() != expectedStatus) return false;

        try { Thread.sleep(5); } catch (InterruptedException ignored) {}

        targetDriver.setStatus(newStatus);
        saveAll(allDrivers);
        return true;
    }

    private boolean updateStatusOptimistic(int driverId, DriverStatus expectedStatus, DriverStatus newStatus) {
        Driver initialDriver = findById(driverId);
        if (initialDriver == null || initialDriver.getStatus() != expectedStatus) return false;
        
        int expectedVersion = initialDriver.getVersion();
        
        try { Thread.sleep(5); } catch (InterruptedException ignored) {}

        boolean[] result = new boolean[1];
        synchronized (LockManager.getLock("Driver_DB_" + driverId)) {
            List<Driver> allDrivers = readAll();
            Driver targetDriver = null;
            for (Driver d : allDrivers) {
                if (d.getId() == driverId) {
                    targetDriver = d;
                    break;
                }
            }
            
            if (targetDriver != null) {
                if (targetDriver.getVersion() != expectedVersion) {
                    throw new OptimisticLockException("Version mismatch for Driver " + driverId);
                }
                if (targetDriver.getStatus() == expectedStatus) {
                    targetDriver.setStatus(newStatus);
                    targetDriver.setVersion(expectedVersion + 1);
                    saveAll(allDrivers);
                    result[0] = true;
                }
            }
        }
        return result[0];
    }
}
