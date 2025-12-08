package com.example.nike_fe.data.model;

import com.google.gson.annotations.SerializedName;

public class DashboardStats {
    @SerializedName("totalRevenue")
    private double totalRevenue;
    
    @SerializedName("totalOrders")
    private int totalOrders;
    
    @SerializedName("totalUsers")
    private int totalUsers;
    
    @SerializedName("totalProducts")
    private int totalProducts;

    public DashboardStats() {
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    public int getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(int totalUsers) {
        this.totalUsers = totalUsers;
    }

    public int getProductsInStock() {
        // Backend returns totalProducts, we use it as in-stock
        return totalProducts;
    }

    public void setProductsInStock(int productsInStock) {
        this.totalProducts = productsInStock;
    }

    public int getProductsOutOfStock() {
        // TODO: Backend needs to add this field
        return 0;
    }

    public void setProductsOutOfStock(int productsOutOfStock) {
        // Not used for now
    }
}
