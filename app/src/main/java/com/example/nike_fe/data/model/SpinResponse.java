package com.example.nike_fe.data.model;

import com.google.gson.annotations.SerializedName;

public class SpinResponse {
    
    @SerializedName("success")
    private Boolean success;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("prize")
    private Prize prize;
    
    @SerializedName("prizeCode")
    private String prizeCode;
    
    @SerializedName("spinHistory")
    private SpinHistoryItem spinHistory;

    // Getters and Setters
    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public Prize getPrize() { return prize; }
    public void setPrize(Prize prize) { this.prize = prize; }
    
    public String getPrizeCode() { return prizeCode; }
    public void setPrizeCode(String prizeCode) { this.prizeCode = prizeCode; }
    
    public SpinHistoryItem getSpinHistory() { return spinHistory; }
    public void setSpinHistory(SpinHistoryItem spinHistory) { this.spinHistory = spinHistory; }
}
