package com.example.nike_fe.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Review {
    @SerializedName("id")
    private Long id;

    @SerializedName("productId")
    private Long productId;

    @SerializedName("productName")
    private String productName;

    @SerializedName("userId")
    private Long userId;

    @SerializedName("userName")
    private String userName;

    @SerializedName("rating")
    private Integer rating;

    @SerializedName("comment")
    private String comment;

    @SerializedName("title")
    private String title;

    @SerializedName("images")
    private List<String> images;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("approved")
    private Boolean approved;

    @SerializedName("reviewStatus")
    private String reviewStatus; // SAFE, WARNING, BLOCK

    @SerializedName("adminNote")
    private String adminNote;

    @SerializedName("aiSuggestion")
    private String aiSuggestion;

    @SerializedName("aiReasons")
    private List<String> aiReasons;

    @SerializedName("replies")
    private List<ReviewReply> replies;

    // Constructors
    public Review() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getApproved() {
        return approved != null && approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public String getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(String reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public String getAdminNote() {
        return adminNote;
    }

    public void setAdminNote(String adminNote) {
        this.adminNote = adminNote;
    }

    public String getAiSuggestion() {
        return aiSuggestion;
    }

    public void setAiSuggestion(String aiSuggestion) {
        this.aiSuggestion = aiSuggestion;
    }

    public List<String> getAiReasons() {
        return aiReasons;
    }

    public void setAiReasons(List<String> aiReasons) {
        this.aiReasons = aiReasons;
    }

    public List<ReviewReply> getReplies() {
        return replies;
    }

    public void setReplies(List<ReviewReply> replies) {
        this.replies = replies;
    }

    // Helper class for replies
    public static class ReviewReply {
        @SerializedName("id")
        private Long id;

        @SerializedName("comment")
        private String comment;

        @SerializedName("userName")
        private String userName;

        @SerializedName("isAdminReply")
        private Boolean isAdminReply;

        @SerializedName("createdAt")
        private String createdAt;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public Boolean getIsAdminReply() {
            return isAdminReply != null && isAdminReply;
        }

        public void setIsAdminReply(Boolean isAdminReply) {
            this.isAdminReply = isAdminReply;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }
    }
}
