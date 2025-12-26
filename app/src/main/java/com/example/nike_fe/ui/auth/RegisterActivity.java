package com.example.nike_fe.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nike_fe.R;
import com.example.nike_fe.data.api.AuthApi;
import com.example.nike_fe.data.api.RetrofitClient;
import com.example.nike_fe.data.model.RegisterRequest;
import com.example.nike_fe.data.model.RegisterResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * RegisterActivity - Màn hình đăng ký tài khoản
 * Chuyển từ React RegisterForm.tsx sang Android
 * 
 * Backend API: POST http://10.0.2.2:8080/api/v1/auth/register
 * Request: { fullName, email, phone, password }
 * Response: { success, message, email }
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPhone, etPassword, etConfirmPassword;
    private TextView tvFullNameError, tvEmailError, tvPhoneError, tvPasswordError, tvConfirmPasswordError;
    private Button btnRegister;
    private TextView tvLoginLink;

    private AuthApi authApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Retrofit
        authApi = RetrofitClient.getInstance(this).getAuthApi();

        // Ánh xạ views
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        tvFullNameError = findViewById(R.id.tvFullNameError);
        tvEmailError = findViewById(R.id.tvEmailError);
        tvPhoneError = findViewById(R.id.tvPhoneError);
        tvPasswordError = findViewById(R.id.tvPasswordError);
        tvConfirmPasswordError = findViewById(R.id.tvConfirmPasswordError);

        btnRegister = findViewById(R.id.btnRegister);
        tvLoginLink = findViewById(R.id.tvLoginLink);

        // Xử lý sự kiện nút đăng ký
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptRegister();
            }
        });

        // Xử lý link "Đăng nhập"
        tvLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    /**
     * Thực hiện đăng ký
     */
    private void attemptRegister() {
        // Reset errors
        hideAllErrors();

        // Lấy dữ liệu từ form
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        // Validate inputs
        if (!validateInputs(fullName, email, phone, password, confirmPassword)) {
            return;
        }

        // Hiển thị loading state
        setLoading(true);

        // Tạo request object
        RegisterRequest request = new RegisterRequest(fullName, email, phone, password);

        // Gọi API đăng ký VỚI OTP
        authApi.registerWithOtp(request).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                setLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    RegisterResponse registerResponse = response.body();

                    if (registerResponse.isSuccess()) {
                        // Đăng ký thành công - OTP đã gửi
                        Toast.makeText(RegisterActivity.this,
                                "OTP đã được gửi đến email của bạn!",
                                Toast.LENGTH_LONG).show();

                        // Chuyển đến màn OTP Verification
                        Intent intent = new Intent(RegisterActivity.this, VerifyOtpActivity.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                        finish();
                    } else {
                        // Backend trả success = false
                        Toast.makeText(RegisterActivity.this,
                                registerResponse.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    // HTTP error (400, 409, 500...)
                    handleErrorResponse(response.code());
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(RegisterActivity.this,
                        "Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Validate tất cả các trường input
     */
    private boolean validateInputs(String fullName, String email, String phone,
            String password, String confirmPassword) {
        boolean isValid = true;

        // Validate Full Name
        if (fullName.isEmpty()) {
            tvFullNameError.setText("Họ và tên không được để trống");
            tvFullNameError.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (fullName.length() < 2) {
            tvFullNameError.setText("Tên phải có ít nhất 2 ký tự");
            tvFullNameError.setVisibility(View.VISIBLE);
            isValid = false;
        }

        // Validate Email
        if (email.isEmpty()) {
            tvEmailError.setText("Email không được để trống");
            tvEmailError.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tvEmailError.setText("Địa chỉ email không hợp lệ");
            tvEmailError.setVisibility(View.VISIBLE);
            isValid = false;
        }

        // Validate Phone
        if (phone.isEmpty()) {
            tvPhoneError.setText("Số điện thoại không được để trống");
            tvPhoneError.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (phone.length() < 10 || phone.length() > 15) {
            tvPhoneError.setText("Số điện thoại phải có 10-15 ký tự");
            tvPhoneError.setVisibility(View.VISIBLE);
            isValid = false;
        }

        // Validate Password
        if (password.isEmpty()) {
            tvPasswordError.setText("Mật khẩu không được để trống");
            tvPasswordError.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (password.length() < 6) {
            tvPasswordError.setText("Mật khẩu phải có ít nhất 6 ký tự");
            tvPasswordError.setVisibility(View.VISIBLE);
            isValid = false;
        }

        // Validate Confirm Password
        if (confirmPassword.isEmpty()) {
            tvConfirmPasswordError.setText("Xác nhận mật khẩu không được để trống");
            tvConfirmPasswordError.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            tvConfirmPasswordError.setText("Mật khẩu không khớp");
            tvConfirmPasswordError.setVisibility(View.VISIBLE);
            isValid = false;
        }

        return isValid;
    }

    /**
     * Ẩn tất cả thông báo lỗi
     */
    private void hideAllErrors() {
        tvFullNameError.setVisibility(View.GONE);
        tvEmailError.setVisibility(View.GONE);
        tvPhoneError.setVisibility(View.GONE);
        tvPasswordError.setVisibility(View.GONE);
        tvConfirmPasswordError.setVisibility(View.GONE);
    }

    /**
     * Xử lý lỗi HTTP response
     */
    private void handleErrorResponse(int statusCode) {
        String message;
        switch (statusCode) {
            case 400:
                message = "Dữ liệu không hợp lệ. Vui lòng kiểm tra lại.";
                break;
            case 409:
                message = "Email đã được đăng ký. Vui lòng sử dụng email khác.";
                break;
            case 500:
                message = "Lỗi server. Vui lòng thử lại sau.";
                break;
            default:
                message = "Đăng ký thất bại. Vui lòng thử lại.";
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Hiển thị/ẩn loading state
     */
    private void setLoading(boolean isLoading) {
        btnRegister.setEnabled(!isLoading);
        btnRegister.setText(isLoading ? "ĐANG TẠO TÀI KHOẢN..." : "TẠO TÀI KHOẢN");

        // Disable tất cả các EditText khi đang loading
        etFullName.setEnabled(!isLoading);
        etEmail.setEnabled(!isLoading);
        etPhone.setEnabled(!isLoading);
        etPassword.setEnabled(!isLoading);
        etConfirmPassword.setEnabled(!isLoading);
    }
}
