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
    private LinearLayout layoutStoreInfo, layoutChangePassword, layoutSystemRules;
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
        layoutSystemRules = findViewById(R.id.layoutSystemRules);
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

        // 2️⃣ Quy tắc hệ thống
        layoutSystemRules.setOnClickListener(v -> showSystemRulesDialog());

        // 3️⃣ Đổi mật khẩu
        layoutChangePassword.setOnClickListener(v -> showChangePasswordDialog());
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
        builder.setTitle("Đổi mật khẩu");

        android.view.LayoutInflater inflater = getLayoutInflater();
        android.view.View dialogView = inflater.inflate(R.layout.dialog_change_password, null);
        builder.setView(dialogView);

        com.google.android.material.textfield.TextInputLayout tilCurrentPassword = dialogView.findViewById(R.id.tilCurrentPassword);
        com.google.android.material.textfield.TextInputLayout tilNewPassword = dialogView.findViewById(R.id.tilNewPassword);
        com.google.android.material.textfield.TextInputLayout tilConfirmPassword = dialogView.findViewById(R.id.tilConfirmPassword);
        TextInputEditText etCurrentPassword = dialogView.findViewById(R.id.etCurrentPassword);
        TextInputEditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        TextInputEditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);

        builder.setPositiveButton("Đổi mật khẩu", null);
        builder.setNegativeButton("Hủy", null);
        
        AlertDialog dialog = builder.create();
        dialog.show();
        
        // Override positive button để ngăn auto-dismiss
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            // Xóa lỗi cũ
            tilCurrentPassword.setError(null);
            tilNewPassword.setError(null);
            tilConfirmPassword.setError(null);
            
            String currentPassword = etCurrentPassword.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            // Validation
            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPassword.length() < 6) {
                tilNewPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                tilConfirmPassword.setError("Mật khẩu xác nhận không khớp");
                return;
            }

            // Gọi API đổi mật khẩu THẬT
            ChangePasswordRequest request = new ChangePasswordRequest(currentPassword, newPassword);
            progressBar.setVisibility(View.VISIBLE);
            
            adminApi.changePassword(token, request).enqueue(new Callback<okhttp3.ResponseBody>() {
                @Override
                public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> response) {
                    progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful()) {
                        Log.d(TAG, "✅ Password changed successfully");
                        dialog.dismiss();
                        Toast.makeText(AdminSettingsActivity.this, 
                            "✅ Đổi mật khẩu thành công!", Toast.LENGTH_LONG).show();
                    } else {
                        // Parse error body để lấy message cụ thể
                        String errorMessage = "Lỗi đổi mật khẩu";
                        try {
                            if (response.errorBody() != null) {
                                String errorBody = response.errorBody().string();
                                Log.e(TAG, "❌ Error response body: " + errorBody);
                                
                                // Parse JSON để lấy error message
                                if (errorBody.contains("Current password is wrong") || 
                                    errorBody.contains("Mật khẩu hiện tại")) {
                                    // Mật khẩu hiện tại sai - hiển thị lỗi trên input
                                    tilCurrentPassword.setError("Mật khẩu hiện tại không đúng");
                                    etCurrentPassword.requestFocus();
                                    return;
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing error body", e);
                        }
                        
                        Log.e(TAG, "❌ Failed to change password: code=" + response.code() + 
                                ", message=" + response.message());
                        Toast.makeText(AdminSettingsActivity.this, 
                            "❌ " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "❌ Network error changing password", t);
                    Toast.makeText(AdminSettingsActivity.this, 
                        "❌ Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    // 3️⃣ QUY TẮC HỆ THỐNG - NGƯỠNG TỒN KHO
    private void showSystemRulesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quy tắc hệ thống");

        android.view.LayoutInflater inflater = getLayoutInflater();
        android.view.View dialogView = inflater.inflate(R.layout.dialog_system_rules, null);
        builder.setView(dialogView);

        TextInputEditText etStockThreshold = dialogView.findViewById(R.id.etStockThreshold);

        // Load cài đặt từ SharedPreferences
        android.content.SharedPreferences prefs = getSharedPreferences("AdminSettings", MODE_PRIVATE);
        etStockThreshold.setText(String.valueOf(prefs.getInt("stock_threshold", 10)));

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String thresholdStr = etStockThreshold.getText().toString().trim();
            
            if (thresholdStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập ngưỡng cảnh báo", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int stockThreshold = Integer.parseInt(thresholdStr);

                // Validation
                if (stockThreshold < 1 || stockThreshold > 1000) {
                    Toast.makeText(this, "Ngưỡng tồn kho phải từ 1-1000", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Lưu cài đặt
                android.content.SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("stock_threshold", stockThreshold);
                editor.apply();

                Toast.makeText(this, "✅ Đã lưu ngưỡng cảnh báo tồn kho: " + stockThreshold, 
                    Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Stock threshold saved: " + stockThreshold);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Vui lòng nhập số hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }
}
