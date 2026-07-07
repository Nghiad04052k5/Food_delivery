package com.delivery.model;

public class MenuItem extends BaseEntity {
    private int restaurantId;
    private String itemName;
    private double price;
    private int stockQty;
    private int version;

    public MenuItem() {
    }

    public MenuItem(int id, int restaurantId, String itemName, double price, int stockQty, int version) {
        this.id = id;
        this.restaurantId = restaurantId;
        this.itemName = itemName;
        this.price = price;
        this.stockQty = stockQty;
        this.version = version;
    }

    public int getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(int restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStockQty() {
        return stockQty;
    }

    public void setStockQty(int stockQty) {
        this.stockQty = stockQty;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "MenuItem{" +
                "id='" + id + '\'' +
                ", restaurantId='" + restaurantId + '\'' +
                ", itemName='" + itemName + '\'' +
                ", price=" + price +
                ", stockQty=" + stockQty +
                ", version=" + version +
                '}';
    }
}
