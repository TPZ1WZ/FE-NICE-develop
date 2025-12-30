package com.example.nike_fe.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class RevenueChartData {
    @SerializedName("labels")
    private List<String> labels;

    @SerializedName("data")
    private List<Double> data;

    @SerializedName("title")
    private String title;
    
    // Optional: Previous period total for comparison
    // Backend should provide this to calculate accurate change percentage
    @SerializedName("previousPeriodTotal")
    private Double previousPeriodTotal;
    
    @SerializedName("changePercent")
    private Double changePercent;

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public List<Double> getData() {
        return data;
    }

    public void setData(List<Double> data) {
        this.data = data;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
    public Double getPreviousPeriodTotal() {
        return previousPeriodTotal;
    }

    public void setPreviousPeriodTotal(Double previousPeriodTotal) {
        this.previousPeriodTotal = previousPeriodTotal;
    }
    
    public Double getChangePercent() {
        return changePercent;
    }

    public void setChangePercent(Double changePercent) {
        this.changePercent = changePercent;
    }
}
