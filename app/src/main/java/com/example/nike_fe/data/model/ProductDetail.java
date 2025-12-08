package com.example.nike_fe.data.model;

import java.util.List;

public class ProductDetail {
    private Long id;
    private String name;
    private String slug;
    private String subTitle;
    private String description;
    private Double price;
    private Integer stock;
    private List<String> images;
    private List<Long> categoryId;
    private Long reviewId;
    private Boolean isFavorite;

    public ProductDetail() {
    }

    public ProductDetail(Long id, String name, String slug, String subTitle, String description, 
                        Double price, Integer stock, List<String> images, List<Long> categoryId, 
                        Long reviewId, Boolean isFavorite) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.subTitle = subTitle;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.images = images;
        this.categoryId = categoryId;
        this.reviewId = reviewId;
        this.isFavorite = isFavorite;
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public List<Long> getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(List<Long> categoryId) {
        this.categoryId = categoryId;
    }

    public Long getReviewId() {
        return reviewId;
    }

    public void setReviewId(Long reviewId) {
        this.reviewId = reviewId;
    }

    public Boolean getIsFavorite() {
        return isFavorite;
    }

    public void setIsFavorite(Boolean isFavorite) {
        this.isFavorite = isFavorite;
    }
}
