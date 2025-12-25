package com.example.nike_fe.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * UnreadCountResponse - Response chứa số lượng thông báo chưa đọc
 */
public class UnreadCountResponse {
    
    @SerializedName("count")
    private Long count;

    public UnreadCountResponse() {}

    public UnreadCountResponse(Long count) {
        this.count = count;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
