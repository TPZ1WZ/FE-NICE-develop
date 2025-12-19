package com.example.nike_fe.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FavoriteProduct {
    @SerializedName("id")
    private Long id;
    
    @SerializedName("productId")
    private Long productId;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("slug")
    private String slug;
    
    @SerializedName("subTitle")
    private String subTitle;
    
    @SerializedName("price")
    private Double price;
    
    @SerializedName("stock")
    private Integer stock;
    
    @SerializedName("images")
    private List<String> images;
    
    @SerializedName("categoryName")
    private String categoryName;
    
    @SerializedName("addedAt")
    private String addedAt;
    
    // Constructor
    public FavoriteProduct() {}
    
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
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getSlug() {
        return slug;
    }
    
    public void setSlug(String slug) {
        this.slug = slug;
    }
    
    public String getSubTitle() {
        return subTitle;
    }
    
    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }
    
    public Double getPrice() {
        return price;
    }
    
    public void setPrice(Double price) {
        this.price = price;
    }
    
    public Integer getStock() {
        return stock;
    }
    
    public void setStock(Integer stock) {
        this.stock = stock;
    }
    
    public List<String> getImages() {
        return images;
    }
    
    public void setImages(List<String> images) {
        this.images = images;
    }
    
    public String getCategoryName() {
        return categoryName;
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    
    public String getAddedAt() {
        return addedAt;
    }
    
    public void setAddedAt(String addedAt) {
        this.addedAt = addedAt;
    }
}
