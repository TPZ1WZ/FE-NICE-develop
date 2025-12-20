package com.example.nike_fe.data.model;

import java.util.List;

public class Product {
    private Long id;
    private String name;
    private Double price;
    private String thumbnail;
    private java.util.List<String> sizes;
    private List<String> images;
    private boolean isFavorite;

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public Product() {
    }

    public Product(Long id, String name, Double price, String thumbnail) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.thumbnail = thumbnail;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getThumbnail() {
        // Fallback to first image if thumbnail is null
        if (thumbnail == null && images != null && !images.isEmpty()) {
            return images.get(0);
        }
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public java.util.List<String> getSizes() {
        return sizes;
    }

    public void setSizes(java.util.List<String> sizes) {
        this.sizes = sizes;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }
}
