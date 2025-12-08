package com.example.nike_fe.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TopProductsResponse {
    @SerializedName("products")
    private List<TopProductItem> products;
    
    @SerializedName("total")
    private int total;

    public List<TopProductItem> getProducts() {
        return products;
    }

    public void setProducts(List<TopProductItem> products) {
        this.products = products;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
    
    public static class TopProductItem {
        @SerializedName("id")
        private Long id;
        
        @SerializedName("name")
        private String name;
        
        @SerializedName("soldQuantity")
        private int soldQuantity;

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

        public int getSoldQuantity() {
            return soldQuantity;
        }

        public void setSoldQuantity(int soldQuantity) {
            this.soldQuantity = soldQuantity;
        }
    }
}
