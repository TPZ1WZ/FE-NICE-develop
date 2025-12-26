package com.example.nike_fe.data.api;

import com.example.nike_fe.data.model.ForgotPasswordRequest;
import com.example.nike_fe.data.model.ForgotPasswordResponse;
import com.example.nike_fe.data.model.LoginRequest;
import com.example.nike_fe.data.model.LoginResponse;
import com.example.nike_fe.data.model.RegisterRequest;
import com.example.nike_fe.data.model.RegisterResponse;
import com.example.nike_fe.data.model.ResetPasswordWithOtpRequest;
import com.example.nike_fe.data.model.VerifyOtpRequest;
import com.example.nike_fe.data.model.VerifyOtpResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * AuthApi - API endpoints từ backend NICESTORE-develop
 * Base URL: http://10.0.2.2:8080/api/v1/auth
 */
public interface AuthApi {

    /**
     * POST /api/v1/auth/login
     * Request: { "username": "email@example.com", "password": "123456789" }
     * Response: { "access_token": "...", "token_type": "Bearer", "expires_in": 3600
     * }
     */
    @POST("api/v1/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    /**
     * POST /api/v1/auth/register
     * Request: { "fullName": "Nguyen Van A", "email": "email@example.com", "phone":
     * "0123456789", "password": "123456" }
     * Response: { "success": true, "message": "User registered successfully...",
     * "email": "email@example.com" }
     */
    @POST("api/v1/auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    /**
     * POST /api/v1/auth/register-with-otp
     * Request: { "fullName": "Nguyen Van A", "email": "email@example.com", "phone":
     * "0123456789", "password": "123456" }
     * Response: { "success": true, "message": "OTP sent to your email...",
     * "email": "email@example.com" }
     */
    @POST("api/v1/auth/register-with-otp")
    Call<RegisterResponse> registerWithOtp(@Body RegisterRequest request);

    /**
     * POST /api/v1/auth/verify-registration-otp
     * Request: { "email": "email@example.com", "otp": 123456 }
     * Response: { "success": true, "message": "Registration completed...",
     * "email": "email@example.com" }
     */
    @POST("api/v1/auth/verify-registration-otp")
    Call<VerifyOtpResponse> verifyRegistrationOtp(@Body VerifyOtpRequest request);

    // ============================================
    // 🔐 Forgot Password APIs
    // ============================================

    /**
     * POST /api/v1/auth/forgot-password-otp
     * Request: { "email": "email@example.com" }
     * Response: { "success": true, "message": "OTP sent...", "email": "..." }
     */
    @POST("api/v1/auth/forgot-password-otp")
    Call<ForgotPasswordResponse> forgotPasswordOtp(@Body ForgotPasswordRequest request);

    /**
     * POST /api/v1/auth/verify-password-reset-otp
     * Request: { "email": "email@example.com", "otp": 123456 }
     * Response: { "success": true, "message": "OTP verified...", "email": "..." }
     */
    @POST("api/v1/auth/verify-password-reset-otp")
    Call<VerifyOtpResponse> verifyPasswordResetOtp(@Body VerifyOtpRequest request);

    /**
     * POST /api/v1/auth/reset-password-with-otp
     * Request: { "email": "email@example.com", "otp": 123456, "newPassword": "..." }
     * Response: { "success": true, "message": "Password reset...", "email": "..." }
     */
    @POST("api/v1/auth/reset-password-with-otp")
    Call<VerifyOtpResponse> resetPasswordWithOtp(@Body ResetPasswordWithOtpRequest request);
}
