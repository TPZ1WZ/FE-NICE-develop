package com.example.nike_fe.data.model;

public class Banner {
    private int backgroundDrawable;
    private String discount;
    private String title;
    private String description;
    private int imageResId;

    public Banner(int backgroundDrawable, String discount, String title, String description, int imageResId) {
        this.backgroundDrawable = backgroundDrawable;
        this.discount = discount;
        this.title = title;
        this.description = description;
        this.imageResId = imageResId;
    }

    public int getBackgroundDrawable() {
        return backgroundDrawable;
    }

    public String getDiscount() {
        return discount;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getImageResId() {
        return imageResId;
    }
}
