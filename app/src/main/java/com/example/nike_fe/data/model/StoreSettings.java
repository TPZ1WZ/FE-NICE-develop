package com.example.nike_fe.data.model;

import com.google.gson.annotations.SerializedName;

public class StoreSettings {
    @SerializedName("store_name")
    private String storeName;
    
    @SerializedName("store_phone")
    private String storePhone;
    
    @SerializedName("store_address")
    private String storeAddress;
    
    @SerializedName("notif_new_orders")
    private boolean notifNewOrders;
    
    @SerializedName("notif_out_of_stock")
    private boolean notifOutOfStock;
    
    @SerializedName("notif_system")
    private boolean notifSystem;

    public StoreSettings() {
    }

    public StoreSettings(String storeName, String storePhone, String storeAddress) {
        this.storeName = storeName;
        this.storePhone = storePhone;
        this.storeAddress = storeAddress;
        this.notifNewOrders = true;
        this.notifOutOfStock = true;
        this.notifSystem = true;
    }

    // Getters and Setters
    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getStorePhone() {
        return storePhone;
    }

    public void setStorePhone(String storePhone) {
        this.storePhone = storePhone;
    }

    public String getStoreAddress() {
        return storeAddress;
    }

    public void setStoreAddress(String storeAddress) {
        this.storeAddress = storeAddress;
    }

    public boolean isNotifNewOrders() {
        return notifNewOrders;
    }

    public void setNotifNewOrders(boolean notifNewOrders) {
        this.notifNewOrders = notifNewOrders;
    }

    public boolean isNotifOutOfStock() {
        return notifOutOfStock;
    }

    public void setNotifOutOfStock(boolean notifOutOfStock) {
        this.notifOutOfStock = notifOutOfStock;
    }

    public boolean isNotifSystem() {
        return notifSystem;
    }

    public void setNotifSystem(boolean notifSystem) {
        this.notifSystem = notifSystem;
    }
}
