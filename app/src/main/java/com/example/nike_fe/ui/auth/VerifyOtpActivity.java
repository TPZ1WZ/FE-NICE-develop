package com.example.nike_fe.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nike_fe.R;
import com.example.nike_fe.data.api.AuthApi;
import com.example.nike_fe.data.api.RetrofitClient;
import com.example.nike_fe.data.model.VerifyOtpRequest;
import com.example.nike_fe.data.model.VerifyOtpResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * VerifyOtpActivity - Màn hình xác thực OTP sau đăng ký
 * Backend API: POST http://10.0.2.2:8080/api/v1/auth/verify-registration-otp
 */
public class VerifyOtpActivity extends AppCompatActivity {

    private TextView tvEmail, tvOtpError;
    private EditText etOtp; // Single OTP field
    private Button btnVerify, btnBack;
    private TextView tvResendOtp;

    private AuthApi authApi;
    private String email;
    private String mode; // "REGISTER" or "RESET_PASSWORD"
    private long currentOtp; // Store OTP for reset password flow

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        // Initialize Retrofit
        authApi = RetrofitClient.getInstance(this).getAuthApi();

        // Get email from Intent
        email = getIntent().getStringExtra("email");
        mode = getIntent().getStringExtra("mode");
        
        // Default to REGISTER if mode not specified
        if (mode == null || mode.isEmpty()) {
            mode = "REGISTER";
        }
        
        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy email", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        tvEmail = findViewById(R.id.tvEmail);
        tvOtpError = findViewById(R.id.tvOtpError);
        etOtp = findViewById(R.id.etOtp); // Single field
        
        btnVerify = findViewById(R.id.btnVerify);
        btnBack = findViewById(R.id.btnBack);
        tvResendOtp = findViewById(R.id.tvResendOtp);

        // Display email
        tvEmail.setText(email);

        // Verify button click
        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptVerifyOtp();
            }
        });

        // Back button click
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Return to RegisterActivity
            }
        });

        // Resend OTP link
        tvResendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (System.currentTimeMillis() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = System.currentTimeMillis();
                resendOtp();
            }
        });

        // Start countdown timer immediately
        startResendTimer(30000);

        // Auto-focus OTP field
        etOtp.requestFocus();
    }

    private void resendOtp() {
        setLoading(true);
        
        java.util.Map<String, String> request = new java.util.HashMap<>();
        request.put("email", email);

        authApi.resendRegistrationOtp(request).enqueue(new Callback<com.example.nike_fe.data.model.RegisterResponse>() {
            @Override
            public void onResponse(Call<com.example.nike_fe.data.model.RegisterResponse> call, Response<com.example.nike_fe.data.model.RegisterResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(VerifyOtpActivity.this, "Mã OTP mới đã được gửi!", Toast.LENGTH_LONG).show();
                    startResendTimer(30000);
                } else {
                    String message = "Gửi lại thất bại. Vui lòng thử lại.";
                    if (response.code() == 409) {
                        message = "Email đã đăng ký. Vui lòng đăng nhập.";
                    }
                    Toast.makeText(VerifyOtpActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<com.example.nike_fe.data.model.RegisterResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(VerifyOtpActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private android.os.CountDownTimer resendTimer;
    private long mLastClickTime = 0;

    private void startResendTimer(long durationMillis) {
        if (resendTimer != null) {
            resendTimer.cancel();
        }

        tvResendOtp.setEnabled(false);
        tvResendOtp.setTextColor(android.graphics.Color.WHITE); // Ensure white text
        
        resendTimer = new android.os.CountDownTimer(durationMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                String timeString = String.format("00:%02d", seconds);
                tvResendOtp.setText("Gửi lại mã OTP (" + timeString + ")");
            }

            @Override
            public void onFinish() {
                tvResendOtp.setEnabled(true);
                tvResendOtp.setText("Gửi lại mã OTP");
            }
        }.start();
    }

    /**
     * Attempt to verify OTP
     */
    private void attemptVerifyOtp() {
        // Hide error
        tvOtpError.setVisibility(View.GONE);

        // Get OTP from single field
        String otpString = etOtp.getText().toString().trim();

        // Validate OTP
        if (otpString.length() != 6) {
            tvOtpError.setText("Vui lòng nhập đủ 6 chữ số OTP");
            tvOtpError.setVisibility(View.VISIBLE);
            return;
        }

        long otp;
        try {
            otp = Long.parseLong(otpString);
        } catch (NumberFormatException e) {
            tvOtpError.setText("Mã OTP không hợp lệ");
            tvOtpError.setVisibility(View.VISIBLE);
            return;
        }

        // Show loading
        setLoading(true);

        // Create request
        VerifyOtpRequest request = new VerifyOtpRequest(email, otp);
        currentOtp = otp; // Store for reset password flow

        // Choose API endpoint based on mode
        Call<VerifyOtpResponse> call;
        if ("RESET_PASSWORD".equals(mode)) {
            call = authApi.verifyPasswordResetOtp(request);
        } else {
            call = authApi.verifyRegistrationOtp(request);
        }

        // Call API
        call.enqueue(new Callback<VerifyOtpResponse>() {
            @Override
            public void onResponse(Call<VerifyOtpResponse> call, Response<VerifyOtpResponse> response) {
                setLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    VerifyOtpResponse verifyResponse = response.body();

                    if (verifyResponse.isSuccess()) {
                        // Success
                        if ("RESET_PASSWORD".equals(mode)) {
                            // Navigate to Reset Password screen
                            Toast.makeText(VerifyOtpActivity.this,
                                    "Xác thực thành công! Vui lòng nhập mật khẩu mới.",
                                    Toast.LENGTH_LONG).show();

                            Intent intent = new Intent(VerifyOtpActivity.this, ResetPasswordActivity.class);
                            intent.putExtra("email", email);
                            intent.putExtra("otp", currentOtp);
                            startActivity(intent);
                            finish();
                        } else {
                            // Navigate to Login (Register flow)
                            Toast.makeText(VerifyOtpActivity.this,
                                    "Xác thực thành công! Vui lòng đăng nhập.",
                                    Toast.LENGTH_LONG).show();

                            Intent intent = new Intent(VerifyOtpActivity.this, LoginActivity.class);
                            intent.putExtra("registered_email", email);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        // Invalid OTP
                        tvOtpError.setText(verifyResponse.getMessage());
                        tvOtpError.setVisibility(View.VISIBLE);
                        clearOtpFields();
                    }
                } else {
                    // HTTP error
                    if (response.code() == 400) {
                        tvOtpError.setText("Mã OTP không đúng hoặc đã hết hạn");
                    } else {
                        tvOtpError.setText("Xác thực thất bại. Vui lòng thử lại.");
                    }
                    tvOtpError.setVisibility(View.VISIBLE);
                    clearOtpFields();
                }
            }

            @Override
            public void onFailure(Call<VerifyOtpResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(VerifyOtpActivity.this,
                        "Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Clear all OTP fields
     */
    private void clearOtpFields() {
        etOtp.setText("");
        etOtp.requestFocus();
    }

    /**
     * Show/hide loading state
     */
    private void setLoading(boolean isLoading) {
        btnVerify.setEnabled(!isLoading);
        btnVerify.setText(isLoading ? "ĐANG XÁC THỰC..." : "XÁC THỰC");

        btnBack.setEnabled(!isLoading);
        tvResendOtp.setEnabled(!isLoading);

        etOtp.setEnabled(!isLoading);
    }
}
