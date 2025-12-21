package com.example.nike_fe.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.nike_fe.R;
import com.example.nike_fe.data.api.AdminApi;
import com.example.nike_fe.data.api.RetrofitClient;
import com.example.nike_fe.data.model.DashboardStats;
import com.example.nike_fe.data.model.OrderStatusDistribution;
import com.example.nike_fe.ui.auth.LoginActivity;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nike_fe.adapter.TopProductAdapter;
import com.example.nike_fe.data.model.TopProduct;
import com.example.nike_fe.data.model.TopProductsResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.util.Log;

public class AdminDashboardActivity extends AppCompatActivity {

    private static final String TAG = "AdminDashboard";

    private DrawerLayout drawerLayout;
    private FrameLayout menuIconContainer;
    private ImageView ivMenu, ivNotifications;
    private TextView tvTotalRevenue, tvTotalOrders, tvTotalUsers, tvProductsInStock, tvProductsOutOfStock;
    private TextView tvRevenueGrowth, tvOrdersGrowth;
    private TextView tvOrderCompleted, tvOrderConfirmed, tvOrderShipping, tvOrderPending, tvOrderCanceled;
    private LinearLayout menuDashboard, menuProducts, menuOrders, menuUsers, menuCategories, menuReviews, menuCoupons,
            menuSettings, menuLogout;
    private FrameLayout layoutLoading;
    private LinearLayout layoutContent;
    private RecyclerView rvTopProducts;
    private TopProductAdapter topProductAdapter;

    private AdminApi adminApi;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        initViews();
        loadDashboardStats();
        loadOrderStatusDistribution();
        loadTopProducts();
    }

    private void initViews() {
        try {
            drawerLayout = findViewById(R.id.drawerLayout);
            menuIconContainer = findViewById(R.id.menuIconContainer);
            ivMenu = findViewById(R.id.ivMenu);
            ivNotifications = findViewById(R.id.ivNotifications);
            tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
            tvTotalOrders = findViewById(R.id.tvTotalOrders);
            tvTotalUsers = findViewById(R.id.tvTotalUsers);
            tvProductsInStock = findViewById(R.id.tvProductsInStock);
            tvProductsOutOfStock = findViewById(R.id.tvProductsOutOfStock);

            tvRevenueGrowth = findViewById(R.id.tvRevenueGrowth);
            tvOrdersGrowth = findViewById(R.id.tvOrdersGrowth);

            layoutLoading = findViewById(R.id.layoutLoading);
            layoutContent = findViewById(R.id.layoutContent);
            rvTopProducts = findViewById(R.id.rvTopProducts);

            // ... (rest of initViews is fine, just replacing the block to include new IDs)

            // Setup RecyclerView
            rvTopProducts.setLayoutManager(new LinearLayoutManager(this));
            topProductAdapter = new TopProductAdapter(this, new ArrayList<>());
            rvTopProducts.setAdapter(topProductAdapter);

            // Order status TextViews
            tvOrderCompleted = findViewById(R.id.tvOrderCompleted);
            tvOrderConfirmed = findViewById(R.id.tvOrderConfirmed);
            tvOrderShipping = findViewById(R.id.tvOrderShipping);
            tvOrderPending = findViewById(R.id.tvOrderPending);
            tvOrderCanceled = findViewById(R.id.tvOrderCanceled);

            // Navigation menu items
            menuDashboard = findViewById(R.id.menuDashboard);
            menuProducts = findViewById(R.id.menuProducts);
            menuOrders = findViewById(R.id.menuOrders);
            menuUsers = findViewById(R.id.menuUsers);
            menuCategories = findViewById(R.id.menuCategories);
            menuReviews = findViewById(R.id.menuReviews);
            menuCoupons = findViewById(R.id.menuCoupons);
            menuSettings = findViewById(R.id.menuSettings);
            menuLogout = findViewById(R.id.menuLogout);

            RetrofitClient retrofitClient = RetrofitClient.getInstance(this);
            adminApi = retrofitClient.getAdminApi();
            token = retrofitClient.getToken();

            if (token == null || token.isEmpty()) {
                Toast.makeText(this, "Vui lòng đăng nhập với tài khoản Admin", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            setupClickListeners();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khởi tạo: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            finish();
        }
    }

    private void setupClickListeners() {
        Log.d(TAG, "Setting up click listeners");

        // Set click listener on container for larger touch area
        if (menuIconContainer != null && drawerLayout != null) {
            Log.d(TAG, "Menu container and drawer layout found, setting up listener");
            menuIconContainer.setOnClickListener(v -> {
                Log.d(TAG, "Menu container clicked!");
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    Log.d(TAG, "Drawer is open, closing it");
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    Log.d(TAG, "Drawer is closed, opening it");
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            });
        }

        // Also set on ImageView as backup
        if (ivMenu != null && drawerLayout != null) {
            ivMenu.setOnClickListener(v -> {
                Log.d(TAG, "Menu icon clicked directly!");
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            });
        }

        if (menuDashboard != null) {
            menuDashboard.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                Toast.makeText(this, "Đã ở trang Dashboard", Toast.LENGTH_SHORT).show();
            });
        }

        if (menuProducts != null) {
            menuProducts.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(this, AdminProductsActivity.class);
                startActivity(intent);
            });
        }

        if (menuOrders != null) {
            menuOrders.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(this, AdminOrdersActivity.class);
                startActivity(intent);
            });
        }

        if (menuUsers != null) {
            menuUsers.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(this, AdminUsersActivity.class);
                startActivity(intent);
            });
        }

        if (menuCategories != null) {
            menuCategories.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(this, AdminCategoriesActivity.class);
                startActivity(intent);
            });
        }

        if (menuReviews != null) {
            menuReviews.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(this, AdminReviewsActivity.class);
                startActivity(intent);
            });
        }

        if (menuCoupons != null) {
            menuCoupons.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(this, AdminCouponsActivity.class);
                startActivity(intent);
            });
        }

        if (menuSettings != null) {
            menuSettings.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                Toast.makeText(this, "Tính năng Cài đặt đang phát triển", Toast.LENGTH_SHORT).show();
            });
        }

        if (menuLogout != null) {
            menuLogout.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                logout();
            });
        }
    }

    private void loadDashboardStats() {
        Log.d(TAG, "Loading dashboard stats...");
        layoutLoading.setVisibility(View.VISIBLE);
        layoutContent.setVisibility(View.GONE);

        adminApi.getDashboardStats("Bearer " + token).enqueue(new Callback<DashboardStats>() {
            @Override
            public void onResponse(Call<DashboardStats> call, Response<DashboardStats> response) {
                Log.d(TAG, "API Response received: " + response.code());
                layoutLoading.setVisibility(View.GONE);
                layoutContent.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Stats received: " + response.body());
                    displayStats(response.body());
                } else if (response.code() == 403) {
                    Log.e(TAG, "Access denied: 403");
                    Toast.makeText(AdminDashboardActivity.this,
                            "Bạn không có quyền truy cập Admin", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Log.e(TAG, "Error loading stats: " + response.code());
                    Toast.makeText(AdminDashboardActivity.this,
                            "Lỗi tải thống kê: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DashboardStats> call, Throwable t) {
                Log.e(TAG, "Failed to load stats", t);
                layoutLoading.setVisibility(View.GONE);
                layoutContent.setVisibility(View.VISIBLE);
                Toast.makeText(AdminDashboardActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayStats(DashboardStats stats) {
        Log.d(TAG, "Displaying stats - Revenue: " + stats.getTotalRevenue() +
                ", Orders: " + stats.getTotalOrders() +
                ", Users: " + stats.getTotalUsers() +
                ", Products: " + stats.getProductsInStock());

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        tvTotalRevenue.setText(currencyFormat.format(stats.getTotalRevenue()));
        tvTotalOrders.setText(String.valueOf(stats.getTotalOrders()));
        tvTotalUsers.setText(String.valueOf(stats.getTotalUsers()));
        tvProductsInStock.setText(String.valueOf(stats.getProductsInStock()));
        tvProductsOutOfStock.setText("Hết hàng: " + stats.getProductsOutOfStock());

        // Display Growth
        setGrowthText(tvRevenueGrowth, stats.getRevenueGrowth());
        setGrowthText(tvOrdersGrowth, stats.getOrdersGrowth());

        Log.d(TAG, "Stats displayed successfully");
    }

    private void setGrowthText(TextView tv, double growth) {
        if (tv == null)
            return;
        String symbol = growth >= 0 ? "+" : "";
        tv.setText(String.format(Locale.US, "%s%.1f%%", symbol, growth));
        // Green for positive/zero, Red for negative
        int color = growth >= 0 ? android.graphics.Color.parseColor("#10B981")
                : android.graphics.Color.parseColor("#EF4444");
        tv.setTextColor(color);
    }

    private void loadOrderStatusDistribution() {
        Log.d(TAG, "Loading order status distribution...");

        adminApi.getOrderStatusDistribution("Bearer " + token).enqueue(new Callback<OrderStatusDistribution>() {
            @Override
            public void onResponse(Call<OrderStatusDistribution> call, Response<OrderStatusDistribution> response) {
                if (response.isSuccessful() && response.body() != null) {
                    OrderStatusDistribution distribution = response.body();
                    Log.d(TAG, "Order status distribution received");
                    displayOrderStatus(distribution);
                } else {
                    Log.e(TAG, "Error loading order status: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<OrderStatusDistribution> call, Throwable t) {
                Log.e(TAG, "Failed to load order status", t);
            }
        });
    }

    private void displayOrderStatus(OrderStatusDistribution distribution) {
        tvOrderCompleted.setText("Hoàn thành: " + distribution.getCompleted());
        tvOrderConfirmed.setText("Đã xác nhận: " + distribution.getConfirmed());
        tvOrderShipping.setText("Đang giao: " + distribution.getShipping());
        tvOrderPending.setText("Chờ xác nhận: " + distribution.getPending());
        tvOrderCanceled.setText("Đã hủy: " + distribution.getCanceled());

        Log.d(TAG, "Order status displayed successfully");
    }

    private void loadTopProducts() {
        Log.d(TAG, "Loading top products...");
        adminApi.getTopProducts("Bearer " + token, 5).enqueue(new Callback<TopProductsResponse>() {
            @Override
            public void onResponse(Call<TopProductsResponse> call, Response<TopProductsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TopProduct> products = response.body().getProducts();
                    if (products != null) {
                        Log.d(TAG, "Top products loaded: " + products.size());
                        topProductAdapter.setData(products);
                    }
                } else {
                    Log.e(TAG, "Error loading top products: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<TopProductsResponse> call, Throwable t) {
                Log.e(TAG, "Failed to load top products", t);
            }
        });
    }

    private void logout() {
        RetrofitClient.getInstance(this).clearToken();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            getOnBackPressedDispatcher().onBackPressed();
        }
    }
}
