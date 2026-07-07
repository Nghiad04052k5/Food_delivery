package com.delivery.model;

public class DeliveryRoute extends BaseEntity {
    private int orderId;
    private double distanceKm;
    private int estimatedTimeMin;

    // Constructor mặc định (bắt buộc cho CsvRepository)
    public DeliveryRoute() {}

    // Constructor đầy đủ tham số
    public DeliveryRoute(int id, int orderId, double distanceKm, int estimatedTimeMin) {
        this.setId(id);
        this.orderId = orderId;
        this.distanceKm = distanceKm;
        this.estimatedTimeMin = estimatedTimeMin;
    }

    // Getter & Setter
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }

    public int getEstimatedTimeMin() { return estimatedTimeMin; }
    public void setEstimatedTimeMin(int estimatedTimeMin) { this.estimatedTimeMin = estimatedTimeMin; }

    @Override
    public String toString() {
        return "DeliveryRoute{id='" + getId() + '\'' +
                ", orderId='" + orderId + '\'' +
                ", distanceKm=" + distanceKm +
                ", estimatedTimeMin=" + estimatedTimeMin +
                '}';
    }
}