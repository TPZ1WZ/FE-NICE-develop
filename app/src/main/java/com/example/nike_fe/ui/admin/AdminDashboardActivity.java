package com.example.nike_fe.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.graphics.Color;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.MaterialToolbar;
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
import com.example.nike_fe.data.model.ProductStats;
import com.example.nike_fe.data.model.RevenueChartData;
import com.example.nike_fe.data.api.AdminProductApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

public class AdminDashboardActivity extends AppCompatActivity {

    private static final String TAG = "AdminDashboard";

    // Layout v2 views
    private DrawerLayout drawerLayoutV2;
    private MaterialToolbar topAppBarV2;
    private TextView tvRevenueV2, tvRevenueDeltaV2;
    private TextView tvOrdersV2, tvOrdersDeltaV2;
    private TextView tvCustomersV2, tvCustomersDeltaV2;
    private TextView tvStockTotalV2, tvStockLowV2;
    private TextView tvPendingAlertV2;
    private RecyclerView rvTopProductsV2;
    private TopProductAdapter topProductAdapter;
    private TextView tvTopEmptyV2;
    
    // Revenue chart views
    private Spinner spinnerRevenuePeriod;
    private TextView tvRevenueTotalV2Chart, tvRevenueChangeV2, tvRevenueCompareV2;
    private LineChart lineChartRevenue;
    private TextView tvRevenueEmptyV2;
    private int currentRevenuePeriod = 7; // 7=7 days (default), 30=30 days
    
    // Quick action buttons
    private View btnQuickOrdersV2, btnQuickAddProductV2, btnQuickVoucherV2;
    
    // Navigation menu items
    private LinearLayout menuDashboardV2, menuProductsV2, menuOrdersV2, menuUsersV2, 
            menuCategoriesV2, menuReviewsV2, menuCouponsV2, menuSettingsV2, menuLogoutV2;

    private AdminApi adminApi;
    private AdminProductApi productApi;
    private String token;
    
    // Auto-refresh
    private Handler refreshHandler;
    private Runnable refreshRunnable;
    private static final long REFRESH_INTERVAL = 30000; // 30 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard_v2);

        initViews();
        loadDashboardStats();
        loadProductStats(); // Load product stats from products/stats endpoint
        loadOrderStatusDistribution();
        loadTopProducts();
        loadRevenueChart(currentRevenuePeriod);
        startAutoRefresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data khi quay lại trang dashboard
        loadDashboardStats();
        loadProductStats();
        loadOrderStatusDistribution();
        loadTopProducts();
        loadRevenueChart(currentRevenuePeriod);
        startAutoRefresh();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        stopAutoRefresh();
    }

    private void initViews() {
        try {
            // DrawerLayout
            drawerLayoutV2 = findViewById(R.id.drawerLayoutV2);
            
            // Toolbar
            topAppBarV2 = findViewById(R.id.topAppBarV2);
            
            // KPI Cards (4 cards)
            tvRevenueV2 = findViewById(R.id.tvRevenueV2);
            tvRevenueDeltaV2 = findViewById(R.id.tvRevenueDeltaV2);
            tvOrdersV2 = findViewById(R.id.tvOrdersV2);
            tvOrdersDeltaV2 = findViewById(R.id.tvOrdersDeltaV2);
            tvCustomersV2 = findViewById(R.id.tvCustomersV2);
            tvCustomersDeltaV2 = findViewById(R.id.tvCustomersDeltaV2);
            tvStockTotalV2 = findViewById(R.id.tvStockTotalV2);
            tvStockLowV2 = findViewById(R.id.tvStockLowV2);
            
            // Alert card
            tvPendingAlertV2 = findViewById(R.id.tvPendingAlertV2);
            
            // Quick action buttons
            btnQuickOrdersV2 = findViewById(R.id.btnQuickOrdersV2);
            btnQuickAddProductV2 = findViewById(R.id.btnQuickAddProductV2);
            btnQuickVoucherV2 = findViewById(R.id.btnQuickVoucherV2);
            
            // Top products RecyclerView
            rvTopProductsV2 = findViewById(R.id.rvTopProductsV2);
            rvTopProductsV2.setLayoutManager(new LinearLayoutManager(this));
            topProductAdapter = new TopProductAdapter(this, new ArrayList<>());
            rvTopProductsV2.setAdapter(topProductAdapter);
            
            tvTopEmptyV2 = findViewById(R.id.tvTopEmptyV2);
            
            // Revenue chart views
            spinnerRevenuePeriod = findViewById(R.id.spinnerRevenuePeriod);
            tvRevenueTotalV2Chart = findViewById(R.id.tvRevenueTotalV2);
            tvRevenueChangeV2 = findViewById(R.id.tvRevenueChangeV2);
            tvRevenueCompareV2 = findViewById(R.id.tvRevenueCompareV2);
            lineChartRevenue = findViewById(R.id.lineChartRevenue);
            tvRevenueEmptyV2 = findViewById(R.id.tvRevenueEmptyV2);
            
            setupRevenuePeriodSpinner();
            
            // Navigation menu items
            menuDashboardV2 = findViewById(R.id.menuDashboardV2);
            menuProductsV2 = findViewById(R.id.menuProductsV2);
            menuOrdersV2 = findViewById(R.id.menuOrdersV2);
            menuUsersV2 = findViewById(R.id.menuUsersV2);
            menuCategoriesV2 = findViewById(R.id.menuCategoriesV2);
            menuReviewsV2 = findViewById(R.id.menuReviewsV2);
            menuCouponsV2 = findViewById(R.id.menuCouponsV2);
            menuSettingsV2 = findViewById(R.id.menuSettingsV2);
            menuLogoutV2 = findViewById(R.id.menuLogoutV2);

            RetrofitClient retrofitClient = RetrofitClient.getInstance(this);
            adminApi = retrofitClient.getAdminApi();
            productApi = retrofitClient.getAdminProductApi();
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

    private void setupRevenuePeriodSpinner() {
        String[] periods = {"7 ngày", "30 ngày"};
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, periods);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        spinnerRevenuePeriod.setAdapter(adapter);
        
        // Set default selection (7 days)
        spinnerRevenuePeriod.setSelection(0);
        
        spinnerRevenuePeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentRevenuePeriod = (position == 0) ? 7 : 30;
                loadRevenueChart(currentRevenuePeriod);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void setupClickListeners() {
        Log.d(TAG, "Setting up click listeners for v2 layout");

        // Toolbar navigation icon (menu) - Open drawer
        if (topAppBarV2 != null && drawerLayoutV2 != null) {
            topAppBarV2.setNavigationOnClickListener(v -> {
                Log.d(TAG, "Toolbar navigation clicked - opening drawer");
                if (drawerLayoutV2.isDrawerOpen(GravityCompat.START)) {
                    drawerLayoutV2.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayoutV2.openDrawer(GravityCompat.START);
                }
            });
        }

        // Navigation menu items
        if (menuDashboardV2 != null) {
            menuDashboardV2.setOnClickListener(v -> {
                if (drawerLayoutV2 != null) drawerLayoutV2.closeDrawer(GravityCompat.START);
                Toast.makeText(this, "Đã ở trang Dashboard", Toast.LENGTH_SHORT).show();
            });
        }

        if (menuProductsV2 != null) {
            menuProductsV2.setOnClickListener(v -> {
                if (drawerLayoutV2 != null) drawerLayoutV2.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(this, AdminProductsActivity.class);
                startActivity(intent);
            });
        }

        if (menuOrdersV2 != null) {
            menuOrdersV2.setOnClickListener(v -> {
                if (drawerLayoutV2 != null) drawerLayoutV2.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(this, AdminOrdersActivity.class);
                startActivity(intent);
            });
        }

        if (menuUsersV2 != null) {
            menuUsersV2.setOnClickListener(v -> {
                if (drawerLayoutV2 != null) drawerLayoutV2.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(this, AdminUsersActivity.class);
                startActivity(intent);
            });
        }

        if (menuCategoriesV2 != null) {
            menuCategoriesV2.setOnClickListener(v -> {
                if (drawerLayoutV2 != null) drawerLayoutV2.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(this, AdminCategoriesActivity.class);
                startActivity(intent);
            });
        }

        if (menuReviewsV2 != null) {
            menuReviewsV2.setOnClickListener(v -> {
                if (drawerLayoutV2 != null) drawerLayoutV2.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(this, AdminReviewsActivity.class);
                startActivity(intent);
            });
        }

        if (menuCouponsV2 != null) {
            menuCouponsV2.setOnClickListener(v -> {
                if (drawerLayoutV2 != null) drawerLayoutV2.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(this, AdminCouponsActivity.class);
                startActivity(intent);
            });
        }

        if (menuSettingsV2 != null) {
            menuSettingsV2.setOnClickListener(v -> {
                if (drawerLayoutV2 != null) drawerLayoutV2.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(this, AdminSettingsActivity.class);
                startActivity(intent);
            });
        }

        if (menuLogoutV2 != null) {
            menuLogoutV2.setOnClickListener(v -> {
                if (drawerLayoutV2 != null) drawerLayoutV2.closeDrawer(GravityCompat.START);
                logout();
            });
        }

        // Quick action buttons
        if (btnQuickOrdersV2 != null) {
            btnQuickOrdersV2.setOnClickListener(v -> {
                Intent intent = new Intent(this, AdminOrdersActivity.class);
                startActivity(intent);
            });
        }

        if (btnQuickAddProductV2 != null) {
            btnQuickAddProductV2.setOnClickListener(v -> {
                Intent intent = new Intent(this, AdminProductsActivity.class);
                intent.putExtra("action", "add");
                startActivity(intent);
            });
        }

        if (btnQuickVoucherV2 != null) {
            btnQuickVoucherV2.setOnClickListener(v -> {
                Intent intent = new Intent(this, AdminCouponsActivity.class);
                startActivity(intent);
            });
        }

        // Pending alert card click
        View cardAlert = findViewById(R.id.cardAlertPendingV2);
        if (cardAlert != null) {
            cardAlert.setOnClickListener(v -> {
                Intent intent = new Intent(this, AdminOrdersActivity.class);
                intent.putExtra("filter", "pending");
                startActivity(intent);
            });
        }
        
        // "Xem danh sách" text click - go to pending orders
        TextView tvViewPendingList = findViewById(R.id.tvViewPendingListV2);
        if (tvViewPendingList != null) {
            tvViewPendingList.setOnClickListener(v -> {
                Log.d(TAG, "View pending list clicked - opening orders with PENDING filter");
                Intent intent = new Intent(this, AdminOrdersActivity.class);
                intent.putExtra("filter", "pending");
                startActivity(intent);
            });
        }
    }

    private void loadDashboardStats() {
        Log.d(TAG, "Loading dashboard stats from API...");

        adminApi.getDashboardStats("Bearer " + token).enqueue(new Callback<DashboardStats>() {
            @Override
            public void onResponse(Call<DashboardStats> call, Response<DashboardStats> response) {
                Log.d(TAG, "API Response received: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Stats received from database: " + response.body());
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
                Log.e(TAG, "Failed to load stats from database", t);
                Toast.makeText(AdminDashboardActivity.this,
                        "Lỗi kết nối database: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayStats(DashboardStats stats) {
        Log.d(TAG, "Displaying stats from database - Revenue: " + stats.getTotalRevenue() +
                ", Orders: " + stats.getTotalOrders() +
                ", Users: " + stats.getTotalUsers());

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        // KPI Card 1: Revenue
        tvRevenueV2.setText(currencyFormat.format(stats.getTotalRevenue()));
        setGrowthText(tvRevenueDeltaV2, stats.getRevenueGrowth());

        // KPI Card 2: Orders
        tvOrdersV2.setText(String.valueOf(stats.getTotalOrders()));
        setGrowthText(tvOrdersDeltaV2, stats.getOrdersGrowth());

        // KPI Card 3: Customers/Users
        tvCustomersV2.setText(String.valueOf(stats.getTotalUsers()));
        // Giả sử có customer growth, nếu không có trong API thì ẩn đi
        if (tvCustomersDeltaV2 != null) {
            tvCustomersDeltaV2.setVisibility(View.GONE);
        }

        // KPI Card 4: Stock - will be loaded separately from ProductStats API
        // Don't use stats.getProductsInStock() here as it may include hidden products

        Log.d(TAG, "Stats displayed successfully in v2 layout from database");
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

    private void loadProductStats() {
        Log.d(TAG, "Loading product stats from products/stats API (only active products)...");
        
        productApi.getStats("Bearer " + token).enqueue(new Callback<ProductStats>() {
            @Override
            public void onResponse(Call<ProductStats> call, Response<ProductStats> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProductStats stats = response.body();
                    Log.d(TAG, "Product stats loaded: total=" + stats.getTotal() + 
                            ", lowStock=" + stats.getLowStock() + 
                            ", outOfStock=" + stats.getOutOfStock());
                    
                    // Update stock card with accurate data (only active products)
                    int totalStock = stats.getTotal();
                    int lowStock = stats.getLowStock(); // Products with stock < 10
                    
                    tvStockTotalV2.setText("Sản phẩm: " + totalStock);
                    tvStockLowV2.setText("Gần hết (<10): " + lowStock);
                    
                    Log.d(TAG, "Product stats displayed successfully (excluding hidden products)");
                } else {
                    Log.e(TAG, "Error loading product stats: " + response.code());
                    // Set default values on error
                    tvStockTotalV2.setText("Sản phẩm: 0");
                    tvStockLowV2.setText("Gần hết (<10): 0");
                }
            }

            @Override
            public void onFailure(Call<ProductStats> call, Throwable t) {
                Log.e(TAG, "Failed to load product stats", t);
                // Set default values on failure
                tvStockTotalV2.setText("Sản phẩm: 0");
                tvStockLowV2.setText("Gần hết (<10): 0");
            }
        });
    }

    private void loadOrderStatusDistribution() {
        Log.d(TAG, "Loading order status distribution from database...");

        adminApi.getOrderStatusDistribution("Bearer " + token).enqueue(new Callback<OrderStatusDistribution>() {
            @Override
            public void onResponse(Call<OrderStatusDistribution> call, Response<OrderStatusDistribution> response) {
                if (response.isSuccessful() && response.body() != null) {
                    OrderStatusDistribution distribution = response.body();
                    Log.d(TAG, "Order status distribution received from database");
                    displayOrderStatus(distribution);
                } else {
                    Log.e(TAG, "Error loading order status from database: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<OrderStatusDistribution> call, Throwable t) {
                Log.e(TAG, "Failed to load order status from database", t);
            }
        });
    }

    private void displayOrderStatus(OrderStatusDistribution distribution) {
        // Update pending alert card
        long pendingCount = distribution.getPending();
        if (tvPendingAlertV2 != null) {
            tvPendingAlertV2.setText("Đơn chờ xác nhận: " + pendingCount);
        }

        Log.d(TAG, "Order status displayed: Pending=" + pendingCount + " (from database)");
    }

    private void loadTopProducts() {
        Log.d(TAG, "Loading top products from database...");
        
        // Show loading state
        if (rvTopProductsV2 != null) {
            rvTopProductsV2.setVisibility(View.VISIBLE);
        }
        if (tvTopEmptyV2 != null) {
            tvTopEmptyV2.setVisibility(View.GONE);
        }
        
        adminApi.getTopProducts("Bearer " + token, 5).enqueue(new Callback<TopProductsResponse>() {
            @Override
            public void onResponse(Call<TopProductsResponse> call, Response<TopProductsResponse> response) {
                Log.d(TAG, "Top products response code: " + response.code());
                
                if (response.isSuccessful() && response.body() != null) {
                    TopProductsResponse responseBody = response.body();
                    Log.d(TAG, "Top products response body: " + responseBody);
                    
                    List<TopProduct> products = responseBody.getProducts();
                    
                    if (products != null && !products.isEmpty()) {
                        Log.d(TAG, "Top products loaded from database: " + products.size() + " items");
                        for (int i = 0; i < products.size(); i++) {
                            TopProduct p = products.get(i);
                            Log.d(TAG, "  Product " + (i+1) + ": " + p.getName() + " - Sold: " + p.getSoldQuantity());
                        }
                        
                        if (topProductAdapter != null) {
                            topProductAdapter.setData(products);
                            Log.d(TAG, "Adapter updated with " + products.size() + " products");
                        } else {
                            Log.e(TAG, "TopProductAdapter is null!");
                        }
                        
                        if (rvTopProductsV2 != null) {
                            rvTopProductsV2.setVisibility(View.VISIBLE);
                        }
                        
                        if (tvTopEmptyV2 != null) {
                            tvTopEmptyV2.setVisibility(View.GONE);
                        }
                    } else {
                        Log.d(TAG, "No top products found in database (list is null or empty)");
                        
                        if (rvTopProductsV2 != null) {
                            rvTopProductsV2.setVisibility(View.GONE);
                        }
                        
                        if (tvTopEmptyV2 != null) {
                            tvTopEmptyV2.setVisibility(View.VISIBLE);
                        }
                    }
                } else {
                    Log.e(TAG, "Error loading top products from database: " + response.code());
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBody);
                        } catch (Exception e) {
                            Log.e(TAG, "Could not read error body", e);
                        }
                    }
                    
                    // Show empty state on error
                    if (rvTopProductsV2 != null) {
                        rvTopProductsV2.setVisibility(View.GONE);
                    }
                    if (tvTopEmptyV2 != null) {
                        tvTopEmptyV2.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<TopProductsResponse> call, Throwable t) {
                Log.e(TAG, "Failed to load top products from database", t);
                Log.e(TAG, "Error message: " + t.getMessage());
                
                // Show empty state on failure
                if (rvTopProductsV2 != null) {
                    rvTopProductsV2.setVisibility(View.GONE);
                }
                if (tvTopEmptyV2 != null) {
                    tvTopEmptyV2.setVisibility(View.VISIBLE);
                }
                
                Toast.makeText(AdminDashboardActivity.this, 
                    "Lỗi tải sản phẩm bán chạy: " + t.getMessage(), 
                    Toast.LENGTH_SHORT).show();
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

    private void loadRevenueChart(int days) {
        Log.d(TAG, "Loading revenue chart for period: " + days + " days");
        
        adminApi.getRevenueChart("Bearer " + token, days).enqueue(new Callback<RevenueChartData>() {
            @Override
            public void onResponse(Call<RevenueChartData> call, Response<RevenueChartData> response) {
                Log.d(TAG, "Revenue chart API response code: " + response.code());
                
                if (response.isSuccessful() && response.body() != null) {
                    RevenueChartData chartData = response.body();
                    Log.d(TAG, "Revenue chart data received: " + chartData.getTitle());
                    Log.d(TAG, "Revenue chart labels: " + (chartData.getLabels() != null ? chartData.getLabels().size() : 0));
                    Log.d(TAG, "Revenue chart data points: " + (chartData.getData() != null ? chartData.getData().size() : 0));
                    
                    if (chartData.getData() != null && !chartData.getData().isEmpty()) {
                        // Calculate total and change percent
                        List<Double> values = chartData.getData();
                        double currentTotal = 0;
                        for (Double value : values) {
                            currentTotal += (value != null ? value : 0);
                        }
                        
                        double changePercent;
                        
                        // Check if backend provided changePercent or previousPeriodTotal
                        if (chartData.getChangePercent() != null) {
                            // Backend calculated the percentage - use it directly
                            changePercent = chartData.getChangePercent();
                            Log.d(TAG, "Using backend-provided change percent: " + changePercent + "%");
                        } else if (chartData.getPreviousPeriodTotal() != null) {
                            // Backend provided previous period total - calculate percentage
                            double previousTotal = chartData.getPreviousPeriodTotal();
                            if (previousTotal == 0) {
                                changePercent = currentTotal > 0 ? 100 : 0;
                            } else {
                                changePercent = ((currentTotal - previousTotal) / previousTotal) * 100;
                            }
                            Log.d(TAG, "Calculated change from previous: current=" + currentTotal + 
                                    ", previous=" + previousTotal + ", change=" + changePercent + "%");
                        } else {
                            // Fallback: Backend doesn't provide comparison data
                            // Use first half vs second half as approximation (NOT ACCURATE)
                            changePercent = calculateChangePercent(values);
                            Log.w(TAG, "Using fallback calculation (first half vs second half). " +
                                    "For accurate comparison, backend should return 'previousPeriodTotal' or 'changePercent'");
                        }
                        
                        displayRevenueChart(chartData.getLabels(), values, days, currentTotal, changePercent);
                    } else {
                        Log.d(TAG, "No revenue data available - showing empty state");
                        showEmptyRevenueChart();
                    }
                } else {
                    Log.e(TAG, "Error loading revenue chart: " + response.code());
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBody);
                        } catch (Exception e) {
                            Log.e(TAG, "Could not read error body", e);
                        }
                    }
                    showEmptyRevenueChart();
                }
            }

            @Override
            public void onFailure(Call<RevenueChartData> call, Throwable t) {
                Log.e(TAG, "Failed to load revenue chart", t);
                showEmptyRevenueChart();
            }
        });
    }
    
    private void displayRevenueChart(List<String> labels, List<Double> values, int days, double total, double changePercent) {
        if (labels == null || values == null || labels.isEmpty() || values.isEmpty()) {
            showEmptyRevenueChart();
            return;
        }
        
        // Update summary UI
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        tvRevenueTotalV2Chart.setText(currencyFormat.format(total));
        
        String changeText = String.format(Locale.US, "%s%.1f%%", 
                changePercent >= 0 ? "+" : "", changePercent);
        tvRevenueChangeV2.setText(changeText);
        int changeColor = changePercent >= 0 ? 
                Color.parseColor("#10B981") : 
                Color.parseColor("#EF4444");
        tvRevenueChangeV2.setTextColor(changeColor);
        
        // Prepare chart entries
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            entries.add(new Entry(i, values.get(i).floatValue()));
        }
        
        // Create dataset
        LineDataSet dataSet = new LineDataSet(entries, "Doanh thu");
        dataSet.setColor(Color.parseColor("#3B82F6")); // Blue line
        dataSet.setCircleColor(Color.parseColor("#3B82F6"));
        dataSet.setCircleRadius(4f);
        dataSet.setCircleHoleRadius(2f);
        dataSet.setLineWidth(2.5f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.LINEAR); // Straight line connecting points
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#3B82F6"));
        dataSet.setFillAlpha(30);
        dataSet.setHighLightColor(Color.parseColor("#1E40AF"));
        dataSet.setHighlightLineWidth(1.5f);
        
        LineData lineData = new LineData(dataSet);
        
        // Configure chart
        lineChartRevenue.setData(lineData);
        lineChartRevenue.setDrawGridBackground(false);
        lineChartRevenue.setDrawBorders(false);
        lineChartRevenue.getLegend().setEnabled(false);
        lineChartRevenue.setTouchEnabled(true);
        lineChartRevenue.setDragEnabled(true);
        lineChartRevenue.setScaleEnabled(false);
        lineChartRevenue.setPinchZoom(false);
        lineChartRevenue.setDoubleTapToZoomEnabled(false);
        
        Description description = new Description();
        description.setText("");
        lineChartRevenue.setDescription(description);
        
        // Configure X axis
        XAxis xAxis = lineChartRevenue.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(Color.parseColor("#9CA3AF"));
        xAxis.setTextSize(10f);
        
        // Set labels for X axis
        final List<String> xLabels = labels;
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < xLabels.size()) {
                    return xLabels.get(index);
                }
                return "";
            }
        });
        
        // Configure Y axis
        YAxis leftAxis = lineChartRevenue.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#E5E7EB"));
        leftAxis.setTextColor(Color.parseColor("#9CA3AF"));
        leftAxis.setTextSize(10f);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value >= 1000000) {
                    return String.format(Locale.US, "%.0fM", value / 1000000);
                } else if (value >= 1000) {
                    return String.format(Locale.US, "%.0fK", value / 1000);
                }
                return String.format(Locale.US, "%.0f", value);
            }
        });
        
        YAxis rightAxis = lineChartRevenue.getAxisRight();
        rightAxis.setEnabled(false);
        
        // Add value selection listener for tooltip
        final NumberFormat tooltipFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        lineChartRevenue.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                int index = (int) e.getX();
                String date = index >= 0 && index < xLabels.size() ? xLabels.get(index) : "";
                String revenue = tooltipFormat.format(e.getY());
                Toast.makeText(AdminDashboardActivity.this, 
                    date + "\n" + revenue, 
                    Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected() {
            }
        });
        
        lineChartRevenue.animateX(800);
        lineChartRevenue.invalidate();
        
        // Show chart, hide empty state
        lineChartRevenue.setVisibility(View.VISIBLE);
        tvRevenueEmptyV2.setVisibility(View.GONE);
        
        Log.d(TAG, "Revenue chart displayed: total=" + total + ", change=" + changePercent + "%");
    }
    
    private double calculateChangePercent(List<Double> data) {
        if (data == null || data.size() < 2) return 0;
        
        int halfSize = data.size() / 2;
        double firstHalfSum = 0;
        double secondHalfSum = 0;
        
        for (int i = 0; i < halfSize; i++) {
            firstHalfSum += (data.get(i) != null ? data.get(i) : 0);
        }
        
        for (int i = halfSize; i < data.size(); i++) {
            secondHalfSum += (data.get(i) != null ? data.get(i) : 0);
        }
        
        if (firstHalfSum == 0) return secondHalfSum > 0 ? 100 : 0;
        
        return ((secondHalfSum - firstHalfSum) / firstHalfSum) * 100;
    }
    
    private void showEmptyRevenueChart() {
        lineChartRevenue.clear();
        lineChartRevenue.setVisibility(View.GONE);
        tvRevenueEmptyV2.setVisibility(View.VISIBLE);
        
        tvRevenueTotalV2Chart.setText("0đ");
        tvRevenueChangeV2.setText("+0%");
        tvRevenueChangeV2.setTextColor(Color.parseColor("#6B7280"));
    }

    private void startAutoRefresh() {
        if (refreshHandler == null) {
            refreshHandler = new Handler(Looper.getMainLooper());
        }
        
        if (refreshRunnable == null) {
            refreshRunnable = new Runnable() {
                @Override
                public void run() {
                    refreshData();
                    refreshHandler.postDelayed(this, REFRESH_INTERVAL);
                }
            };
        }
        
        // Start the refresh cycle
        refreshHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL);
    }
    
    private void stopAutoRefresh() {
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
        }
    }
    
    private void refreshData() {
        Log.d(TAG, "Auto-refreshing dashboard data...");
        loadDashboardStats();
        loadProductStats();
        loadOrderStatusDistribution();
        loadTopProducts();
        loadRevenueChart(currentRevenuePeriod);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayoutV2 != null && drawerLayoutV2.isDrawerOpen(GravityCompat.START)) {
            drawerLayoutV2.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAutoRefresh();
    }

}

