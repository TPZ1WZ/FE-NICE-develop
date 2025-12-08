package com.example.nike_fe.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OrderItem {
    
    @SerializedName("id")
    private Long id;
    
    @SerializedName("product_id")
    private Long productId;
    
    @SerializedName("product_name")
    private String productName;
    
    @SerializedName("product_images")
    private List<String> productImages;
    
    @SerializedName("quantity")
    private Integer quantity;
    
    @SerializedName("product_price")
    private Double productPrice;
    
    @SerializedName("total_price")
    private Double totalPrice;
    
    @SerializedName("size")
    private String size;
    
    @SerializedName("reviewed")
    private boolean reviewed;
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public List<String> getProductImages() {
        return productImages;
    }
    
    public void setProductImages(List<String> productImages) {
        this.productImages = productImages;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public Double getProductPrice() {
        return productPrice;
    }
    
    public void setProductPrice(Double productPrice) {
        this.productPrice = productPrice;
    }
    
    public Double getTotalPrice() {
        return totalPrice;
    }
    
    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }
    
    public String getSize() {
        return size;
    }
    
    public void setSize(String size) {
        this.size = size;
    }
    
    public boolean isReviewed() {
        return reviewed;
    }
    
    public void setReviewed(boolean reviewed) {
        this.reviewed = reviewed;
    }
    
    public String getFirstImage() {
        if (productImages != null && !productImages.isEmpty()) {
            return productImages.get(0);
        }
        return null;
    }
}
