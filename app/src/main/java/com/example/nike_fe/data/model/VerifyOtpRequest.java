package com.example.nike_fe.data.model;

public class VerifyOtpRequest {
    private String email;
    private long otp;

    public VerifyOtpRequest(String email, long otp) {
        this.email = email;
        this.otp = otp;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getOtp() {
        return otp;
    }

    public void setOtp(long otp) {
        this.otp = otp;
    }
}
