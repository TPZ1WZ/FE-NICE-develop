package com.example.nike_fe.data.model;

import com.google.gson.annotations.SerializedName;

public class OrderStatusDistribution {
    @SerializedName("completed")
    private long completed;
    
    @SerializedName("pending")
    private long pending;
    
    @SerializedName("confirmed")
    private long confirmed;
    
    @SerializedName("shipping")
    private long shipping;
    
    @SerializedName("canceled")
    private long canceled;
    
    @SerializedName("total")
    private long total;

    public long getCompleted() {
        return completed;
    }

    public void setCompleted(long completed) {
        this.completed = completed;
    }

    public long getPending() {
        return pending;
    }

    public void setPending(long pending) {
        this.pending = pending;
    }

    public long getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(long confirmed) {
        this.confirmed = confirmed;
    }

    public long getShipping() {
        return shipping;
    }

    public void setShipping(long shipping) {
        this.shipping = shipping;
    }

    public long getCanceled() {
        return canceled;
    }

    public void setCanceled(long canceled) {
        this.canceled = canceled;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
