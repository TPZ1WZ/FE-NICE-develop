package com.example.nike_fe.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

/**
 * NotificationStatistics - Thống kê thông báo theo loại
 */
public class NotificationStatistics {
    
    @SerializedName("ORDER")
    private Long orderCount;
    
    @SerializedName("FAVORITE")
    private Long favoriteCount;
    
    @SerializedName("COUPON")
    private Long couponCount;
    
    @SerializedName("PRODUCT")
    private Long productCount;
    
    @SerializedName("SYSTEM")
    private Long systemCount;

    public NotificationStatistics() {}

    // Getters and Setters
    public Long getOrderCount() {
        return orderCount != null ? orderCount : 0L;
    }

    public void setOrderCount(Long orderCount) {
        this.orderCount = orderCount;
    }

    public Long getFavoriteCount() {
        return favoriteCount != null ? favoriteCount : 0L;
    }

    public void setFavoriteCount(Long favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    public Long getCouponCount() {
        return couponCount != null ? couponCount : 0L;
    }

    public void setCouponCount(Long couponCount) {
        this.couponCount = couponCount;
    }

    public Long getProductCount() {
        return productCount != null ? productCount : 0L;
    }

    public void setProductCount(Long productCount) {
        this.productCount = productCount;
    }

    public Long getSystemCount() {
        return systemCount != null ? systemCount : 0L;
    }

    public void setSystemCount(Long systemCount) {
        this.systemCount = systemCount;
    }

    /**
     * Tổng số thông báo
     */
    public Long getTotalCount() {
        return getOrderCount() + getFavoriteCount() + getCouponCount() + 
               getProductCount() + getSystemCount();
    }
}
