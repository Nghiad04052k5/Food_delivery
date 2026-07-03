package com.delivery.repository;

public class GeoUtils {
    // Bán kính Trái Đất tính bằng Kilometer
    private static final double EARTH_RADIUS = 6371.0;

    /**
     * Hàm tính khoảng cách giữa 2 tọa độ dựa trên công thức toán học Haversine
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Chuyển đổi vĩ độ và kinh độ từ độ (Degree) sang Radian
        double latRad1 = Math.toRadians(lat1);
        double latRad2 = Math.toRadians(lat2);

        // Tính độ chênh lệch giữa 2 tọa độ (đơn vị Radian)
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);

        // Áp dụng công thức Haversine toán học
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(latRad1) * Math.cos(latRad2) *
                        Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double straightLineDistance = c * EARTH_RADIUS; // Đây là đường chim bay gốc

        // SỬA TẠI ĐÂY: Nhân thêm hệ số đường bộ trung bình (ví dụ 1.35)
        double roadDistanceFactor = 1.35;
        double finalRoadDistance = straightLineDistance * roadDistanceFactor;

        return finalRoadDistance;
    }
}