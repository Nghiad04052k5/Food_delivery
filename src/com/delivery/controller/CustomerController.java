package com.delivery.controller;

import com.delivery.model.Customer;
import com.delivery.repository.CsvRepository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * CustomerRepository - nội bộ trong Controller để đọc/ghi customers.csv.
 * Được định nghĩa bên trong package controller để Controller tự quản lý.
 */
class CustomerRepository extends CsvRepository<Customer> {

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

/**
 * CustomerController - Sub-Controller xử lý nghiệp vụ Khách hàng.
 *
 * Nhiệm vụ (Thành viên 3 - Tuấn):
 * - Đăng ký khách hàng mới -> lưu vào customers.csv
 * - Xem thông tin khách hàng theo ID
 * - Liệt kê tất cả khách hàng
 */
public class CustomerController {

    private final CustomerRepository customerRepository;

    public CustomerController(String customerFilePath) {
        this.customerRepository = new CustomerRepository(customerFilePath);
    }

    /**
     * Đăng ký khách hàng mới vào hệ thống.
     * Tự động sinh ID = (max hiện tại + 1) để không bị trùng.
     */
    public Customer registerCustomer(String name, String phone, String address,
                                     double latitude, double longitude,
                                     String email, String password) {
        // Tự sinh ID tự tăng dựa trên danh sách hiện có
        List<Customer> all = customerRepository.readAll();
        int newId = all.stream().mapToInt(c -> c.getId()).max().orElse(0) + 1;

        Customer newCustomer = new Customer(newId, name, phone, address,
                latitude, longitude, email, password);
        customerRepository.save(newCustomer);

        System.out.println("==> [CustomerController] Dang ky thanh cong! ID=" + newId + ", Ten=" + name);
        return newCustomer;
    }

    /**
     * Xem thông tin khách hàng theo ID.
     */
    public Customer getCustomerById(int id) {
        Customer c = customerRepository.findById(id);
        if (c != null) {
            System.out.println("==> [CustomerController] Tim thay: " + c);
        } else {
            System.out.println("==> [CustomerController] Khong tim thay khach hang voi ID=" + id);
        }
        return c;
    }

    /**
     * Liệt kê tất cả khách hàng trong hệ thống.
     */
    public List<Customer> getAllCustomers() {
        List<Customer> all = customerRepository.readAll();
        System.out.println("==> [CustomerController] Tong so khach hang: " + all.size());
        return all;
    }


    /**
     * NHIỆM VỤ BỔ SUNG: Xem lịch sử đặt hàng của một Khách hàng dựa trên ID
     * Hàm này sẽ đọc trực tiếp từ file orders.csv và lọc ra các đơn hàng của khách đó.
     */
    public List<String> getViewPastOrderHistory(int customerId, String orderCsvPath) {
        List<String> history = new ArrayList<>();
        File file = new File(orderCsvPath);
        if (!file.exists()) return history;

        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(file))) {
            String line = br.readLine(); // Bỏ qua dòng tiêu đề (Header)
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                // Cấu trúc orders.csv: order_id,customer_id,restaurant_id,driver_id,total_amount,status,...
                if (parts.length >= 6) {
                    int idCustInOrder = Integer.parseInt(parts[1].trim());
                    if (idCustInOrder == customerId) {
                        history.add(String.format("Đơn hàng ID: %s | Tổng tiền: %s VNĐ | Trạng thái: %s", 
                                parts[0], parts[4], parts[5]));
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Lỗi khi đọc lịch sử đơn hàng: " + e.getMessage());
        }
        return history;
    }
}
