package com.example.nike_fe.data.model;

import com.google.gson.annotations.SerializedName;

public class LuckyWheelStatistics {

    @SerializedName("totalSpins")
    private Long totalSpins;

    @SerializedName("spinsToday")
    private Long spinsToday;

    @SerializedName("spinsThisWeek")
    private Long spinsThisWeek;

    @SerializedName("uniqueUsers")
    private Long uniqueUsers;

    @SerializedName("totalPrizesWon")
    private Long totalPrizesWon;

    @SerializedName("mostPopularPrize")
    private String mostPopularPrize;

    @SerializedName("mostPopularPrizeCount")
    private Long mostPopularPrizeCount;

    @SerializedName("topSpinner")
    private String topSpinner;

    @SerializedName("topSpinnerCount")
    private Long topSpinnerCount;

    // Getters and Setters
    public Long getTotalSpins() {
        return totalSpins;
    }

    public void setTotalSpins(Long totalSpins) {
        this.totalSpins = totalSpins;
    }

    public Long getSpinsToday() {
        return spinsToday;
    }

    public void setSpinsToday(Long spinsToday) {
        this.spinsToday = spinsToday;
    }

    public Long getSpinsThisWeek() {
        return spinsThisWeek;
    }

    public void setSpinsThisWeek(Long spinsThisWeek) {
        this.spinsThisWeek = spinsThisWeek;
    }

    public Long getUniqueUsers() {
        return uniqueUsers;
    }

    public void setUniqueUsers(Long uniqueUsers) {
        this.uniqueUsers = uniqueUsers;
    }

    public Long getTotalPrizesWon() {
        return totalPrizesWon;
    }

    public void setTotalPrizesWon(Long totalPrizesWon) {
        this.totalPrizesWon = totalPrizesWon;
    }

    public String getMostPopularPrize() {
        return mostPopularPrize;
    }

    public void setMostPopularPrize(String mostPopularPrize) {
        this.mostPopularPrize = mostPopularPrize;
    }

    public Long getMostPopularPrizeCount() {
        return mostPopularPrizeCount;
    }

    public void setMostPopularPrizeCount(Long mostPopularPrizeCount) {
        this.mostPopularPrizeCount = mostPopularPrizeCount;
    }

    public String getTopSpinner() {
        return topSpinner;
    }

    public void setTopSpinner(String topSpinner) {
        this.topSpinner = topSpinner;
    }

    public Long getTopSpinnerCount() {
        return topSpinnerCount;
    }

    public void setTopSpinnerCount(Long topSpinnerCount) {
        this.topSpinnerCount = topSpinnerCount;
    }
}
