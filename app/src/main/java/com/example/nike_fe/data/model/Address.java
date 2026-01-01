package com.example.nike_fe.data.model;

import com.google.gson.annotations.SerializedName;

public class Address {
    @SerializedName("id")
    private Long id;

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

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    // Constructors
    public Address() {
    }

    public Address(String recipientName, String phoneNumber, String addressLine, 
                   String ward, String district, String city, boolean isDefault) {
        this.recipientName = recipientName;
        this.phoneNumber = phoneNumber;
        this.addressLine = addressLine;
        this.ward = ward;
        this.district = district;
        this.city = city;
        this.isDefault = isDefault;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddressLine() {
        return addressLine;
    }

    public void setAddressLine(String addressLine) {
        this.addressLine = addressLine;
    }

    public String getWard() {
        return ward;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper method to get full address
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (addressLine != null && !addressLine.isEmpty()) {
            sb.append(addressLine);
        }
        if (ward != null && !ward.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(ward);
        }
        if (district != null && !district.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(district);
        }
        if (city != null && !city.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(city);
        }
        return sb.toString();
    }
}
