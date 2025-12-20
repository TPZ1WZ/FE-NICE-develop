package com.example.nike_fe.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CouponListResponse {
    @SerializedName("content")
    private List<Coupon> content;

    @SerializedName("totalPages")
    private int totalPages;

    @SerializedName("totalElements")
    private int totalElements;

    @SerializedName("last")
    private boolean last;

    public List<Coupon> getContent() {
        return content;
    }

    public void setContent(List<Coupon> content) {
        this.content = content;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getTotalElements() {
        return totalElements;
    }

    public boolean isLast() {
        return last;
    }
}
