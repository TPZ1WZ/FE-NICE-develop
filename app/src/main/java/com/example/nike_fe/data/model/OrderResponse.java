package com.example.nike_fe.data.model;

import com.google.gson.annotations.SerializedName;

public class OrderResponse {
    
    @SerializedName("status")
    private String status;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName(value = "paymentUrl", alternate = {"payment_url"})
    private String paymentUrl;
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getPaymentUrl() {
        return paymentUrl;
    }
    
    public void setPaymentUrl(String paymentUrl) {
        this.paymentUrl = paymentUrl;
    }
    
    public boolean isSuccess() {
        return "success".equals(status);
    }
    
    public boolean requiresPayment() {
        return "redirect_to_payment".equals(status);
    }
}
