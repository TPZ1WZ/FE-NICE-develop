package com.example.nike_fe.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TopProductsResponse {
    @SerializedName("products")
    private List<TopProduct> products;

    @SerializedName("total")
    private int total;

    public List<TopProduct> getProducts() {
        return products;
    }

    public void setProducts(List<TopProduct> products) {
        this.products = products;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
