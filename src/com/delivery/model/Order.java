package com.delivery.model;

public class Order extends BaseEntity {
    private int customerId;
    private Integer driverId; // Can be null
    private double totalPrice;
    private String paymentMethod; // CASH, ONLINE_PAYMENT, QR_CODE
    private OrderStatus status;
    private int version;

    public Order() {
    }

    public Order(int id, int customerId, Integer driverId, double totalPrice, String paymentMethod, OrderStatus status, int version) {
        this.id = id;
        this.customerId = customerId;
        this.driverId = driverId;
        this.totalPrice = totalPrice;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.version = version;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public Integer getDriverId() {
        return driverId;
    }

    public void setDriverId(Integer driverId) {
        this.driverId = driverId;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", customerId=" + customerId +
                ", driverId=" + driverId +
                ", totalPrice=" + totalPrice +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", status=" + status +
                ", version=" + version +
                '}';
    }
}
