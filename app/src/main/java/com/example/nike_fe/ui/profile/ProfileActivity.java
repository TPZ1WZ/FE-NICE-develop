package com.example.nike_fe.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private CircleImageView ivAvatar;
    private EditText etName, etEmail, etPassword;
    private Button btnSave, btnAdminDashboard;
    private TextView tvLogout;

    private UserApi userApi;
    private String token;
    private User currentUser;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

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
                            Glide.with(this)
                                    .load(imageUri)
                                    .placeholder(R.drawable.img_placeholder_shoe)
                                    .error(R.drawable.img_placeholder_shoe)
                                    .into(ivAvatar);
                            Toast.makeText(this, "Profile image selected", Toast.LENGTH_SHORT).show();
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
        btnAdminDashboard = findViewById(R.id.btnAdminDashboard);
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

        btnAdminDashboard.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, AdminDashboardActivity.class);
            startActivity(intent);
        });

        tvLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Sign Out")
                    .setMessage("Are you sure you want to sign out?")
                    .setPositiveButton("Sign Out", (dialog, which) -> {
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
        etName.setText(user.getFullName());
        etEmail.setText(user.getEmail());
        // Do not display password

        // Show Admin Dashboard if user has role
        if ("ADMIN".equalsIgnoreCase(user.getRole()) || "ROOT".equalsIgnoreCase(user.getRole())) {
            btnAdminDashboard.setVisibility(View.VISIBLE);
        } else {
            btnAdminDashboard.setVisibility(View.GONE);
        }
    }

    private void updateProfile() {
        String newName = etName.getText().toString().trim();

        if (newName.isEmpty()) {
            etName.setError("Name is required");
            return;
        }

        btnSave.setEnabled(false);
        btnSave.setText("Saving...");

        UpdateProfileRequest request = new UpdateProfileRequest(newName);

        userApi.updateProfile("Bearer " + token, request).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                btnSave.setEnabled(true);
                btnSave.setText("Save Now");

                if (response.isSuccessful() && response.body() != null) {
                    currentUser = response.body();
                    displayProfile(currentUser);
                    Toast.makeText(ProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this, "Update failed: " + response.code(), Toast.LENGTH_SHORT)
                            .show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                btnSave.setEnabled(true);
                btnSave.setText("Save Now");
                Toast.makeText(ProfileActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
