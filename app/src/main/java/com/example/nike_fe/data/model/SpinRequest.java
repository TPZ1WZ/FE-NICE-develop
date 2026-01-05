package com.example.nike_fe.data.model;

public class SpinRequest {
    private Boolean useFreeSpin;

    public SpinRequest(Boolean useFreeSpin) {
        this.useFreeSpin = useFreeSpin;
    }

    public Boolean getUseFreeSpin() {
        return useFreeSpin;
    }

    public void setUseFreeSpin(Boolean useFreeSpin) {
        this.useFreeSpin = useFreeSpin;
    }
}
