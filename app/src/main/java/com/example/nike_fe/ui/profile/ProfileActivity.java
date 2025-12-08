package com.example.nike_fe.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.nike_fe.R;
import com.example.nike_fe.data.api.RetrofitClient;
import com.example.nike_fe.data.api.UserApi;
import com.example.nike_fe.data.model.UpdateProfileRequest;
import com.example.nike_fe.data.model.User;
import com.example.nike_fe.ui.auth.LoginActivity;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {
    
    private ImageView ivAvatar;
    private TextView tvFullName, tvEmail, tvEmailReadonly, tvPhone, tvRole, tvChangeAvatar, tvEditProfile, tvViewAllOrders;
    private TextInputEditText etFullName;
    private Button btnUpdateProfile, btnChangePassword, btnLogout;
    private LinearLayout btnOrderHistory, btnAdminPanel, layoutAdminSection;
    private View dividerAdminSection;
    private FrameLayout layoutLoading;
    private LinearLayout layoutProfileContent;
    
    private UserApi userApi;
    private String token;
    private User currentUser;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        
        setupImagePicker();
        initViews();
        setupToolbar();
        loadProfile();
    }
    
    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        Glide.with(this)
                            .load(imageUri)
                            .circleCrop()
                            .placeholder(R.drawable.ic_heart)
                            .error(R.drawable.ic_heart)
                            .into(ivAvatar);
                        Toast.makeText(this, "Đã chọn ảnh đại diện", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        );
    }
    
    private void initViews() {
        ivAvatar = findViewById(R.id.ivAvatar);
        tvChangeAvatar = findViewById(R.id.tvChangeAvatar);
        tvFullName = findViewById(R.id.tvFullName);
        tvEmail = findViewById(R.id.tvEmail);
        tvEmailReadonly = findViewById(R.id.tvEmailReadonly);
        tvPhone = findViewById(R.id.tvPhone);
        tvRole = findViewById(R.id.tvRole);
        tvEditProfile = findViewById(R.id.tvEditProfile);
        tvViewAllOrders = findViewById(R.id.tvViewAllOrders);
        etFullName = findViewById(R.id.etFullName);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnLogout = findViewById(R.id.btnLogout);
        btnOrderHistory = findViewById(R.id.btnOrderHistory);
        btnAdminPanel = findViewById(R.id.btnAdminPanel);
        layoutAdminSection = findViewById(R.id.layoutAdminSection);
        dividerAdminSection = findViewById(R.id.dividerAdminSection);
        layoutLoading = findViewById(R.id.layoutLoading);
        layoutProfileContent = findViewById(R.id.layoutProfileContent);
        
        RetrofitClient retrofitClient = RetrofitClient.getInstance(this);
        userApi = retrofitClient.getUserApi();
        token = retrofitClient.getToken();
        
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return;
        }
        
        setupAvatarClick();
    }
    
    private void setupAvatarClick() {
        View.OnClickListener pickImageListener = v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        };
        
        ivAvatar.setOnClickListener(pickImageListener);
        tvChangeAvatar.setOnClickListener(pickImageListener);
    }
    
    private void setupToolbar() {
        btnUpdateProfile.setOnClickListener(v -> updateProfile());
        
        btnChangePassword.setOnClickListener(v -> {
            // TODO: Navigate to ChangePasswordActivity
            Toast.makeText(this, "Chức năng đổi mật khẩu đang phát triển", Toast.LENGTH_SHORT).show();
        });
        
        btnOrderHistory.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, com.example.nike_fe.ui.order.OrderHistoryActivity.class);
            startActivity(intent);
        });
        
        tvViewAllOrders.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, com.example.nike_fe.ui.order.OrderHistoryActivity.class);
            startActivity(intent);
        });
        
        tvEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivityForResult(intent, 100);
        });
        
        btnAdminPanel.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, com.example.nike_fe.ui.admin.AdminDashboardActivity.class);
            startActivity(intent);
        });
        
        btnLogout.setOnClickListener(v -> showLogoutConfirmation());
    }
    
    private void loadProfile() {
        layoutLoading.setVisibility(View.VISIBLE);
        layoutProfileContent.setVisibility(View.GONE);
        
        userApi.getProfile("Bearer " + token).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                layoutLoading.setVisibility(View.GONE);
                layoutProfileContent.setVisibility(View.VISIBLE);
                
                if (response.isSuccessful() && response.body() != null) {
                    currentUser = response.body();
                    displayProfile(currentUser);
                } else if (response.code() == 401) {
                    Toast.makeText(ProfileActivity.this, 
                            "Phiên đăng nhập hết hạn", Toast.LENGTH_SHORT).show();
                    navigateToLogin();
                } else {
                    Toast.makeText(ProfileActivity.this, 
                            "Lỗi tải thông tin: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                layoutLoading.setVisibility(View.GONE);
                layoutProfileContent.setVisibility(View.VISIBLE);
                Toast.makeText(ProfileActivity.this, 
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void displayProfile(User user) {
        tvFullName.setText(user.getFullName());
        tvEmail.setText(user.getEmail());
        tvEmailReadonly.setText(user.getEmail());
        etFullName.setText(user.getFullName());
        
        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            tvPhone.setText(user.getPhone());
        } else {
            tvPhone.setText("Chưa cập nhật");
        }
        
        tvRole.setText(user.getRole());
        
        // Show admin section if user is ADMIN
        if ("ADMIN".equalsIgnoreCase(user.getRole()) || "ROOT".equalsIgnoreCase(user.getRole())) {
            layoutAdminSection.setVisibility(View.VISIBLE);
            dividerAdminSection.setVisibility(View.VISIBLE);
        } else {
            layoutAdminSection.setVisibility(View.GONE);
            dividerAdminSection.setVisibility(View.GONE);
        }
    }
    
    private void updateProfile() {
        String newFullName = etFullName.getText().toString().trim();
        
        if (newFullName.isEmpty()) {
            etFullName.setError("Vui lòng nhập họ tên");
            return;
        }
        
        if (currentUser != null && newFullName.equals(currentUser.getFullName())) {
            Toast.makeText(this, "Không có thay đổi nào", Toast.LENGTH_SHORT).show();
            return;
        }
        
        btnUpdateProfile.setEnabled(false);
        btnUpdateProfile.setText("Đang cập nhật...");
        
        UpdateProfileRequest request = new UpdateProfileRequest(newFullName);
        
        userApi.updateProfile("Bearer " + token, request).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                btnUpdateProfile.setEnabled(true);
                btnUpdateProfile.setText("Cập nhật thông tin");
                
                if (response.isSuccessful() && response.body() != null) {
                    currentUser = response.body();
                    displayProfile(currentUser);
                    Toast.makeText(ProfileActivity.this, 
                            "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this, 
                            "Lỗi cập nhật: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                btnUpdateProfile.setEnabled(true);
                btnUpdateProfile.setText("Cập nhật thông tin");
                Toast.makeText(ProfileActivity.this, 
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> logout())
                .setNegativeButton("Hủy", null)
                .show();
    }
    
    private void logout() {
        RetrofitClient.getInstance(this).clearToken();
        navigateToLogin();
        Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
    }
    
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            // Reload profile after edit
            loadProfile();
        }
    }
}
