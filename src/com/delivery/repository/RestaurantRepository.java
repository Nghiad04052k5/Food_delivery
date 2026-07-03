package com.delivery.repository;

import com.delivery.model.Restaurant;
import com.delivery.model.RestaurantStatus;

import java.util.Locale;

/**
 * RestaurantRepository để đọc/ghi restaurants.csv.
 */
public class RestaurantRepository extends CsvRepository<Restaurant> {

    public RestaurantRepository(String filePath) {
        super(filePath);
    }

    @Override
    protected Restaurant parseLine(String line) {
        // Cấu trúc: restaurant_id,name,phone,address,latitude,longitude,status,rating
        String[] parts = line.split(",");
        if (parts.length >= 8) {
            Restaurant r = new Restaurant();
            r.setId(Integer.parseInt(parts[0].trim()));
            r.setName(parts[1].trim());
            r.setPhone(parts[2].trim());
            r.setAddress(parts[3].trim());
            r.setLatitude(Double.parseDouble(parts[4].trim()));
            r.setLongitude(Double.parseDouble(parts[5].trim()));
            r.setStatus(RestaurantStatus.valueOf(parts[6].trim()));
            r.setRating(Double.parseDouble(parts[7].trim()));
            return r;
        }
        return null;
    }

    @Override
    protected String toCsvRow(Restaurant r) {
        return String.format(Locale.US, "%d,%s,%s,%s,%.6f,%.6f,%s,%.1f",
                r.getId(), r.getName(), r.getPhone(), r.getAddress(),
                r.getLatitude(), r.getLongitude(), r.getStatus().name(), r.getRating());
    }

    @Override
    protected String getHeader() {
        return "restaurant_id,name,phone,address,latitude,longitude,status,rating";
    }
}
