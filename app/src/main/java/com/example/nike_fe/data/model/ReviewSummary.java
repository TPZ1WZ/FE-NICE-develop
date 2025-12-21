package com.example.nike_fe.data.model;

import com.google.gson.annotations.SerializedName;

public class ReviewSummary {
    @SerializedName("productId")
    private Long productId;

    @SerializedName("averageRating")
    private Double averageRating;

    @SerializedName("totalReviews")
    private Long totalReviews;

    @SerializedName("fiveStars")
    private Long fiveStars;

    @SerializedName("fourStars")
    private Long fourStars;

    @SerializedName("threeStars")
    private Long threeStars;

    @SerializedName("twoStars")
    private Long twoStars;

    @SerializedName("oneStar")
    private Long oneStar;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Long getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(Long totalReviews) {
        this.totalReviews = totalReviews;
    }

    public Long getFiveStars() {
        return fiveStars;
    }

    public void setFiveStars(Long fiveStars) {
        this.fiveStars = fiveStars;
    }

    public Long getFourStars() {
        return fourStars;
    }

    public void setFourStars(Long fourStars) {
        this.fourStars = fourStars;
    }

    public Long getThreeStars() {
        return threeStars;
    }

    public void setThreeStars(Long threeStars) {
        this.threeStars = threeStars;
    }

    public Long getTwoStars() {
        return twoStars;
    }

    public void setTwoStars(Long twoStars) {
        this.twoStars = twoStars;
    }

    public Long getOneStar() {
        return oneStar;
    }

    public void setOneStar(Long oneStar) {
        this.oneStar = oneStar;
    }
}
