package com.example.nike_fe.data.model;

import com.google.gson.annotations.SerializedName;

public class SpinResponse {
    private Boolean success;
    private String message;
    
    @SerializedName("rewardPosition")
    private Integer rewardPosition; // Vị trí trúng thưởng (0-7)
    
    @SerializedName("rewardType")
    private String rewardType; // COIN, COUPON, NOTHING
    
    @SerializedName("coinAmount")
    private Integer coinAmount; // Số coin nhận được
    
    @SerializedName("totalPoints")
    private Integer totalPoints; // Tổng coin sau khi quay
    
    @SerializedName("hasFreeSpinLeft")
    private Boolean hasFreeSpinLeft; // Còn lượt free không

    // Getters and Setters
    public Boolean isSuccess() {
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

    public Integer getRewardPosition() {
        return rewardPosition;
    }

    public void setRewardPosition(Integer rewardPosition) {
        this.rewardPosition = rewardPosition;
    }

    public String getRewardType() {
        return rewardType;
    }

    public void setRewardType(String rewardType) {
        this.rewardType = rewardType;
    }

    public Integer getCoinAmount() {
        return coinAmount;
    }

    public void setCoinAmount(Integer coinAmount) {
        this.coinAmount = coinAmount;
    }

    public Integer getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(Integer totalPoints) {
        this.totalPoints = totalPoints;
    }

    public Boolean getHasFreeSpinLeft() {
        return hasFreeSpinLeft;
    }

    public void setHasFreeSpinLeft(Boolean hasFreeSpinLeft) {
        this.hasFreeSpinLeft = hasFreeSpinLeft;
    }
    
    // Convenience method for prize index (same as rewardPosition)
    public int getPrizeIndex() {
        return rewardPosition != null ? rewardPosition : 0;
    }
}
