package com.example.nike_fe.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nike_fe.MainActivity;
import com.example.nike_fe.R;
import com.example.nike_fe.data.api.AuthApi;
import com.example.nike_fe.data.api.RetrofitClient;
import com.example.nike_fe.data.model.LoginRequest;
import com.example.nike_fe.data.model.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * LoginActivity - Màn hình đăng nhập
 * Kết nối với backend NICESTORE-develop
 * API: POST http://10.0.2.2:8080/api/v1/auth/login
 */
public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private TextView tvEmailError, tvPasswordError;
    private CheckBox cbRememberMe;
    private Button btnLogin, btnGoogle;
    private TextView tvForgotPassword, tvRegister;
    
    private AuthApi authApi;
    private RetrofitClient retrofitClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Khởi tạo Retrofit
        retrofitClient = RetrofitClient.getInstance(this);
        authApi = retrofitClient.getAuthApi();

        // Khởi tạo Views
        initViews();
        setupListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        tvEmailError = findViewById(R.id.tvEmailError);
        tvPasswordError = findViewById(R.id.tvPasswordError);
        cbRememberMe = findViewById(R.id.cbRememberMe);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogle = findViewById(R.id.btnGoogle);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvRegister = findViewById(R.id.tvRegister);
    }

    private void setupListeners() {
        // Nút Đăng nhập
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        // Quên mật khẩu
        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
            }
        });

        // Đăng ký
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        // Google
        btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "Đăng nhập Google Demo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void attemptLogin() {
        // Reset errors
        tvEmailError.setVisibility(View.GONE);
        tvPasswordError.setVisibility(View.GONE);

        // Lấy dữ liệu từ EditText
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate
        if (!validateInputs(email, password)) {
            return;
        }

        // Hiển thị trạng thái loading
        setLoading(true);

        // Gửi request đến backend
        LoginRequest request = new LoginRequest(email, password);
        authApi.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                setLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    // Login thành công
                    LoginResponse loginResponse = response.body();
                    
                    // Lưu token
                    retrofitClient.saveToken(loginResponse.getAccessToken());
                    
                    // Hiển thị thông báo
                    Toast.makeText(LoginActivity.this, "Chào mừng trở lại!", Toast.LENGTH_SHORT).show();
                    
                    // Chuyển sang MainActivity
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    // Xử lý lỗi từ server
                    String errorMessage = "Đăng nhập thất bại. Vui lòng thử lại.";
                    if (response.code() == 401) {
                        errorMessage = "Email hoặc mật khẩu không chính xác.";
                    } else if (response.code() == 500) {
                        errorMessage = "Lỗi server. Vui lòng thử lại sau.";
                    }
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                setLoading(false);
                
                // Xử lý lỗi kết nối
                String errorMessage = "Không thể kết nối đến server. Kiểm tra:\n" +
                        "1. Backend đang chạy tại localhost:8080\n" +
                        "2. Kết nối mạng";
                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validateInputs(String email, String password) {
        boolean isValid = true;

        // Validate Email
        if (TextUtils.isEmpty(email)) {
            tvEmailError.setText("Email không được để trống");
            tvEmailError.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tvEmailError.setText("Địa chỉ email không hợp lệ");
            tvEmailError.setVisibility(View.VISIBLE);
            isValid = false;
        }

        // Validate Password
        if (TextUtils.isEmpty(password)) {
            tvPasswordError.setText("Mật khẩu không được để trống");
            tvPasswordError.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (password.length() < 6) {
            tvPasswordError.setText("Mật khẩu phải có ít nhất 6 ký tự");
            tvPasswordError.setVisibility(View.VISIBLE);
            isValid = false;
        }

        return isValid;
    }

    private void setLoading(boolean isLoading) {
        btnLogin.setEnabled(!isLoading);
        btnLogin.setText(isLoading ? "Đang đăng nhập..." : "ĐĂNG NHẬP");
        etEmail.setEnabled(!isLoading);
        etPassword.setEnabled(!isLoading);
        btnGoogle.setEnabled(!isLoading);
    }
}
