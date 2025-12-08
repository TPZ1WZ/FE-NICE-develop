package com.example.nike_fe.data.model;

import com.google.gson.annotations.SerializedName;

public class ProductStats {
    @SerializedName("total")
    private int total;
    
    @SerializedName("outOfStock")
    private int outOfStock;
    
    @SerializedName("lowStock")
    private int lowStock;

    public ProductStats() {
    }

    public ProductStats(int total, int outOfStock, int lowStock) {
        this.total = total;
        this.outOfStock = outOfStock;
        this.lowStock = lowStock;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getOutOfStock() {
        return outOfStock;
    }

    public void setOutOfStock(int outOfStock) {
        this.outOfStock = outOfStock;
    }

    public int getLowStock() {
        return lowStock;
    }

    public void setLowStock(int lowStock) {
        this.lowStock = lowStock;
    }
}
