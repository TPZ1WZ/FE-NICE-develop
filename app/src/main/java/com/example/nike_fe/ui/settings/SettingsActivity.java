package com.example.nike_fe.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.nike_fe.R;
import com.example.nike_fe.data.api.RetrofitClient;
import com.example.nike_fe.ui.auth.LoginActivity;

public class SettingsActivity extends AppCompatActivity {

    private ImageView ivBack;
    private LinearLayout layoutEditProfile, layoutChangePassword, layoutLanguage;
    private LinearLayout layoutNotificationSettings, layoutPrivacy, layoutTerms;
    private LinearLayout layoutHelp, layoutAbout, layoutLogout;
    private Switch switchDarkMode, switchNotifications;
    private TextView tvVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        layoutEditProfile = findViewById(R.id.layoutEditProfile);
        layoutChangePassword = findViewById(R.id.layoutChangePassword);
        layoutLanguage = findViewById(R.id.layoutLanguage);
        layoutNotificationSettings = findViewById(R.id.layoutNotificationSettings);
        layoutPrivacy = findViewById(R.id.layoutPrivacy);
        layoutTerms = findViewById(R.id.layoutTerms);
        layoutHelp = findViewById(R.id.layoutHelp);
        layoutAbout = findViewById(R.id.layoutAbout);
        layoutLogout = findViewById(R.id.layoutLogout);
        
        switchDarkMode = findViewById(R.id.switchDarkMode);
        switchNotifications = findViewById(R.id.switchNotifications);
        tvVersion = findViewById(R.id.tvVersion);

        if (tvVersion != null) {
            tvVersion.setText("Phiên bản 1.0.0");
        }
    }

    private void setupClickListeners() {
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> finish());
        }

        if (layoutEditProfile != null) {
            layoutEditProfile.setOnClickListener(v -> {
                startActivity(new Intent(this, com.example.nike_fe.ui.profile.EditProfileActivity.class));
            });
        }

        if (layoutChangePassword != null) {
            layoutChangePassword.setOnClickListener(v -> {
                // TODO: Implement change password
                Toast.makeText(this, "Tính năng đổi mật khẩu đang phát triển", Toast.LENGTH_SHORT).show();
            });
        }

        if (layoutLanguage != null) {
            layoutLanguage.setOnClickListener(v -> {
                // TODO: Implement language selection
                Toast.makeText(this, "Tính năng đổi ngôn ngữ đang phát triển", Toast.LENGTH_SHORT).show();
            });
        }

        if (layoutNotificationSettings != null) {
            layoutNotificationSettings.setOnClickListener(v -> {
                Toast.makeText(this, "Cài đặt thông báo đang phát triển", Toast.LENGTH_SHORT).show();
            });
        }

        if (layoutPrivacy != null) {
            layoutPrivacy.setOnClickListener(v -> {
                Toast.makeText(this, "Chính sách bảo mật", Toast.LENGTH_SHORT).show();
            });
        }

        if (layoutTerms != null) {
            layoutTerms.setOnClickListener(v -> {
                Toast.makeText(this, "Điều khoản sử dụng", Toast.LENGTH_SHORT).show();
            });
        }

        if (layoutHelp != null) {
            layoutHelp.setOnClickListener(v -> {
                Toast.makeText(this, "Trung tâm hỗ trợ", Toast.LENGTH_SHORT).show();
            });
        }

        if (layoutAbout != null) {
            layoutAbout.setOnClickListener(v -> {
                Toast.makeText(this, "Nike Store App v1.0.0", Toast.LENGTH_SHORT).show();
            });
        }

        if (layoutLogout != null) {
            layoutLogout.setOnClickListener(v -> showLogoutDialog());
        }

        // Dark mode switch
        if (switchDarkMode != null) {
            int currentMode = AppCompatDelegate.getDefaultNightMode();
            switchDarkMode.setChecked(currentMode == AppCompatDelegate.MODE_NIGHT_YES);
            
            switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                // Restart activity to apply theme
                recreate();
            });
        }

        // Notifications switch
        if (switchNotifications != null) {
            switchNotifications.setChecked(true); // Default enabled
            switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    Toast.makeText(this, "Đã bật thông báo", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Đã tắt thông báo", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showLogoutDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    RetrofitClient.getInstance(this).clearToken();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
