package com.example.nike_fe.data.model;

import java.util.List;

public class CartItem {
    private Long id;
    private ProductInCart product;
    private Integer quantity;
    private String size;
    private Double productPrice;
    private Double totalPrice;

    public CartItem() {
    }

    public CartItem(Long id, ProductInCart product, Integer quantity, String size, 
                   Double productPrice, Double totalPrice) {
        this.id = id;
        this.product = product;
        this.quantity = quantity;
        this.size = size;
        this.productPrice = productPrice;
        this.totalPrice = totalPrice;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ProductInCart getProduct() {
        return product;
    }

    public void setProduct(ProductInCart product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public Double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(Double productPrice) {
        this.productPrice = productPrice;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public static class ProductInCart {
        private Long id;
        private String name;
        private String slug;
        private String subTitle;
        private String description;
        private Double price;
        private List<String> images;

        public ProductInCart() {
        }

        public ProductInCart(Long id, String name, String slug, String subTitle, 
                           String description, Double price, List<String> images) {
            this.id = id;
            this.name = name;
            this.slug = slug;
            this.subTitle = subTitle;
            this.description = description;
            this.price = price;
            this.images = images;
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

        public String getSlug() {
            return slug;
        }

        public void setSlug(String slug) {
            this.slug = slug;
        }

        public String getSubTitle() {
            return subTitle;
        }

        public void setSubTitle(String subTitle) {
            this.subTitle = subTitle;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Double getPrice() {
            return price;
        }

        public void setPrice(Double price) {
            this.price = price;
        }

        public List<String> getImages() {
            return images;
        }

        public void setImages(List<String> images) {
            this.images = images;
        }
    }
}
