package com.example.nike_fe.data.model;

import com.google.gson.annotations.SerializedName;

public class OrderRequest {
    
    @SerializedName("shipping_address")
    private String shippingAddress;
    
    @SerializedName("payment_method")
    private String paymentMethod;
    
    @SerializedName("phone")
    private String phone;
    
    @SerializedName("coupon_code")
    private String couponCode;
    
    public OrderRequest(String shippingAddress, String paymentMethod, String phone, String couponCode) {
        this.shippingAddress = shippingAddress;
        this.paymentMethod = paymentMethod;
        this.phone = phone;
        this.couponCode = couponCode;
    }
    
    public String getShippingAddress() {
        return shippingAddress;
    }
    
    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getCouponCode() {
        return couponCode;
    }
    
    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }
}
