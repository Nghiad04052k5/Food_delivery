package com.delivery.model;

public class Customer extends BaseEntity {
    private String name;
    private String phone;
    private String address;
    private double latitude;
    private double longitude;
    private String email;
    private String password;

    // Constructor mặc định (bắt buộc cho CsvRepository)
    public Customer() {}

    // Constructor đầy đủ tham số
    public Customer(int id, String name, String phone, String address,
                    double latitude, double longitude, String email, String password) {
        this.setId(id);
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.email = email;
        this.password = password;
    }

    // Getter & Setter
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    @Override
    public String toString() {
        return "Customer{id=" + getId() +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
