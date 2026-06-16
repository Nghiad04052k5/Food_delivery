package com.delivery.model;

public class Driver extends BaseEntity {
    private String name;
    private String phone;
    private String email;
    private String password;
    private double latitude;
    private double longitude;
    private double collectedQrMoney;
    private DriverStatus status;
    private int version;

    // Constructor mặc định (bắt buộc cho CsvRepository)
    public Driver() {}

    // Constructor đầy đủ tham số
    public Driver(int id, String name, String phone, String email, String password,
                  double latitude, double longitude, double collectedQrMoney, DriverStatus status, int version) {
        this.setId(id);
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.password = password;
        this.latitude = latitude;
        this.longitude = longitude;
        this.collectedQrMoney = collectedQrMoney;
        this.status = status;
        this.version = version;
    }

    // Getter & Setter
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public double getCollectedQrMoney() { return collectedQrMoney; }
    public void setCollectedQrMoney(double collectedQrMoney) { this.collectedQrMoney = collectedQrMoney; }

    public DriverStatus getStatus() { return status; }
    public void setStatus(DriverStatus status) { this.status = status; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }

    @Override
    public String toString() {
        return "Driver{id=" + getId() +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", collectedQrMoney=" + collectedQrMoney +
                ", status=" + status +
                ", version=" + version +
                '}';
    }
}
