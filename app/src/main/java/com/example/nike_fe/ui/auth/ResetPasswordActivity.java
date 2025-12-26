package com.example.nike_fe.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nike_fe.R;
import com.example.nike_fe.data.api.AuthApi;
import com.example.nike_fe.data.api.RetrofitClient;
import com.example.nike_fe.data.model.ResetPasswordWithOtpRequest;
import com.example.nike_fe.data.model.VerifyOtpResponse;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ResetPasswordActivity - Màn hình đặt lại mật khẩu mới
 * Backend API: POST http://10.0.2.2:8080/api/v1/auth/reset-password-with-otp
 */
public class ResetPasswordActivity extends AppCompatActivity {

    private TextInputEditText etNewPassword, etConfirmPassword;
    private TextView tvNewPasswordError, tvConfirmPasswordError;
    private Button btnReset;

    private AuthApi authApi;
    private String email;
    private long otp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // Initialize Retrofit
        authApi = RetrofitClient.getInstance(this).getAuthApi();

        // Get data from Intent
        email = getIntent().getStringExtra("email");
        otp = getIntent().getLongExtra("otp", 0);

        if (email == null || email.isEmpty() || otp == 0) {
            Toast.makeText(this, "Lỗi: Thông tin không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        tvNewPasswordError = findViewById(R.id.tvNewPasswordError);
        tvConfirmPasswordError = findViewById(R.id.tvConfirmPasswordError);
        btnReset = findViewById(R.id.btnReset);

        // Auto-focus new password field
        etNewPassword.requestFocus();

        // Reset button click
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptResetPassword();
            }
        });
    }

    /**
     * Validate and reset password
     */
    private void attemptResetPassword() {
        // Hide previous errors
        tvNewPasswordError.setVisibility(View.GONE);
        tvConfirmPasswordError.setVisibility(View.GONE);

        // Get passwords
        String newPassword = etNewPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        // Validate new password
        if (TextUtils.isEmpty(newPassword)) {
            tvNewPasswordError.setText("Vui lòng nhập mật khẩu mới");
            tvNewPasswordError.setVisibility(View.VISIBLE);
            etNewPassword.requestFocus();
            return;
        }

        if (newPassword.length() < 6) {
            tvNewPasswordError.setText("Mật khẩu phải có ít nhất 6 ký tự");
            tvNewPasswordError.setVisibility(View.VISIBLE);
            etNewPassword.requestFocus();
            return;
        }

        // Validate confirm password
        if (TextUtils.isEmpty(confirmPassword)) {
            tvConfirmPasswordError.setText("Vui lòng xác nhận mật khẩu");
            tvConfirmPasswordError.setVisibility(View.VISIBLE);
            etConfirmPassword.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            tvConfirmPasswordError.setText("Mật khẩu xác nhận không khớp");
            tvConfirmPasswordError.setVisibility(View.VISIBLE);
            etConfirmPassword.requestFocus();
            return;
        }

        // Disable button during request
        btnReset.setEnabled(false);
        btnReset.setText("ĐANG ĐẶT LẠI...");

        // Call API
        ResetPasswordWithOtpRequest request = new ResetPasswordWithOtpRequest(email, otp, newPassword);
        Call<VerifyOtpResponse> call = authApi.resetPasswordWithOtp(request);

        call.enqueue(new Callback<VerifyOtpResponse>() {
            @Override
            public void onResponse(Call<VerifyOtpResponse> call, Response<VerifyOtpResponse> response) {
                // Re-enable button
                btnReset.setEnabled(true);
                btnReset.setText("ĐẶT LẠI MẬT KHẨU");

                if (response.isSuccessful() && response.body() != null) {
                    VerifyOtpResponse resetResponse = response.body();

                    if (resetResponse.isSuccess()) {
                        // Success - Navigate to Login
                        Toast.makeText(ResetPasswordActivity.this,
                                "Đặt lại mật khẩu thành công! Vui lòng đăng nhập.",
                                Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                        intent.putExtra("reset_success", true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        // API returned success:false
                        Toast.makeText(ResetPasswordActivity.this,
                                resetResponse.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    // HTTP error
                    Toast.makeText(ResetPasswordActivity.this,
                            "Đặt lại mật khẩu thất bại. Vui lòng thử lại.",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<VerifyOtpResponse> call, Throwable t) {
                // Re-enable button
                btnReset.setEnabled(true);
                btnReset.setText("ĐẶT LẠI MẬT KHẨU");

                // Network error
                Toast.makeText(ResetPasswordActivity.this,
                        "Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        });
    }
}
