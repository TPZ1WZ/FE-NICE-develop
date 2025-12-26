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
    private EditText etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6;
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

        etOtp1 = findViewById(R.id.etOtp1);
        etOtp2 = findViewById(R.id.etOtp2);
        etOtp3 = findViewById(R.id.etOtp3);
        etOtp4 = findViewById(R.id.etOtp4);
        etOtp5 = findViewById(R.id.etOtp5);
        etOtp6 = findViewById(R.id.etOtp6);

        btnVerify = findViewById(R.id.btnVerify);
        btnBack = findViewById(R.id.btnBack);
        tvResendOtp = findViewById(R.id.tvResendOtp);

        // Display email
        tvEmail.setText(email);

        // Setup OTP auto-focus
        setupOtpAutoFocus();

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
                // TODO: Implement resend OTP (call register-with-otp again)
                Toast.makeText(VerifyOtpActivity.this,
                        "Tính năng gửi lại OTP đang phát triển",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Auto-focus first OTP field
        etOtp1.requestFocus();
    }

    /**
     * Setup auto-focus between OTP fields
     */
    private void setupOtpAutoFocus() {
        EditText[] otpFields = { etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6 };

        for (int i = 0; i < otpFields.length; i++) {
            final int index = i;
            final EditText currentField = otpFields[i];

            // Auto-focus next field on text input
            currentField.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && index < otpFields.length - 1) {
                        otpFields[index + 1].requestFocus();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            // Handle backspace to go to previous field
            currentField.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                        if (currentField.getText().toString().isEmpty() && index > 0) {
                            otpFields[index - 1].requestFocus();
                            return true;
                        }
                    }
                    return false;
                }
            });
        }
    }

    /**
     * Attempt to verify OTP
     */
    private void attemptVerifyOtp() {
        // Hide error
        tvOtpError.setVisibility(View.GONE);

        // Get OTP from 6 fields
        String otp1 = etOtp1.getText().toString().trim();
        String otp2 = etOtp2.getText().toString().trim();
        String otp3 = etOtp3.getText().toString().trim();
        String otp4 = etOtp4.getText().toString().trim();
        String otp5 = etOtp5.getText().toString().trim();
        String otp6 = etOtp6.getText().toString().trim();

        // Validate OTP
        if (otp1.isEmpty() || otp2.isEmpty() || otp3.isEmpty() ||
                otp4.isEmpty() || otp5.isEmpty() || otp6.isEmpty()) {
            tvOtpError.setText("Vui lòng nhập đủ 6 chữ số OTP");
            tvOtpError.setVisibility(View.VISIBLE);
            return;
        }

        // Combine OTP
        String otpString = otp1 + otp2 + otp3 + otp4 + otp5 + otp6;
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
        etOtp1.setText("");
        etOtp2.setText("");
        etOtp3.setText("");
        etOtp4.setText("");
        etOtp5.setText("");
        etOtp6.setText("");
        etOtp1.requestFocus();
    }

    /**
     * Show/hide loading state
     */
    private void setLoading(boolean isLoading) {
        btnVerify.setEnabled(!isLoading);
        btnVerify.setText(isLoading ? "ĐANG XÁC THỰC..." : "XÁC THỰC");

        btnBack.setEnabled(!isLoading);
        tvResendOtp.setEnabled(!isLoading);

        etOtp1.setEnabled(!isLoading);
        etOtp2.setEnabled(!isLoading);
        etOtp3.setEnabled(!isLoading);
        etOtp4.setEnabled(!isLoading);
        etOtp5.setEnabled(!isLoading);
        etOtp6.setEnabled(!isLoading);
    }
}
