package com.example.nike_fe.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Order {
    
    private Long id;
    
    @SerializedName("user")
    private UserInfo user;
    
    private Double totalAmount;
    private Double totalDiscount;
    private Double finalAmount;
    private Integer quantity;
    private String receiverName;  // Tên người nhận hàng
    private String phone;
    private String status;
    private String paymentMethod;
    private String shippingAddress;
    private String txnId;
    private List<OrderItem> items;
    private String createdAt;
    
    // Nested class for user info
    public static class UserInfo {
        private Long id;
        private String fullName;
        private String email;
        private String phone;
        
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
        public String getFullName() {
            return fullName;
        }
        
        public void setFullName(String fullName) {
            this.fullName = fullName;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
        
        public String getPhone() {
            return phone;
        }
        
        public void setPhone(String phone) {
            this.phone = phone;
        }
    }
    
    // Helper methods for backward compatibility
    public String getUserName() {
        return user != null ? user.getFullName() : null;
    }
    
    public String getEmail() {
        return user != null ? user.getEmail() : null;
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public UserInfo getUser() {
        return user;
    }
    
    public void setUser(UserInfo user) {
        this.user = user;
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
    
    public String getReceiverName() {
        return receiverName;
    }
    
    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
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
