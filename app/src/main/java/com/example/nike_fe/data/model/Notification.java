package com.example.nike_fe.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

/**
 * Notification - Model cho thông báo
 */
public class Notification {

    @SerializedName("id")
    private Long id;

    @SerializedName("userId")
    private Long userId;

    @SerializedName("type")
    private String type; // ORDER, FAVORITE, COUPON, PRODUCT, SYSTEM

    @SerializedName("title")
    private String title;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private Map<String, Object> data;

    @SerializedName("isRead")
    private Boolean isRead;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    // Constructors
    public Notification() {
    }

    public Notification(Long id, Long userId, String type, String title, String message,
            Map<String, Object> data, Boolean isRead, String createdAt, String updatedAt) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.data = data;
        this.isRead = isRead;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
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

    // Helper methods

    /**
     * Kiểm tra thông báo có phải về đơn hàng không
     */
    public boolean isOrderNotification() {
        return "ORDER".equals(type);
    }

    /**
     * Kiểm tra thông báo có phải về sản phẩm yêu thích không
     */
    public boolean isFavoriteNotification() {
        return "FAVORITE".equals(type);
    }

    /**
     * Kiểm tra thông báo có phải về khuyến mãi không
     */
    public boolean isCouponNotification() {
        return "COUPON".equals(type);
    }

    /**
     * Lấy order_id từ data nếu là thông báo đơn hàng
     */
    public Long getOrderId() {
        if (data != null && data.containsKey("order_id")) {
            Object orderId = data.get("order_id");
            if (orderId instanceof Number) {
                return ((Number) orderId).longValue();
            }
        }
        return null;
    }

    /**
     * Lấy product_id từ data
     */
    public Long getProductId() {
        if (data != null && data.containsKey("product_id")) {
            Object productId = data.get("product_id");
            if (productId instanceof Number) {
                return ((Number) productId).longValue();
            }
        }
        return null;
    }

    /**
     * Lấy coupon_code từ data
     */
    public String getCouponCode() {
        if (data != null && data.containsKey("coupon_code")) {
            return (String) data.get("coupon_code");
        }
        return null;
    }

    /**
     * Lấy icon drawable resource id theo loại thông báo
     */
    public int getIconResource() {
        switch (type) {
            case "ORDER":
                return com.example.nike_fe.R.drawable.ic_shopping_bag;
            case "FAVORITE":
                return com.example.nike_fe.R.drawable.ic_heart;
            case "COUPON":
                return com.example.nike_fe.R.drawable.ic_coupon;
            case "PRODUCT":
                return com.example.nike_fe.R.drawable.ic_products;
            default:
                return com.example.nike_fe.R.drawable.ic_notification;
        }
    }

    /**
     * Lấy màu theo loại thông báo
     */
    public int getColor() {
        switch (type) {
            case "ORDER":
                return 0xFF4CAF50; // Green
            case "FAVORITE":
                return 0xFFE91E63; // Pink
            case "COUPON":
                return 0xFFFF9800; // Orange
            case "PRODUCT":
                return 0xFF2196F3; // Blue
            default:
                return 0xFF9E9E9E; // Gray
        }
    }

    /**
     * Format thời gian hiển thị (relative time)
     */
    public String getFormattedTime() {
        if (createdAt == null)
            return "";

        // TODO: Implement relative time formatting
        // Ví dụ: "2 phút trước", "1 giờ trước", "Hôm qua"
        return createdAt;
    }
}
