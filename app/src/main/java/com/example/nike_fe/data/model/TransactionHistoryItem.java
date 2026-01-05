package com.example.nike_fe.data.model;

import com.google.gson.annotations.SerializedName;

public class TransactionHistoryItem {
    
    @SerializedName("id")
    private Long id;
    
    @SerializedName("transactionType")
    private String transactionType; // EARN, SPEND, REFUND
    
    @SerializedName("amount")
    private Integer amount;
    
    @SerializedName("source")
    private String source; // DAILY_CHECKIN, ORDER, REVIEW, etc.
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("balanceAfter")
    private Integer balanceAfter;
    
    @SerializedName("createdAt")
    private String createdAt;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(Integer balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
