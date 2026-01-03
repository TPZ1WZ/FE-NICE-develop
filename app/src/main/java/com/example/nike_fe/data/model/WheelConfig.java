package com.example.nike_fe.data.model;

import com.google.gson.annotations.SerializedName;

public class WheelConfig {

    @SerializedName("id")
    private Long id;

    @SerializedName("configKey")
    private String configKey;

    @SerializedName("isActive")
    private Boolean isActive;

    @SerializedName("maxSpinsPerDay")
    private Integer maxSpinsPerDay;

    @SerializedName("maxSpinsPerWeek")
    private Integer maxSpinsPerWeek;

    @SerializedName("requiresLogin")
    private Boolean requiresLogin;

    @SerializedName("requiresOrder")
    private Boolean requiresOrder;

    @SerializedName("minOrderCount")
    private Integer minOrderCount;

    @SerializedName("description")
    private String description;

    @SerializedName("eventName")
    private String eventName;

    @SerializedName("startDate")
    private String startDate; // Using String for simplicity

    @SerializedName("endDate")
    private String endDate; // Using String for simplicity

    @SerializedName("isTimeRestricted")
    private Boolean isTimeRestricted;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Integer getMaxSpinsPerDay() {
        return maxSpinsPerDay;
    }

    public void setMaxSpinsPerDay(Integer maxSpinsPerDay) {
        this.maxSpinsPerDay = maxSpinsPerDay;
    }

    public Integer getMaxSpinsPerWeek() {
        return maxSpinsPerWeek;
    }

    public void setMaxSpinsPerWeek(Integer maxSpinsPerWeek) {
        this.maxSpinsPerWeek = maxSpinsPerWeek;
    }

    public Boolean getRequiresLogin() {
        return requiresLogin;
    }

    public void setRequiresLogin(Boolean requiresLogin) {
        this.requiresLogin = requiresLogin;
    }

    public Boolean getRequiresOrder() {
        return requiresOrder;
    }

    public void setRequiresOrder(Boolean requiresOrder) {
        this.requiresOrder = requiresOrder;
    }

    public Integer getMinOrderCount() {
        return minOrderCount;
    }

    public void setMinOrderCount(Integer minOrderCount) {
        this.minOrderCount = minOrderCount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public Boolean getIsTimeRestricted() {
        return isTimeRestricted;
    }

    public void setIsTimeRestricted(Boolean timeRestricted) {
        isTimeRestricted = timeRestricted;
    }
}
