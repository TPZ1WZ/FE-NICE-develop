package com.example.nike_fe.data.model;

public class AddToCartRequest {
    private Long productId;
    private Integer quantity;
    private String size;

    public AddToCartRequest() {
    }

    public AddToCartRequest(Long productId, Integer quantity, String size) {
        this.productId = productId;
        this.quantity = quantity;
        this.size = size;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}
