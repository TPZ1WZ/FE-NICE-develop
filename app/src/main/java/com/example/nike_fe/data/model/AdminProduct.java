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
    
    @SerializedName("categoryId")
    private Long categoryId;
    
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
    
    @SerializedName("images")
    private java.util.List<String> images;
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("sizes")
    private java.util.List<String> sizes;
    
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

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
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

    public java.util.List<String> getImages() {
        return images;
    }

    public void setImages(java.util.List<String> images) {
        this.images = images;
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
    
    public java.util.List<String> getSizes() {
        return sizes;
    }

    public void setSizes(java.util.List<String> sizes) {
        this.sizes = sizes;
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
