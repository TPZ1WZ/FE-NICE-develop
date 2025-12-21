package com.example.nike_fe.data.model;

import java.util.List;

public class OrderItem {

    private Long id;
    private Long productId;
    private String productName;
    private List<String> productImages;
    private Integer quantity;
    private Double productPrice;
    private Double totalPrice;
    private String size;
    private boolean reviewed;
    private List<String> reviewImages; // For user upload
    private String commentDraft; // To save user input while scrolling or updating

    // Getters and Setters

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

    public List<String> getReviewImages() {
        return reviewImages;
    }

    public void setReviewImages(List<String> reviewImages) {
        this.reviewImages = reviewImages;
    }

    public String getFirstImage() {
        if (productImages != null && !productImages.isEmpty()) {
            return productImages.get(0);
        }
        return null;
    }

    public String getCommentDraft() {
        return commentDraft;
    }

    public void setCommentDraft(String commentDraft) {
        this.commentDraft = commentDraft;
    }
}
