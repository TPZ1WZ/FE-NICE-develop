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

    @SerializedName("revenueGrowth")
    private double revenueGrowth;

    @SerializedName("ordersGrowth")
    private double ordersGrowth;

    @SerializedName("usersGrowth")
    private double usersGrowth;

    @SerializedName("productsOutOfStock")
    private int productsOutOfStock;

    public int getProductsInStock() {
        return totalProducts;
    }

    public void setProductsInStock(int productsInStock) {
        this.totalProducts = productsInStock;
    }

    public int getProductsOutOfStock() {
        return productsOutOfStock;
    }

    public void setProductsOutOfStock(int productsOutOfStock) {
        this.productsOutOfStock = productsOutOfStock;
    }

    public double getRevenueGrowth() {
        return revenueGrowth;
    }

    public void setRevenueGrowth(double revenueGrowth) {
        this.revenueGrowth = revenueGrowth;
    }

    public double getOrdersGrowth() {
        return ordersGrowth;
    }

    public void setOrdersGrowth(double ordersGrowth) {
        this.ordersGrowth = ordersGrowth;
    }

    public double getUsersGrowth() {
        return usersGrowth;
    }

    public void setUsersGrowth(double usersGrowth) {
        this.usersGrowth = usersGrowth;
    }
}
