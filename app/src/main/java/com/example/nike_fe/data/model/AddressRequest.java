package com.example.nike_fe.data.model;

import com.google.gson.annotations.SerializedName;

public class AddressRequest {
    @SerializedName("recipientName")
    private String recipientName;

    @SerializedName("phoneNumber")
    private String phoneNumber;

    @SerializedName("addressLine")
    private String addressLine;

    @SerializedName("ward")
    private String ward;

    @SerializedName("district")
    private String district;

    @SerializedName("city")
    private String city;

    @SerializedName("isDefault")
    private boolean isDefault;

    public AddressRequest(String recipientName, String phoneNumber, String addressLine,
                         String ward, String district, String city, boolean isDefault) {
        this.recipientName = recipientName;
        this.phoneNumber = phoneNumber;
        this.addressLine = addressLine;
        this.ward = ward;
        this.district = district;
        this.city = city;
        this.isDefault = isDefault;
    }

    // Getters
    public String getRecipientName() {
        return recipientName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAddressLine() {
        return addressLine;
    }

    public String getWard() {
        return ward;
    }

    public String getDistrict() {
        return district;
    }

    public String getCity() {
        return city;
    }

    public boolean isDefault() {
        return isDefault;
    }
}
