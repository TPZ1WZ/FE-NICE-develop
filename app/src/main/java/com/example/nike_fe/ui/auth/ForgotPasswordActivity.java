package com.example.nike_fe.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nike_fe.R;
import com.example.nike_fe.data.api.AuthApi;
import com.example.nike_fe.data.api.RetrofitClient;
import com.example.nike_fe.data.model.ForgotPasswordRequest;
import com.example.nike_fe.data.model.ForgotPasswordResponse;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ForgotPasswordActivity - Màn hình quên mật khẩu
 * Backend API: POST http://10.0.2.2:8080/api/v1/auth/forgot-password-otp
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputEditText etEmail;
    private TextView tvEmailError;
    private Button btnContinue;
    private TextView tvBackToLogin;

    private AuthApi authApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize Retrofit
        authApi = RetrofitClient.getInstance(this).getAuthApi();

        // Initialize views
        etEmail = findViewById(R.id.etEmail);
        tvEmailError = findViewById(R.id.tvEmailError);
        btnContinue = findViewById(R.id.btnContinue);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        // Auto-focus email field
        etEmail.requestFocus();

        // Continue button click
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSendOtp();
            }
        });

        // Back to login link
        tvBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Return to LoginActivity
            }
        });
    }

    /**
     * Validate and send OTP request
     */
    private void attemptSendOtp() {
        // Hide previous errors
        tvEmailError.setVisibility(View.GONE);

        // Get email
        String email = etEmail.getText().toString().trim();

        // Validate email
        if (TextUtils.isEmpty(email)) {
            tvEmailError.setText("Vui lòng nhập email");
            tvEmailError.setVisibility(View.VISIBLE);
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tvEmailError.setText("Email không hợp lệ");
            tvEmailError.setVisibility(View.VISIBLE);
            etEmail.requestFocus();
            return;
        }

        // Disable button during request
        btnContinue.setEnabled(false);
        btnContinue.setText("Đang gửi...");

        // Call API
        ForgotPasswordRequest request = new ForgotPasswordRequest(email);
        Call<ForgotPasswordResponse> call = authApi.forgotPasswordOtp(request);

        call.enqueue(new Callback<ForgotPasswordResponse>() {
            @Override
            public void onResponse(Call<ForgotPasswordResponse> call, Response<ForgotPasswordResponse> response) {
                // Re-enable button
                btnContinue.setEnabled(true);
                btnContinue.setText("TIẾP TỤC");

                if (response.isSuccessful() && response.body() != null) {
                    ForgotPasswordResponse forgotResponse = response.body();

                    if (forgotResponse.isSuccess()) {
                        // Success - Navigate to OTP screen
                        Toast.makeText(ForgotPasswordActivity.this,
                                forgotResponse.getMessage(),
                                Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(ForgotPasswordActivity.this, VerifyOtpActivity.class);
                        intent.putExtra("email", email);
                        intent.putExtra("mode", "RESET_PASSWORD"); // Distinguish from registration
                        startActivity(intent);
                        finish();
                    } else {
                        // API returned success:false
                        tvEmailError.setText(forgotResponse.getMessage());
                        tvEmailError.setVisibility(View.VISIBLE);
                    }
                } else {
                    // HTTP error
                    if (response.code() == 404) {
                        tvEmailError.setText("Email không tồn tại trong hệ thống");
                        tvEmailError.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(ForgotPasswordActivity.this,
                                "Lỗi: " + response.code(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ForgotPasswordResponse> call, Throwable t) {
                // Re-enable button
                btnContinue.setEnabled(true);
                btnContinue.setText("TIẾP TỤC");

                // Network error
                Toast.makeText(ForgotPasswordActivity.this,
                        "Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        });
    }
}
