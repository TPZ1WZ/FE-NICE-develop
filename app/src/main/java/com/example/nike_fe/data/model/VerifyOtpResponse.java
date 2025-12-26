package com.example.nike_fe.data.model;

public class VerifyOtpResponse {
    private boolean success;
    private String message;
    private String email;

    public VerifyOtpResponse() {
    }

    public VerifyOtpResponse(boolean success, String message, String email) {
        this.success = success;
        this.message = message;
        this.email = email;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
