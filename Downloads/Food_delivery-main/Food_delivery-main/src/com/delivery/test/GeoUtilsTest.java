package com.delivery.test;

import com.delivery.repository.GeoUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GeoUtilsTest {

    @Test
    public void testDistanceHanoiToSaigon() {
        double hanoiLat = 21.0285;
        double hanoiLon = 105.8542;
        double hcmLat = 10.8231;
        double hcmLon = 106.6297;

        double distance = GeoUtils.calculateDistance(hanoiLat, hanoiLon, hcmLat, hcmLon);

        assertEquals(1700.0, distance, 200.0, "Khoang cach duong bo Ha Noi - Sai Gon phai xap xi 1700 km!");
    }

    /**
     * Test khoảng cách 0 km khi 2 điểm trùng nhau.
     */
    @Test
    public void testDistanceSamePoint() {
        double lat = 10.7769;
        double lon = 106.7009;

        double distance = GeoUtils.calculateDistance(lat, lon, lat, lon);

        assertEquals(0.0, distance, 0.001,
                "Khoang cach cung mot diem phai bang 0!");
    }

    @Test
    public void testDistanceHanoiToDanang() {
        double hanoiLat = 21.0285;
        double hanoiLon = 105.8542;
        double danangLat = 16.0544;
        double danangLon = 108.2022;

        double distance = GeoUtils.calculateDistance(hanoiLat, hanoiLon, danangLat, danangLon);

        assertEquals(763.0, distance, 100.0,
                "Khoang cach Ha Noi - Da Nang phai ~763 km!");
    }
}