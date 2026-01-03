package com.example.nike_fe.data.model;

import com.google.gson.annotations.SerializedName;

public class UserSpinManagement {

    @SerializedName("userId")
    private Long userId;

    @SerializedName("username")
    private String username;

    @SerializedName("email")
    private String email;

    @SerializedName("spinsToday")
    private Integer spinsToday;

    @SerializedName("spinsThisWeek")
    private Integer spinsThisWeek;

    @SerializedName("totalSpins")
    private Integer totalSpins;

    @SerializedName("prizesWon")
    private Integer prizesWon;

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getSpinsToday() {
        return spinsToday;
    }

    public void setSpinsToday(Integer spinsToday) {
        this.spinsToday = spinsToday;
    }

    public Integer getSpinsThisWeek() {
        return spinsThisWeek;
    }

    public void setSpinsThisWeek(Integer spinsThisWeek) {
        this.spinsThisWeek = spinsThisWeek;
    }

    public Integer getTotalSpins() {
        return totalSpins;
    }

    public void setTotalSpins(Integer totalSpins) {
        this.totalSpins = totalSpins;
    }

    public Integer getPrizesWon() {
        return prizesWon;
    }

    public void setPrizesWon(Integer prizesWon) {
        this.prizesWon = prizesWon;
    }
}
