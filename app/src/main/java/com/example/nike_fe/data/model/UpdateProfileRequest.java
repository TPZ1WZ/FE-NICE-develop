package com.example.nike_fe.data.model;

import com.google.gson.annotations.SerializedName;

public class UpdateProfileRequest {
    @SerializedName("full_name")
    private String fullName;
    
    private String phone;
    private String address;

    public UpdateProfileRequest() {
    }

    public UpdateProfileRequest(String fullName) {
        this.fullName = fullName;
    }

    public UpdateProfileRequest(String fullName, String phone, String address) {
        this.fullName = fullName;
        this.phone = phone;
        this.address = address;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @SerializedName("password")
    private String password;
}
