package com.example.nike_fe.ui.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.nike_fe.R;
import com.example.nike_fe.data.api.AdminApi;
import com.example.nike_fe.data.api.RetrofitClient;
import com.example.nike_fe.data.model.ChangePasswordRequest;
import com.example.nike_fe.data.model.StoreSettings;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminSettingsActivity extends AppCompatActivity {

    private static final String TAG = "AdminSettings";
    
    private ImageView ivBack;
    private LinearLayout layoutStoreInfo, layoutChangePassword, layoutNotifications;
    private ProgressBar progressBar;

    private AdminApi adminApi;
    private String token;
    private StoreSettings currentSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_settings);

        initViews();
        setupListeners();
        loadSettings();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        layoutStoreInfo = findViewById(R.id.layoutStoreInfo);
        layoutChangePassword = findViewById(R.id.layoutChangePassword);
        layoutNotifications = findViewById(R.id.layoutNotifications);
        progressBar = findViewById(R.id.progressBar);

        RetrofitClient retrofitClient = RetrofitClient.getInstance(this);
        adminApi = retrofitClient.getAdminApi();
        String rawToken = retrofitClient.getToken();
        token = (rawToken != null && !rawToken.startsWith("Bearer ")) ? "Bearer " + rawToken : rawToken;
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> onBackPressed());

        // 1️⃣ Thông tin cửa hàng
        layoutStoreInfo.setOnClickListener(v -> showStoreInfoDialog());

        // 2️⃣ Đổi mật khẩu
        layoutChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        // 3️⃣ Cài đặt thông báo
        layoutNotifications.setOnClickListener(v -> showNotificationSettingsDialog());
    }

    private void loadSettings() {
        // Load settings từ server
        progressBar.setVisibility(View.VISIBLE);
        
        adminApi.getStoreSettings(token).enqueue(new Callback<StoreSettings>() {
            @Override
            public void onResponse(Call<StoreSettings> call, Response<StoreSettings> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    currentSettings = response.body();
                    Log.d(TAG, "Settings loaded from server");
                } else {
                    // Fallback to SharedPreferences if API fails
                    loadSettingsFromLocal();
                }
            }

            @Override
            public void onFailure(Call<StoreSettings> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Failed to load settings from server", t);
                // Fallback to local
                loadSettingsFromLocal();
            }
        });
    }

    private void loadSettingsFromLocal() {
        // Fallback: Load từ SharedPreferences
        android.content.SharedPreferences prefs = getSharedPreferences("AdminSettings", MODE_PRIVATE);
        currentSettings = new StoreSettings();
        currentSettings.setStoreName(prefs.getString("store_name", "Nike Store"));
        currentSettings.setStorePhone(prefs.getString("store_phone", "0123 456 789"));
        currentSettings.setStoreAddress(prefs.getString("store_address", "123 Nguyễn Văn Linh, TP.HCM"));
        currentSettings.setNotifNewOrders(prefs.getBoolean("notif_new_orders", true));
        currentSettings.setNotifOutOfStock(prefs.getBoolean("notif_out_of_stock", true));
        currentSettings.setNotifSystem(prefs.getBoolean("notif_system", true));
    }

    // 1️⃣ THÔNG TIN CỬA HÀNG - API THẬT
    private void showStoreInfoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thông tin cửa hàng");

        android.view.LayoutInflater inflater = getLayoutInflater();
        android.view.View dialogView = inflater.inflate(R.layout.dialog_store_info, null);
        builder.setView(dialogView);

        TextInputEditText etStoreName = dialogView.findViewById(R.id.etStoreName);
        TextInputEditText etStorePhone = dialogView.findViewById(R.id.etStorePhone);
        TextInputEditText etStoreAddress = dialogView.findViewById(R.id.etStoreAddress);

        // Load dữ liệu hiện tại
        if (currentSettings != null) {
            etStoreName.setText(currentSettings.getStoreName());
            etStorePhone.setText(currentSettings.getStorePhone());
            etStoreAddress.setText(currentSettings.getStoreAddress());
        }

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String storeName = etStoreName.getText().toString().trim();
            String storePhone = etStorePhone.getText().toString().trim();
            String storeAddress = etStoreAddress.getText().toString().trim();

            if (storeName.isEmpty() || storePhone.isEmpty() || storeAddress.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            // Cập nhật qua API
            StoreSettings updatedSettings = new StoreSettings(storeName, storePhone, storeAddress);
            if (currentSettings != null) {
                updatedSettings.setNotifNewOrders(currentSettings.isNotifNewOrders());
                updatedSettings.setNotifOutOfStock(currentSettings.isNotifOutOfStock());
                updatedSettings.setNotifSystem(currentSettings.isNotifSystem());
            }

            progressBar.setVisibility(View.VISIBLE);
            adminApi.updateStoreSettings(token, updatedSettings).enqueue(new Callback<StoreSettings>() {
                @Override
                public void onResponse(Call<StoreSettings> call, Response<StoreSettings> response) {
                    progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful()) {
                        currentSettings = response.body();
                        // Backup to local
                        saveToLocal(updatedSettings);
                        Toast.makeText(AdminSettingsActivity.this, 
                            "✅ Đã cập nhật thông tin cửa hàng", Toast.LENGTH_SHORT).show();
                    } else {
                        // Fallback to local save
                        saveToLocal(updatedSettings);
                        Toast.makeText(AdminSettingsActivity.this, 
                            "⚠️ Đã lưu cục bộ", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<StoreSettings> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Failed to update store settings", t);
                    // Fallback to local
                    saveToLocal(updatedSettings);
                    Toast.makeText(AdminSettingsActivity.this, 
                        "⚠️ Lỗi server, đã lưu cục bộ", Toast.LENGTH_SHORT).show();
                }
            });
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void saveToLocal(StoreSettings settings) {
        android.content.SharedPreferences.Editor editor = 
            getSharedPreferences("AdminSettings", MODE_PRIVATE).edit();
        editor.putString("store_name", settings.getStoreName());
        editor.putString("store_phone", settings.getStorePhone());
        editor.putString("store_address", settings.getStoreAddress());
        editor.putBoolean("notif_new_orders", settings.isNotifNewOrders());
        editor.putBoolean("notif_out_of_stock", settings.isNotifOutOfStock());
        editor.putBoolean("notif_system", settings.isNotifSystem());
        editor.apply();
    }

    // 2️⃣ ĐỔI MẬT KHẨU ADMIN - API THẬT
    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🔐 Đổi mật khẩu");

        android.view.LayoutInflater inflater = getLayoutInflater();
        android.view.View dialogView = inflater.inflate(R.layout.dialog_change_password, null);
        builder.setView(dialogView);

        TextInputEditText etCurrentPassword = dialogView.findViewById(R.id.etCurrentPassword);
        TextInputEditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        TextInputEditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);

        builder.setPositiveButton("Đổi mật khẩu", (dialog, which) -> {
            String currentPassword = etCurrentPassword.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            // Validation
            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPassword.length() < 6) {
                Toast.makeText(this, "Mật khẩu mới phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            // Gọi API đổi mật khẩu THẬT
            ChangePasswordRequest request = new ChangePasswordRequest(currentPassword, newPassword);
            progressBar.setVisibility(View.VISIBLE);
            
            adminApi.changePassword(token, request).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful()) {
                        Log.d(TAG, "✅ Password changed successfully");
                        Toast.makeText(AdminSettingsActivity.this, 
                            "✅ Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "❌ Failed to change password: " + response.code());
                        String errorMsg = response.code() == 400 ? 
                            "Mật khẩu hiện tại không đúng" : "Lỗi đổi mật khẩu";
                        Toast.makeText(AdminSettingsActivity.this, 
                            "❌ " + errorMsg, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "❌ Network error changing password", t);
                    Toast.makeText(AdminSettingsActivity.this, 
                        "❌ Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    // 3️⃣ CÀI ĐẶT THÔNG BÁO - LƯU SERVER
    private void showNotificationSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🔔 Cài đặt thông báo");

        android.view.LayoutInflater inflater = getLayoutInflater();
        android.view.View dialogView = inflater.inflate(R.layout.dialog_notification_settings, null);
        builder.setView(dialogView);

        SwitchCompat switchNewOrders = dialogView.findViewById(R.id.switchNewOrders);
        SwitchCompat switchOutOfStock = dialogView.findViewById(R.id.switchOutOfStock);
        SwitchCompat switchSystem = dialogView.findViewById(R.id.switchSystem);

        // Load cài đặt hiện tại
        if (currentSettings != null) {
            switchNewOrders.setChecked(currentSettings.isNotifNewOrders());
            switchOutOfStock.setChecked(currentSettings.isNotifOutOfStock());
            switchSystem.setChecked(currentSettings.isNotifSystem());
        }

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            if (currentSettings == null) {
                currentSettings = new StoreSettings();
            }
            
            currentSettings.setNotifNewOrders(switchNewOrders.isChecked());
            currentSettings.setNotifOutOfStock(switchOutOfStock.isChecked());
            currentSettings.setNotifSystem(switchSystem.isChecked());

            // Lưu lên server
            progressBar.setVisibility(View.VISIBLE);
            adminApi.updateStoreSettings(token, currentSettings).enqueue(new Callback<StoreSettings>() {
                @Override
                public void onResponse(Call<StoreSettings> call, Response<StoreSettings> response) {
                    progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful()) {
                        saveToLocal(currentSettings);
                        Toast.makeText(AdminSettingsActivity.this, 
                            "✅ Đã lưu cài đặt thông báo", Toast.LENGTH_SHORT).show();
                    } else {
                        saveToLocal(currentSettings);
                        Toast.makeText(AdminSettingsActivity.this, 
                            "⚠️ Đã lưu cục bộ", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<StoreSettings> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    saveToLocal(currentSettings);
                    Toast.makeText(AdminSettingsActivity.this, 
                        "⚠️ Lỗi server, đã lưu cục bộ", Toast.LENGTH_SHORT).show();
                }
            });
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }
}
