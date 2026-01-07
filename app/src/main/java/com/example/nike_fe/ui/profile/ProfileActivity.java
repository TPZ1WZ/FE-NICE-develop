package com.example.nike_fe.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.example.nike_fe.ui.admin.AdminDashboardActivity;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private CircleImageView ivAvatar;
    private EditText etName, etEmail, etPassword;
    private Button btnSave, btnMyOrders, btnAdminDashboard;
    private TextView tvLogout;
    private View cvAdminDashboard;

    private UserApi userApi;
    private String token;
    private User currentUser;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Uri selectedAvatarUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Force light mode
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        setupImagePicker();
        initViews();
        setupListeners();
        loadProfile();
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            selectedAvatarUri = imageUri;
                            Glide.with(this)
                                    .load(imageUri)
                                    .placeholder(R.drawable.ic_user_placeholder)
                                    .error(R.drawable.ic_user_placeholder)
                                    .into(ivAvatar);
                        }
                    }
                });
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        ivAvatar = findViewById(R.id.ivAvatar);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSave = findViewById(R.id.btnSave);
        btnMyOrders = findViewById(R.id.btnMyOrders);
        btnAdminDashboard = findViewById(R.id.btnAdminDashboard);
        cvAdminDashboard = findViewById(R.id.cvAdminDashboard);
        tvLogout = findViewById(R.id.tvLogout);

        RetrofitClient retrofitClient = RetrofitClient.getInstance(this);
        userApi = retrofitClient.getUserApi();
        token = retrofitClient.getToken();

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            navigateToLogin();
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        ivAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        btnSave.setOnClickListener(v -> updateProfile());

        btnMyOrders.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, com.example.nike_fe.ui.order.OrderHistoryActivity.class);
            startActivity(intent);
        });

        btnAdminDashboard.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, AdminDashboardActivity.class);
            startActivity(intent);
        });

        tvLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Sign Out")
                    .setMessage("Are you sure you want to sign out?")
                    .setPositiveButton("Sign Out", (dialog, which) -> {
                        // Clear chat data
                        com.example.nike_fe.service.WebSocketChatManager.getInstance().disconnect();
                        com.example.nike_fe.ui.chat.WebSocketChatFragment.clearChatHistory();
                        
                        RetrofitClient.getInstance(this).clearToken();
                        navigateToLogin();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void loadProfile() {
        userApi.getProfile("Bearer " + token).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentUser = response.body();
                    displayProfile(currentUser);
                } else if (response.code() == 401) {
                    Toast.makeText(ProfileActivity.this, "Session expired", Toast.LENGTH_SHORT).show();
                    navigateToLogin();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Connection error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayProfile(User user) {
        android.util.Log.d("ProfileActivity", "🔵 Displaying profile");
        android.util.Log.d("ProfileActivity", "🔵 User avatar: " + user.getAvatar());
        android.util.Log.d("ProfileActivity", "🔵 User avatarUrl: " + user.getAvatarUrl());
        
        etName.setText(user.getFullName());
        etEmail.setText(user.getEmail());
        // Do not display password

        // Load avatar
        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            String avatarUrl = user.getAvatar();
            if (!avatarUrl.startsWith("http")) {
                // Remove leading slash if exists to avoid double slash
                if (avatarUrl.startsWith("/")) {
                    avatarUrl = avatarUrl.substring(1);
                }
                avatarUrl = RetrofitClient.getInstance(this).getBaseUrl() + avatarUrl;
            }
            android.util.Log.d("ProfileActivity", "🟢 Loading avatar from: " + avatarUrl);
            Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_user_placeholder)
                    .error(R.drawable.ic_user_placeholder)
                    .into(ivAvatar);
        } else {
            android.util.Log.d("ProfileActivity", "⚪ No avatar, using placeholder");
            ivAvatar.setImageResource(R.drawable.ic_user_placeholder);
        }

        // Show Admin Dashboard if user has role
        if ("ADMIN".equalsIgnoreCase(user.getRole()) || "ROOT".equalsIgnoreCase(user.getRole())) {
            cvAdminDashboard.setVisibility(View.VISIBLE);
        } else {
            cvAdminDashboard.setVisibility(View.GONE);
        }
    }

    private void updateProfile() {
        String newName = etName.getText().toString().trim();

        if (newName.isEmpty()) {
            etName.setError("Name is required");
            return;
        }

        btnSave.setEnabled(false);

        // Upload avatar first if selected
        if (selectedAvatarUri != null) {
            uploadAvatar(() -> {
                // After avatar uploaded, update profile name
                updateProfileName(newName);
            });
        } else {
            // No avatar selected, just update name
            updateProfileName(newName);
        }
    }

    private void uploadAvatar(Runnable onSuccess) {
        try {
            String filePath = getRealPathFromURI(selectedAvatarUri);
            if (filePath == null) {
                Toast.makeText(this, "Không thể đọc file ảnh", Toast.LENGTH_SHORT).show();
                btnSave.setEnabled(true);
                return;
            }

            File file = new File(filePath);
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("avatar", file.getName(), requestFile);

            userApi.updateAvatar("Bearer " + token, body).enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        currentUser = response.body();
                        selectedAvatarUri = null; // Reset after successful upload
                        
                        android.util.Log.d("ProfileActivity", "🟢 Avatar uploaded successfully");
                        android.util.Log.d("ProfileActivity", "🟢 Response avatar: " + currentUser.getAvatar());
                        android.util.Log.d("ProfileActivity", "🟢 Response avatarUrl: " + currentUser.getAvatarUrl());
                        
                        displayProfile(currentUser); // Update UI with new avatar
                        Toast.makeText(ProfileActivity.this, "Avatar đã cập nhật", Toast.LENGTH_SHORT).show();
                        if (onSuccess != null) {
                            onSuccess.run();
                        }
                    } else {
                        android.util.Log.e("ProfileActivity", "🔴 Avatar upload failed: " + response.code());
                        btnSave.setEnabled(true);
                        Toast.makeText(ProfileActivity.this, "Upload avatar thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    android.util.Log.e("ProfileActivity", "🔴 Avatar upload error: " + t.getMessage());
                    btnSave.setEnabled(true);
                    Toast.makeText(ProfileActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            btnSave.setEnabled(true);
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateProfileName(String newName) {
        UpdateProfileRequest request = new UpdateProfileRequest(newName);
        
        String newPassword = etPassword.getText().toString().trim();
        if (!newPassword.isEmpty()) {
            request.setPassword(newPassword);
        }

        userApi.updateProfile("Bearer " + token, request).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                btnSave.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    currentUser = response.body();
                    // Reload profile to get fresh data including avatar
                    loadProfile();
                    Toast.makeText(ProfileActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this, "Cập nhật thất bại: " + response.code(), Toast.LENGTH_SHORT)
                            .show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                btnSave.setEnabled(true);
                Toast.makeText(ProfileActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getRealPathFromURI(Uri contentUri) {
        try {
            java.io.InputStream inputStream = getContentResolver().openInputStream(contentUri);
            if (inputStream == null) return null;

            File tempFile = new File(getCacheDir(), "temp_avatar_" + System.currentTimeMillis() + ".jpg");
            java.io.FileOutputStream outputStream = new java.io.FileOutputStream(tempFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            return tempFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
