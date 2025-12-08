package com.example.nike_fe.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Order {
    
    @SerializedName("id")
    private Long id;
    
    @SerializedName("total_amount")
    private Double totalAmount;
    
    @SerializedName("total_discount")
    private Double totalDiscount;
    
    @SerializedName("final_amount")
    private Double finalAmount;
    
    @SerializedName("quantity")
    private Integer quantity;
    
    @SerializedName("phone")
    private String phone;
    
    @SerializedName("status")
    private String status;
    
    @SerializedName("payment_method")
    private String paymentMethod;
    
    @SerializedName("shipping_address")
    private String shippingAddress;
    
    @SerializedName("txn_id")
    private String txnId;
    
    @SerializedName("items")
    private List<OrderItem> items;
    
    @SerializedName("created_at")
    private String createdAt;
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Double getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public Double getTotalDiscount() {
        return totalDiscount;
    }
    
    public void setTotalDiscount(Double totalDiscount) {
        this.totalDiscount = totalDiscount;
    }
    
    public Double getFinalAmount() {
        return finalAmount;
    }
    
    public void setFinalAmount(Double finalAmount) {
        this.finalAmount = finalAmount;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getShippingAddress() {
        return shippingAddress;
    }
    
    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
    
    public String getTxnId() {
        return txnId;
    }
    
    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }
    
    public List<OrderItem> getItems() {
        return items;
    }
    
    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
