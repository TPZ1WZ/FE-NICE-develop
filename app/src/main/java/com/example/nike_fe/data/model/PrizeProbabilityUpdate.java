package com.example.nike_fe.data.model;

import com.google.gson.annotations.SerializedName;

public class PrizeProbabilityUpdate {

    @SerializedName("prizeId")
    private Long prizeId;

    @SerializedName("probability")
    private Double probability;

    public PrizeProbabilityUpdate(Long prizeId, Double probability) {
        this.prizeId = prizeId;
        this.probability = probability;
    }

    public Long getPrizeId() {
        return prizeId;
    }

    public void setPrizeId(Long prizeId) {
        this.prizeId = prizeId;
    }

    public Double getProbability() {
        return probability;
    }

    public void setProbability(Double probability) {
        this.probability = probability;
    }
}
