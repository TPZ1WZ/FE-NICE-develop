package com.example.nike_fe.data.model;

import com.google.gson.annotations.SerializedName;

public class LoyaltyPointsResponse {
    
    @SerializedName("currentPoints")
    private Integer currentPoints;
    
    @SerializedName("totalEarned")
    private Integer totalEarned;
    
    @SerializedName("totalSpent")
    private Integer totalSpent;
    
    @SerializedName("currentStreak")
    private Integer currentStreak;
    
    @SerializedName("totalCheckins")
    private Long totalCheckins;
    
    @SerializedName("expiringCoins")
    private Integer expiringCoins;
    
    @SerializedName("expiryDate")
    private String expiryDate;

    // Getters and Setters
    public Integer getCurrentPoints() {
        return currentPoints;
    }

    public void setCurrentPoints(Integer currentPoints) {
        this.currentPoints = currentPoints;
    }

    public Integer getTotalEarned() {
        return totalEarned;
    }

    public void setTotalEarned(Integer totalEarned) {
        this.totalEarned = totalEarned;
    }

    public Integer getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(Integer totalSpent) {
        this.totalSpent = totalSpent;
    }

    public Integer getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(Integer currentStreak) {
        this.currentStreak = currentStreak;
    }

    public Long getTotalCheckins() {
        return totalCheckins;
    }

    public void setTotalCheckins(Long totalCheckins) {
        this.totalCheckins = totalCheckins;
    }
    
    public Integer getExpiringCoins() {
        return expiringCoins;
    }
    
    public void setExpiringCoins(Integer expiringCoins) {
        this.expiringCoins = expiringCoins;
    }
    
    public String getExpiryDate() {
        return expiryDate;
    }
    
    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }
}
