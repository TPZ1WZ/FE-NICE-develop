package com.example.nike_fe.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nike_fe.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;

/**
 * AdminLuckyWheelActivity - Quản lý vòng quay may mắn
 * 
 * Chức năng:
 * 1. Quản lý danh sách phần thưởng (thêm/sửa/xóa)
 * 2. Cấu hình xác suất trúng
 * 3. Quản lý sự kiện quay (bật/tắt, đặt thời gian)
 * 4. Quản lý lượt quay của user (xem, reset, tặng thêm)
 */
public class AdminLuckyWheelActivity extends AppCompatActivity {

    private static final String TAG = "AdminLuckyWheel";

    // Layout components
    private DrawerLayout drawerLayout;
    private MaterialToolbar toolbar;

    // Quick actions
    private MaterialCardView cardManagePrizes;
    private MaterialCardView cardManageProbability;
    private MaterialCardView cardManageEvents;
    private MaterialCardView cardManageUserSpins;

    // Statistics
    private TextView tvTotalSpins;
    private TextView tvActiveUsers;
    private TextView tvTotalPrizes;
    private TextView tvWheelStatus;

    // Navigation menu
    private LinearLayout menuDashboardV2, menuProductsV2, menuOrdersV2, menuUsersV2,
            menuCategoriesV2, menuReviewsV2, menuCouponsV2, menuLuckyWheelV2,
            menuSettingsV2, menuHomeV2, menuLogoutV2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_lucky_wheel);

        initViews();
        setupClickListeners();
        loadStatistics();
    }

    private void initViews() {
        // Drawer and toolbar
        drawerLayout = findViewById(R.id.drawerLayoutLuckyWheel);
        toolbar = findViewById(R.id.toolbarLuckyWheel);

        // Quick action cards
        cardManagePrizes = findViewById(R.id.cardManagePrizes);
        cardManageProbability = findViewById(R.id.cardManageProbability);
        cardManageEvents = findViewById(R.id.cardManageEvents);
        cardManageUserSpins = findViewById(R.id.cardManageUserSpins);

        // Statistics
        tvTotalSpins = findViewById(R.id.tvTotalSpins);
        tvActiveUsers = findViewById(R.id.tvActiveUsers);
        tvTotalPrizes = findViewById(R.id.tvTotalPrizes);
        tvWheelStatus = findViewById(R.id.tvWheelStatus);

        // Navigation menu items
        menuDashboardV2 = findViewById(R.id.menuDashboardV2);
        menuProductsV2 = findViewById(R.id.menuProductsV2);
        menuOrdersV2 = findViewById(R.id.menuOrdersV2);
        menuUsersV2 = findViewById(R.id.menuUsersV2);
        menuCategoriesV2 = findViewById(R.id.menuCategoriesV2);
        menuReviewsV2 = findViewById(R.id.menuReviewsV2);
        menuCouponsV2 = findViewById(R.id.menuCouponsV2);
        menuLuckyWheelV2 = findViewById(R.id.menuLuckyWheelV2);
        menuSettingsV2 = findViewById(R.id.menuSettingsV2);
        menuHomeV2 = findViewById(R.id.menuHomeV2);
        menuLogoutV2 = findViewById(R.id.menuLogoutV2);
    }

    private void setupClickListeners() {
        // Toolbar navigation
        if (toolbar != null && drawerLayout != null) {
            toolbar.setNavigationOnClickListener(v -> {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            });
        }

        // Quick action cards
        cardManagePrizes.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminPrizesActivity.class);
            startActivity(intent);
        });

        cardManageProbability.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminProbabilityActivity.class);
            startActivity(intent);
        });

        cardManageEvents.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminWheelConfigActivity.class);
            startActivity(intent);
        });

        cardManageUserSpins.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminUserSpinsActivity.class);
            startActivity(intent);
        });

        // Navigation menu
        setupNavigationMenu();
    }

    private void setupNavigationMenu() {
        if (menuDashboardV2 != null) {
            menuDashboardV2.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(this, AdminDashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            });
        }

        if (menuProductsV2 != null) {
            menuProductsV2.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(this, AdminProductsActivity.class);
                startActivity(intent);
            });
        }

        if (menuOrdersV2 != null) {
            menuOrdersV2.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(this, AdminOrdersActivity.class);
                startActivity(intent);
            });
        }

        if (menuUsersV2 != null) {
            menuUsersV2.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(this, AdminUsersActivity.class);
                startActivity(intent);
            });
        }

        if (menuCategoriesV2 != null) {
            menuCategoriesV2.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(this, AdminCategoriesActivity.class);
                startActivity(intent);
            });
        }

        if (menuReviewsV2 != null) {
            menuReviewsV2.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(this, AdminReviewsActivity.class);
                startActivity(intent);
            });
        }

        if (menuCouponsV2 != null) {
            menuCouponsV2.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(this, AdminCouponsActivity.class);
                startActivity(intent);
            });
        }

        if (menuLuckyWheelV2 != null) {
            menuLuckyWheelV2.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                Toast.makeText(this, "Đã ở trang Vòng quay may mắn", Toast.LENGTH_SHORT).show();
            });
        }

        if (menuSettingsV2 != null) {
            menuSettingsV2.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(this, AdminSettingsActivity.class);
                startActivity(intent);
            });
        }

        if (menuHomeV2 != null) {
            menuHomeV2.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(this, com.example.nike_fe.MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

        if (menuLogoutV2 != null) {
            menuLogoutV2.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                // TODO: Implement logout
                Toast.makeText(this, "Đăng xuất", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void loadStatistics() {
        String token = "";
        String savedToken = com.example.nike_fe.data.api.RetrofitClient.getInstance(this).getToken();
        if (savedToken != null) {
            token = "Bearer " + savedToken;
        } else {
            return;
        }

        com.example.nike_fe.data.api.LuckyWheelApi api = com.example.nike_fe.data.api.RetrofitClient.getInstance(this)
                .getLuckyWheelApi();

        // Load Statistics
        api.getStatistics(token).enqueue(new retrofit2.Callback<com.example.nike_fe.data.model.LuckyWheelStatistics>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.nike_fe.data.model.LuckyWheelStatistics> call,
                    retrofit2.Response<com.example.nike_fe.data.model.LuckyWheelStatistics> response) {
                if (response.isSuccessful() && response.body() != null) {
                    com.example.nike_fe.data.model.LuckyWheelStatistics stats = response.body();
                    tvTotalSpins.setText(String.valueOf(stats.getTotalSpins()));
                    tvActiveUsers.setText(String.valueOf(stats.getUniqueUsers()));
                    tvTotalPrizes.setText(String.valueOf(stats.getTotalPrizesWon()));
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.nike_fe.data.model.LuckyWheelStatistics> call,
                    Throwable t) {
                // Ignore or log
            }
        });

        // Load Status
        api.getConfig(token).enqueue(new retrofit2.Callback<com.example.nike_fe.data.model.WheelConfig>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.nike_fe.data.model.WheelConfig> call,
                    retrofit2.Response<com.example.nike_fe.data.model.WheelConfig> response) {
                if (response.isSuccessful() && response.body() != null) {
                    com.example.nike_fe.data.model.WheelConfig config = response.body();
                    if (Boolean.TRUE.equals(config.getIsActive())) {
                        if (Boolean.TRUE.equals(config.getIsTimeRestricted())) {
                            tvWheelStatus.setText("Sự kiện đang diễn ra");
                            tvWheelStatus.setTextColor(android.graphics.Color.parseColor("#10B981")); // Green
                        } else {
                            tvWheelStatus.setText("Đang hoạt động");
                            tvWheelStatus.setTextColor(android.graphics.Color.parseColor("#10B981")); // Green
                        }
                    } else {
                        tvWheelStatus.setText("Đang tắt");
                        tvWheelStatus.setTextColor(android.graphics.Color.parseColor("#EF4444")); // Red
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.nike_fe.data.model.WheelConfig> call, Throwable t) {
                // Ignore
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
