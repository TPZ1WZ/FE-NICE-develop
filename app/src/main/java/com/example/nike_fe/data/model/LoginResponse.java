package com.example.nike_fe.data.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("accessToken")
    private String accessToken;
    
    @SerializedName("role")
    private String role;
    
    @SerializedName("success")
    private boolean success;
    
    @SerializedName("message")
    private String message;

    public String getAccessToken() {
        return accessToken;
    }

    public String getRole() {
        return role;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
