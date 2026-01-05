package com.example.nike_fe.data.model;

import com.google.gson.annotations.SerializedName;

public class CheckinResponse {
    
    @SerializedName("success")
    private Boolean success;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("rewardAmount")
    private Integer rewardAmount;
    
    @SerializedName("currentStreak")
    private Integer currentStreak;
    
    @SerializedName("totalPoints")
    private Integer totalPoints;
    
    @SerializedName("transactionId")
    private Long transactionId;

    // Getters and Setters
    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getRewardAmount() {
        return rewardAmount;
    }

    public void setRewardAmount(Integer rewardAmount) {
        this.rewardAmount = rewardAmount;
    }

    public Integer getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(Integer currentStreak) {
        this.currentStreak = currentStreak;
    }

    public Integer getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(Integer totalPoints) {
        this.totalPoints = totalPoints;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }
}
