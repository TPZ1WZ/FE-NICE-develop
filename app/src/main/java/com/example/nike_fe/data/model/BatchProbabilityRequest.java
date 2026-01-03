package com.example.nike_fe.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class BatchProbabilityRequest {

    @SerializedName("prizes")
    private List<PrizeProbabilityUpdate> prizes;

    public BatchProbabilityRequest(List<PrizeProbabilityUpdate> prizes) {
        this.prizes = prizes;
    }

    public List<PrizeProbabilityUpdate> getPrizes() {
        return prizes;
    }

    public void setPrizes(List<PrizeProbabilityUpdate> prizes) {
        this.prizes = prizes;
    }
}
