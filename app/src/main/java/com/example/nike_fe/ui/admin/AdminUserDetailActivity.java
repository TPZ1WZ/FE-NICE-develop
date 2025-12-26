package com.example.nike_fe.ui.admin;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.nike_fe.R;
import com.example.nike_fe.data.api.AdminApi;
import com.example.nike_fe.data.api.RetrofitClient;
import com.example.nike_fe.data.model.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminUserDetailActivity extends AppCompatActivity {

    private ImageView ivBack, ivUserAvatar;
    private TextView tvUserName, tvUserEmail, tvUserPhone, tvUserAddress, tvUserCreatedAt;
    private Chip chipRole, chipStatus;
    private MaterialButton btnChangeRole, btnToggleStatus;
    private FrameLayout layoutLoading;

    private AdminApi adminApi;
    private String token;
    private Long userId;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_detail);

        userId = getIntent().getLongExtra("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadUserDetails();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        ivUserAvatar = findViewById(R.id.ivUserAvatar);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvUserPhone = findViewById(R.id.tvUserPhone);
        tvUserAddress = findViewById(R.id.tvUserAddress);
        tvUserCreatedAt = findViewById(R.id.tvUserCreatedAt);
        chipRole = findViewById(R.id.chipRole);
        chipStatus = findViewById(R.id.chipStatus);
        btnChangeRole = findViewById(R.id.btnChangeRole);
        btnToggleStatus = findViewById(R.id.btnToggleStatus);
        layoutLoading = findViewById(R.id.layoutLoading);

        RetrofitClient retrofitClient = RetrofitClient.getInstance(this);
        adminApi = retrofitClient.getAdminApi();
        token = retrofitClient.getToken();

        ivBack.setOnClickListener(v -> finish());

        btnChangeRole.setOnClickListener(v -> showChangeRoleDialog());
        btnToggleStatus.setOnClickListener(v -> showToggleStatusDialog());
    }

    private void loadUserDetails() {
        layoutLoading.setVisibility(View.VISIBLE);
        adminApi.getUserById("Bearer " + token, userId).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                layoutLoading.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    currentUser = response.body();
                    android.util.Log.d("UserDetail", "Parsed User: " + currentUser.toString());
                    android.util.Log.d("UserDetail", "Role: " + currentUser.getRole() + ", Active: "
                            + currentUser.getIsActive() + ", Created: " + currentUser.getCreatedAt());
                    displayUserInfo(currentUser);
                } else {
                    Toast.makeText(AdminUserDetailActivity.this, "Lỗi tải thông tin: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                layoutLoading.setVisibility(View.GONE);
                Toast.makeText(AdminUserDetailActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayUserInfo(User user) {
        if (user == null) {
            Toast.makeText(this, "Lỗi: Không có dữ liệu người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        String avatarUrl = user.getAvatarUrl();
        if (avatarUrl != null && !avatarUrl.startsWith("http")) {
            avatarUrl = RetrofitClient.getInstance(this).getBaseUrl() + avatarUrl;
        }

        Glide.with(this)
                .load(avatarUrl)
                .placeholder(R.drawable.ic_avatar_placeholder)
                .error(R.drawable.ic_avatar_placeholder)
                .into(ivUserAvatar);

        tvUserName.setText(user.getFullName() != null ? user.getFullName() : "Chưa cập nhật");
        tvUserEmail.setText(user.getEmail() != null ? user.getEmail() : "Chưa cập nhật");
        tvUserPhone.setText(user.getPhone() != null ? user.getPhone() : "Chưa cập nhật");
        tvUserAddress.setText(user.getAddress() != null ? user.getAddress() : "Chưa cập nhật");
        tvUserCreatedAt.setText("Tham gia: " + (user.getCreatedAt() != null ? user.getCreatedAt() : "N/A"));

        // Role
        String role = user.getRole() != null ? user.getRole() : "MEMBER";
        if ("ADMIN".equalsIgnoreCase(role)) {
            chipRole.setText("ADMIN");
            chipRole.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#DBEAFE")));
            chipRole.setTextColor(Color.parseColor("#1D4ED8"));
            btnChangeRole.setText("Hạ cấp xuống MEMBER");
        } else {
            chipRole.setText("MEMBER");
            chipRole.setChipBackgroundColorResource(android.R.color.transparent);
            chipRole.setTextColor(Color.parseColor("#374151"));
            btnChangeRole.setText("Thăng cấp lên ADMIN");
        }

        // Status
        boolean isActive = Boolean.TRUE.equals(user.getIsActive());
        if (isActive) {
            chipStatus.setText("Active");
            chipStatus.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#DCFCE7")));
            chipStatus.setTextColor(Color.parseColor("#166534"));

            btnToggleStatus.setText("Khóa Tài Khoản");
            btnToggleStatus.setTextColor(Color.parseColor("#B91C1C"));
            btnToggleStatus.setStrokeColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#B91C1C")));
            btnToggleStatus
                    .setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FEE2E2")));
        } else {
            chipStatus.setText("Inactive");
            chipStatus.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#FEE2E2")));
            chipStatus.setTextColor(Color.parseColor("#B91C1C"));

            btnToggleStatus.setText("Mở Khóa Tài Khoản");
            btnToggleStatus.setTextColor(Color.parseColor("#166534"));
            btnToggleStatus.setStrokeColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#166534")));
            btnToggleStatus
                    .setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#DCFCE7")));
        }
    }

    private void showChangeRoleDialog() {
        if (currentUser == null)
            return;

        String currentRole = currentUser.getRole();
        String newRole = "ADMIN".equalsIgnoreCase(currentRole) ? "MEMBER" : "ADMIN";
        String message = "Bạn có chắc chắn muốn đổi quyền user này thành " + newRole + "?";

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận đổi quyền")
                .setMessage(message)
                .setPositiveButton("Đồng ý", (dialog, which) -> updateRole(newRole))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void updateRole(String newRole) {
        if (currentUser == null)
            return;
        currentUser.setRole(newRole);
        callUpdateUser();
    }

    private void showToggleStatusDialog() {
        if (currentUser == null)
            return;

        boolean isCurrentlyActive = Boolean.TRUE.equals(currentUser.getIsActive());
        String action = isCurrentlyActive ? "KHÓA" : "MỞ KHÓA";
        String message = "Bạn có chắc chắn muốn " + action + " tài khoản này?";

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận " + action)
                .setMessage(message)
                .setPositiveButton("Đồng ý", (dialog, which) -> updateStatus(!isCurrentlyActive))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void updateStatus(boolean newStatus) {
        if (currentUser == null)
            return;
        currentUser.setIsActive(newStatus);
        callUpdateUser();
    }

    private void callUpdateUser() {
        layoutLoading.setVisibility(View.VISIBLE);
        adminApi.updateUser("Bearer " + token, userId, currentUser).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminUserDetailActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    // Reload user details để lấy đầy đủ thông tin từ server
                    loadUserDetails();
                } else {
                    layoutLoading.setVisibility(View.GONE);
                    Toast.makeText(AdminUserDetailActivity.this, "Lỗi cập nhật: " + response.code(), Toast.LENGTH_SHORT)
                            .show();
                    // Reload to reset local changes if failed
                    loadUserDetails();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                layoutLoading.setVisibility(View.GONE);
                Toast.makeText(AdminUserDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                // Reload to reset local changes if failed
                loadUserDetails();
            }
        });
    }
}
