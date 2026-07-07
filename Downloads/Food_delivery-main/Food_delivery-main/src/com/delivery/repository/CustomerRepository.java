package com.delivery.repository;

import com.delivery.model.Customer;

import java.util.Locale;

/**
 * CustomerRepository - để đọc/ghi customers.csv.
 */
public class CustomerRepository extends CsvRepository<Customer> {

    public CustomerRepository(String filePath) {
        super(filePath);
    }

    @Override
    protected Customer parseLine(String line) {
        // Cấu trúc: customer_id,name,phone,address,latitude,longitude,email,password
        String[] parts = line.split(",");
        if (parts.length >= 8) {
            Customer c = new Customer();
            c.setId(Integer.parseInt(parts[0].trim()));
            c.setName(parts[1].trim());
            c.setPhone(parts[2].trim());
            c.setAddress(parts[3].trim());
            c.setLatitude(Double.parseDouble(parts[4].trim()));
            c.setLongitude(Double.parseDouble(parts[5].trim()));
            c.setEmail(parts[6].trim());
            c.setPassword(parts[7].trim());
            return c;
        }
        return null;
    }

    @Override
    protected String toCsvRow(Customer c) {
        return String.format(Locale.US, "%d,%s,%s,%s,%.6f,%.6f,%s,%s",
                c.getId(), c.getName(), c.getPhone(), c.getAddress(),
                c.getLatitude(), c.getLongitude(), c.getEmail(), c.getPassword());
    }

    @Override
    protected String getHeader() {
        return "customer_id,name,phone,address,latitude,longitude,email,password";
    }
}
