package com.delivery.test;

import com.delivery.repository.GeoUtils;

public class GeoUtilsTest {

    public static void main(String[] args) {
        testDistanceHanoiToSaigon();
    }

    public static void testDistanceHanoiToSaigon() {
        // Tọa độ thực tế của Hà Nội
        double hanoiLat = 21.0285;
        double hanoiLon = 105.8542;

        // Tọa độ thực tế của TP. Hồ Chí Minh
        double hcmLat = 10.8231;
        double hcmLon = 106.6297;

        // Chạy hàm tính khoảng cách của bạn
        double actualDistance = GeoUtils.calculateDistance(hanoiLat, hanoiLon, hcmLat, hcmLon);

        System.out.println("Khoảng cách thực tế tính được: " + actualDistance + " km");

        // Khoảng cách đường chim bay HN-HCM tính bằng Haversine ~ 1140km
        // Dùng delta lệch cho phép là 50km vì sai số tùy vị trí quận/huyện lấy mốc.
        if (Math.abs(actualDistance - 1140.0) <= 50.0) {
            System.out.println("Test PASSED");
        } else {
            System.err.println("Thuật toán Haversine tính khoảng cách bị sai lệch quá lớn!");
        }
    }
}