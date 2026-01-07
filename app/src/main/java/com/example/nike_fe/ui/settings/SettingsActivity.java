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

import com.example.nike_fe.R;
import com.example.nike_fe.data.api.RetrofitClient;
import com.example.nike_fe.ui.auth.LoginActivity;

public class SettingsActivity extends AppCompatActivity {

    private ImageView ivBack;
    private LinearLayout layoutEditProfile;
    private LinearLayout layoutPrivacy, layoutTerms;
    private LinearLayout layoutHelp, layoutAbout, layoutLogout;
    private Switch switchNotifications;
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

        layoutPrivacy = findViewById(R.id.layoutPrivacy);
        layoutTerms = findViewById(R.id.layoutTerms);
        layoutHelp = findViewById(R.id.layoutHelp);
        layoutAbout = findViewById(R.id.layoutAbout);
        layoutLogout = findViewById(R.id.layoutLogout);
        
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



        if (layoutPrivacy != null) {
            layoutPrivacy.setOnClickListener(v -> {
                startActivity(new Intent(this, PrivacyPolicyActivity.class));
            });
        }

        if (layoutTerms != null) {
            layoutTerms.setOnClickListener(v -> {
                startActivity(new Intent(this, TermsOfServiceActivity.class));
            });
        }

        if (layoutHelp != null) {
            layoutHelp.setOnClickListener(v -> {
                startActivity(new Intent(this, HelpCenterActivity.class));
            });
        }

        if (layoutAbout != null) {
            layoutAbout.setOnClickListener(v -> {
                startActivity(new Intent(this, AboutAppActivity.class));
            });
        }

        if (layoutLogout != null) {
            layoutLogout.setOnClickListener(v -> showLogoutDialog());
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
                    // Clear chat data
                    com.example.nike_fe.service.WebSocketChatManager.getInstance().disconnect();
                    com.example.nike_fe.ui.chat.WebSocketChatFragment.clearChatHistory();
                    
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
