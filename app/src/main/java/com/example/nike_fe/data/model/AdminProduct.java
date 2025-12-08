package com.example.nike_fe.data.model;

import com.google.gson.annotations.SerializedName;

public class AdminProduct {
    @SerializedName("id")
    private Long id;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("sku")
    private String sku;
    
    @SerializedName("category")
    private String category;
    
    @SerializedName("price")
    private double price;
    
    @SerializedName("salePrice")
    private Double salePrice;
    
    @SerializedName("stock")
    private int stock;
    
    @SerializedName("status")
    private String status; // "active" or "inactive"
    
    @SerializedName("image")
    private String image;
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("createdAt")
    private String createdAt;

    public AdminProduct() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Double getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(Double salePrice) {
        this.salePrice = salePrice;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public boolean isLowStock() {
        return stock > 0 && stock < 10;
    }
    
    public boolean isOutOfStock() {
        return stock == 0;
    }
    
    public boolean isActive() {
        return "active".equals(status);
    }
}
