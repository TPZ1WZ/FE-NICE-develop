package com.example.nike_fe.data.model;

import java.util.ArrayList;
import java.util.List;

public class CartResponse {
    private Long id;
    private List<CartItem> items = new ArrayList<>();
    private Double totalPrice = 0.0;
    private Integer totalQuantity = 0;

    public CartResponse() {
    }

    public CartResponse(Long id, List<CartItem> items, Double totalPrice, Integer totalQuantity) {
        this.id = id;
        this.items = items;
        this.totalPrice = totalPrice;
        this.totalQuantity = totalQuantity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }
}
