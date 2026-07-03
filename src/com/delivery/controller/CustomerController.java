package com.delivery.controller;

import com.delivery.model.Customer;
import com.delivery.repository.CustomerRepository;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * CustomerController - Sub-Controller xử lý nghiệp vụ Khách hàng.
 *
 * Nhiệm vụ (Thành viên 3 - Tuấn):
 * - Đăng ký khách hàng mới -> lưu vào customers.csv
 * - Đăng nhập khách hàng (kiểm tra email + password đã hash)
 * - Cập nhật thông tin Profile khách hàng
 * - Xem thông tin khách hàng theo ID
 * - Liệt kê tất cả khách hàng
 * - Xem lịch sử đơn hàng (Nhiệm vụ bổ sung - chưa dùng trong tuần này)
 */
public class CustomerController {

    private final CustomerRepository customerRepository;

    public CustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    // Giữ lại constructor cũ để không phá vỡ code cũ nếu có nơi khác dùng
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
        List<Customer> all = customerRepository.readAll();
        int newId = all.stream().mapToInt(c -> c.getId()).max().orElse(0) + 1;

        Customer newCustomer = new Customer(newId, name, phone, address,
                latitude, longitude, email, password);
        customerRepository.save(newCustomer);

        System.out.println("==> [CustomerController] Dang ky thanh cong! ID=" + newId + ", Ten=" + name);
        return newCustomer;
    }

    /**
     * Đăng nhập khách hàng: Dò tìm theo email và password đã băm (SHA-256).
     * @param email    Email khách hàng
     * @param hashedPassword Mật khẩu đã được băm SHA-256
     * @return Đối tượng Customer nếu khớp, null nếu không tìm thấy
     */
    public Customer login(String email, String password) {
        for (Customer c : customerRepository.readAll()) {
            if (c.getEmail().equalsIgnoreCase(email) && c.getPassword().equals(password)) {
                System.out.println("==> [CustomerController] Dang nhap thanh cong! Ten=" + c.getName());
                return c;
            }
        }
        System.out.println("==> [CustomerController] Dang nhap that bai! Email=" + email);
        return null;
    }

    /**
     * Cập nhật thông tin profile của khách hàng (tên, SĐT, địa chỉ).
     * Chỉ cập nhật trường nào khác null/rỗng.
     */
    public boolean updateProfile(Customer customer, String newName, String newPhone, String newAddress) {
        boolean changed = false;
        if (newName != null && !newName.isEmpty()) {
            customer.setName(newName);
            changed = true;
        }
        if (newPhone != null && !newPhone.isEmpty()) {
            customer.setPhone(newPhone);
            changed = true;
        }
        if (newAddress != null && !newAddress.isEmpty()) {
            customer.setAddress(newAddress);
            changed = true;
        }
        if (changed) {
            customerRepository.update(customer);
            System.out.println("==> [CustomerController] Cap nhat profile thanh cong! ID=" + customer.getId());
        }
        return changed;
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
     * NHIỆM VỤ BỔ SUNG (Chưa dùng trong tuần này):
     * Xem lịch sử đặt hàng của một Khách hàng dựa trên ID.
     * Hàm này đọc trực tiếp từ file orders.csv và lọc ra các đơn của khách đó.
     */
    public List<String> getViewPastOrderHistory(int customerId, String orderCsvPath) {
        List<String> history = new ArrayList<>();
        File file = new File(orderCsvPath);
        if (!file.exists()) return history;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine(); // Bỏ qua dòng tiêu đề (Header)
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    int idCustInOrder = Integer.parseInt(parts[1].trim());
                    if (idCustInOrder == customerId) {
                        history.add(String.format("Don hang ID: %s | Tong tien: %s VND | Trang thai: %s",
                                parts[0], parts[3], parts[5]));
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Loi khi doc lich su don hang: " + e.getMessage());
        }
        return history;
    }
}
