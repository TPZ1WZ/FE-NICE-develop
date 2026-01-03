package com.example.nike_fe.data.model;

import com.google.gson.annotations.SerializedName;

public class SpinHistoryItem {
    
    @SerializedName("id")
    private Long id;
    
    @SerializedName("prize")
    private Prize prize;
    
    @SerializedName("spinTime")
    private String spinTime;
    
    @SerializedName("prizeCode")
    private String prizeCode;
    
    @SerializedName("isClaimed")
    private Boolean isClaimed;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Prize getPrize() { return prize; }
    public void setPrize(Prize prize) { this.prize = prize; }
    
    public String getSpinTime() { return spinTime; }
    public void setSpinTime(String spinTime) { this.spinTime = spinTime; }
    
    public String getPrizeCode() { return prizeCode; }
    public void setPrizeCode(String prizeCode) { this.prizeCode = prizeCode; }
    
    public Boolean getIsClaimed() { return isClaimed; }
    public void setIsClaimed(Boolean isClaimed) { this.isClaimed = isClaimed; }
}
