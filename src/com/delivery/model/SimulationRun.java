package com.delivery.model;

public class SimulationRun extends BaseEntity {
    private int totalOrders;
    private int totalSuccess;
    private int totalFailed;
    private int durationMs;

    public SimulationRun() {
    }

    public SimulationRun(int id, int totalOrders, int totalSuccess, int totalFailed, int durationMs) {
        this.id = id;
        this.totalOrders = totalOrders;
        this.totalSuccess = totalSuccess;
        this.totalFailed = totalFailed;
        this.durationMs = durationMs;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    public int getTotalSuccess() {
        return totalSuccess;
    }

    public void setTotalSuccess(int totalSuccess) {
        this.totalSuccess = totalSuccess;
    }

    public int getTotalFailed() {
        return totalFailed;
    }

    public void setTotalFailed(int totalFailed) {
        this.totalFailed = totalFailed;
    }

    public int getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(int durationMs) {
        this.durationMs = durationMs;
    }

    @Override
    public String toString() {
        return "SimulationRun{" +
                "id=" + id +
                ", totalOrders=" + totalOrders +
                ", totalSuccess=" + totalSuccess +
                ", totalFailed=" + totalFailed +
                ", durationMs=" + durationMs +
                '}';
    }
}
