package com.example.nike_fe.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FavoriteResponse {
    @SerializedName("success")
    private boolean success;
    
    @SerializedName("data")
    private List<FavoriteProduct> data;
    
    @SerializedName("currentPage")
    private int currentPage;
    
    @SerializedName("totalPages")
    private int totalPages;
    
    @SerializedName("totalItems")
    private long totalItems;
    
    @SerializedName("message")
    private String message;
    
    // Constructor
    public FavoriteResponse() {}
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public List<FavoriteProduct> getData() {
        return data;
    }
    
    public void setData(List<FavoriteProduct> data) {
        this.data = data;
    }
    
    public int getCurrentPage() {
        return currentPage;
    }
    
    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }
    
    public int getTotalPages() {
        return totalPages;
    }
    
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
    
    public long getTotalItems() {
        return totalItems;
    }
    
    public void setTotalItems(long totalItems) {
        this.totalItems = totalItems;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}
