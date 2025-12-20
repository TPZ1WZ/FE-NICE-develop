package com.example.nike_fe;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nike_fe.adapter.HomeProductAdapter;
import com.example.nike_fe.data.api.RetrofitClient;
import com.example.nike_fe.data.api.UserApi;
import com.example.nike_fe.data.model.Category;
import com.example.nike_fe.data.model.Product;
import com.example.nike_fe.data.model.User;
import com.example.nike_fe.ui.auth.LoginActivity;
import com.example.nike_fe.ui.home.BrandAdapter;
import com.example.nike_fe.ui.home.FilterAdapter;
import com.example.nike_fe.ui.profile.ProfileActivity;
import com.example.nike_fe.ui.product.ProductDetailActivity;
import com.example.nike_fe.ui.cart.CartActivity;
import com.example.nike_fe.ui.notification.NotificationActivity;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Arrays;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private RecyclerView rvBrands, rvFilters, rvProductGrid;
    private TextView tvHeaderName;
    private CircleImageView ivHeaderAvatar;
    private ImageView btnNotification;
    private User currentUser;
    private HomeProductAdapter productAdapter; // Promoted to class level
    private FilterAdapter filterAdapter; // Để cập nhật brands động
    private String currentBrandQuery = null;
    private java.util.Map<String, Category> categoryMap = new java.util.HashMap<>(); // Map tên -> Category

    // Bottom Navigation
    private BottomNavigationView bottomNavigation;
    private FloatingActionButton fabCart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupNavigationDrawer();
        setupRecyclerViews();
        setupBottomNavigation();
        setupBottomNavigation();
        fetchCategories();
        fetchProducts(null);
        loadUserProfile();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        rvBrands = findViewById(R.id.rvBrands);
        rvFilters = findViewById(R.id.rvFilters);
        rvProductGrid = findViewById(R.id.rvProductGrid);

        tvHeaderName = findViewById(R.id.tvHeaderName);
        ivHeaderAvatar = findViewById(R.id.ivHeaderAvatar);
        btnNotification = findViewById(R.id.btnNotification);

        // Notification button click
        btnNotification.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
            startActivity(intent);
        });

        bottomNavigation = findViewById(R.id.bottomNavigation);
        fabCart = findViewById(R.id.fabCart);

        // Filter Button Logic
        View btnFilter = findViewById(R.id.btnFilter);
        if (btnFilter != null) {
            btnFilter.setOnClickListener(v -> {
                com.example.nike_fe.ui.home.FilterBottomSheetFragment filterFragment = new com.example.nike_fe.ui.home.FilterBottomSheetFragment();
                filterFragment.setOnApplyFilterListener((min, max) -> {
                    fetchProducts(currentBrandQuery, (double) min, (double) max);
                });
                filterFragment.show(getSupportFragmentManager(), "filter_dialog");
            });
        }

        // ... (rest of initViews) ...
        // Note: Search Logic should update currentBrandQuery too?
        // Yes, but for now let's just make sure filter works with brands.
        // If search is used, reset brand query to search text.

        // Search Logic
        android.widget.EditText etSearch = findViewById(R.id.etSearch);
        if (etSearch != null) {
            etSearch.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Local filtering for search as implemented before...
                    // Or should we call API? The original code did local filtering on adapter.
                    if (productAdapter != null) {
                        productAdapter.filter(s.toString());
                    }
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {
                }
            });
        }

        // Open drawer when clicking avatar
        if (ivHeaderAvatar != null) {
            ivHeaderAvatar.setOnClickListener(v -> {
                if (drawerLayout != null)
                    drawerLayout.openDrawer(GravityCompat.START);
            });
        }
    }

    private void setupBottomNavigation() {
        if (bottomNavigation != null) {
            bottomNavigation.setBackground(null); // Clear background for FAB curve
            bottomNavigation.getMenu().getItem(2).setEnabled(false); // Disable placeholder item for FAB

            bottomNavigation.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    return true;
                } else if (id == R.id.nav_favorites) {
                    startActivity(new Intent(this, com.example.nike_fe.ui.favorite.FavoriteActivity.class));
                    return true;
                } else if (id == R.id.nav_notifications) {
                    Toast.makeText(this, "Notifications", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.nav_profile) {
                    startActivity(new Intent(this, ProfileActivity.class));
                    return true;
                }
                return false;
            });
        }

        if (fabCart != null) {
            fabCart.setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));
        }
    }

    private void setupNavigationDrawer() {
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);

            // Initial dummy data
            View headerView = navigationView.getHeaderView(0);
            if (headerView != null) {
                TextView tvName = headerView.findViewById(R.id.tvUserName);
                TextView tvEmail = headerView.findViewById(R.id.tvUserEmail);
                if (tvName != null)
                    tvName.setText("Guest");
                if (tvEmail != null)
                    tvEmail.setText("guest@nike.com");
            }
        }
    }

    private void setupRecyclerViews() {
        // 1. Brands
        BrandAdapter brandAdapter = new BrandAdapter(this, new java.util.ArrayList<>(), category -> {
            // Update current query
            currentBrandQuery = category.getName();
            Toast.makeText(this, "Filter: " + category.getName(), Toast.LENGTH_SHORT).show();
            fetchProducts(currentBrandQuery, null, null);
        });

        if (rvBrands != null) {
            rvBrands.setLayoutManager(new GridLayoutManager(this, 4));
            rvBrands.setAdapter(brandAdapter);
        }

        // 2. Filters - Load từ API
        List<String> initialFilters = Arrays.asList("All"); // Chỉ có "All" ban đầu
        filterAdapter = new FilterAdapter(this, initialFilters, filter -> {
            if (filter.equals("All")) {
                // Hiển thị tất cả sản phẩm
                fetchProducts(null, null, null);
            } else {
                // Filter theo category - lấy category ID từ map
                Category selectedCategory = categoryMap.get(filter);
                if (selectedCategory != null) {
                    fetchProductsByCategory(selectedCategory.getId(), null, null);
                } else {
                    fetchProducts(null, null, null);
                }
            }
        });

        if (rvFilters != null) {
            rvFilters.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            rvFilters.setAdapter(filterAdapter);
        }

        // Load categories từ API
        loadCategoriesFromApi();

        // ... (rest of setupRecyclerViews)
    }

    /**
     * Load danh sách danh mục từ API backend
     */
    private void loadCategoriesFromApi() {
        RetrofitClient.getInstance(this).getProductApi().getCategories()
                .enqueue(new Callback<List<Category>>() {
                    @Override
                    public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Category> categories = response.body();

                            // Lưu category map
                            categoryMap.clear();

                            // Chuyển thành danh sách tên danh mục
                            List<String> filters = new java.util.ArrayList<>();
                            filters.add("All");
                            for (Category category : categories) {
                                filters.add(category.getName());
                                categoryMap.put(category.getName(), category);
                            }

                            // Cập nhật adapter
                            if (filterAdapter != null) {
                                filterAdapter.updateFilters(filters);
                            }

                            Log.d("MainActivity", "Loaded " + categories.size() + " categories from API");
                        } else {
                            Log.e("MainActivity", "Failed to load categories: " + response.code());
                            Toast.makeText(MainActivity.this, "Không thể tải danh sách danh mục", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Category>> call, Throwable t) {
                        Log.e("MainActivity", "Error loading categories: " + t.getMessage());
                        // Giữ nguyên danh sách "All" nếu lỗi
                    }
                });
    }

    /**
     * Cập nhật danh sách sản phẩm hiển thị
     */
    private void updateProductList(List<Product> products) {
        productAdapter = new HomeProductAdapter(MainActivity.this,
                new HomeProductAdapter.OnProductClickListener() {
                    @Override
                    public void onProductClick(Product product) {
                        Intent intent = new Intent(MainActivity.this, ProductDetailActivity.class);
                        intent.putExtra("product_id", product.getId());
                        startActivity(intent);
                    }

                    @Override
                    public void onAddClick(Product product) {
                        String token = RetrofitClient.getInstance(MainActivity.this).getToken();
                        if (token == null) {
                            Toast.makeText(MainActivity.this, "Vui lòng đăng nhập trước",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Determine size: use first available or default "42"
                        String tempSize = "42";
                        if (product.getSizes() != null && !product.getSizes().isEmpty()) {
                            tempSize = product.getSizes().get(0);
                        }
                        final String selectedSize = tempSize;

                        int quantity = 1;

                        com.example.nike_fe.data.model.AddToCartRequest request = new com.example.nike_fe.data.model.AddToCartRequest(
                                product.getId(), quantity, selectedSize);

                        RetrofitClient.getInstance(MainActivity.this).getCartApi()
                                .addToCart("Bearer " + token, request)
                                .enqueue(new Callback<java.util.Map<String, String>>() {
                                    @Override
                                    public void onResponse(Call<java.util.Map<String, String>> call,
                                            Response<java.util.Map<String, String>> response) {
                                        if (response.isSuccessful()) {
                                            Toast.makeText(MainActivity.this,
                                                    "Đã thêm vào giỏ (Size " + selectedSize + ")",
                                                    Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(MainActivity.this,
                                                    "Thêm vào giỏ thất bại", Toast.LENGTH_SHORT)
                                                    .show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<java.util.Map<String, String>> call,
                                            Throwable t) {
                                        Toast.makeText(MainActivity.this,
                                                "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                });
                    }

                    @Override
                    public void onFavoriteClick(Product product) {
                        String token = RetrofitClient.getInstance(MainActivity.this).getToken();
                        if (token == null) {
                            Toast.makeText(MainActivity.this, "Vui lòng đăng nhập để yêu thích", Toast.LENGTH_SHORT)
                                    .show();
                            return;
                        }

                        // Check if favorite first
                        RetrofitClient.getInstance(MainActivity.this).getFavoriteApi()
                                .checkFavorite(product.getId(), "Bearer " + token)
                                .enqueue(new Callback<java.util.Map<String, Object>>() {
                                    @Override
                                    public void onResponse(Call<java.util.Map<String, Object>> call,
                                            Response<java.util.Map<String, Object>> response) {
                                        if (response.isSuccessful() && response.body() != null) {
                                            Boolean isFavorite = (Boolean) response.body().get("isFavorite");
                                            if (isFavorite != null && isFavorite) {
                                                // Remove
                                                removeFromFavorite(product.getId(), token);
                                            } else {
                                                // Add
                                                addToFavorite(product.getId(), token);
                                            }
                                        } else {
                                            // Assume not favorite, try to add
                                            addToFavorite(product.getId(), token);
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<java.util.Map<String, Object>> call, Throwable t) {
                                        Toast.makeText(MainActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });

        productAdapter.useGridLayout(true);
        productAdapter.setProducts(products);

        if (rvProductGrid != null) {
            rvProductGrid.setAdapter(productAdapter);
        }
    }

    /**
     * Load sản phẩm theo category ID
     */
    private void fetchProductsByCategory(Long categoryId, Double minPrice, Double maxPrice) {
        Log.d("MainActivity", "=== Fetching products for categoryId: " + categoryId + " ===");

        Call<List<Product>> call = RetrofitClient.getInstance(this).getProductApi().getProductsByCategory(categoryId,
                minPrice, maxPrice);
        Log.d("MainActivity", "API URL: " + call.request().url());

        call
                .enqueue(new Callback<List<Product>>() {
                    @Override
                    public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                        Log.d("MainActivity", "Response code: " + response.code());
                        if (response.isSuccessful() && response.body() != null) {
                            List<Product> products = response.body();
                            Log.d("MainActivity", "SUCCESS: Received " + products.size() + " products from API");
                            if (products.isEmpty()) {
                                Toast.makeText(MainActivity.this, "Danh mục này chưa có sản phẩm", Toast.LENGTH_SHORT)
                                        .show();
                            }
                            updateProductList(products);
                        } else {
                            Log.e("MainActivity", "ERROR: Response not successful. Code: " + response.code());
                            try {
                                String errorBody = response.errorBody() != null ? response.errorBody().string()
                                        : "No error body";
                                Log.e("MainActivity", "Error body: " + errorBody);
                            } catch (Exception e) {
                                Log.e("MainActivity", "Cannot read error body", e);
                            }
                            Toast.makeText(MainActivity.this, "Không tìm thấy sản phẩm (Lỗi: " + response.code() + ")",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Product>> call, Throwable t) {
                        Log.e("MainActivity", "FAILURE: " + t.getMessage(), t);
                        Toast.makeText(MainActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        // Fallback: load all products
                        fetchProducts(null, null, null);
                    }
                });
    }

    // Overload for backward compatibility if needed, or just update calls.
    private void fetchProducts(String brandQuery) {
        fetchProducts(brandQuery, null, null);
    }

    private void fetchProducts(String brandQuery, Double minPrice, Double maxPrice) {
        // Log querying for debug
        Log.d("MainActivity", "Fetching products for: " + brandQuery + ", Price: " + minPrice + "-" + maxPrice);

        RetrofitClient.getInstance(this).getProductApi().getProducts(brandQuery, minPrice, maxPrice)
                .enqueue(new Callback<List<Product>>() {
                    @Override
                    public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Product> products = response.body();

                            // Log số lượng sản phẩm nhận được
                            Log.d("MainActivity", "Received " + products.size() + " products from API");

                            updateProductList(products);
                        } else {
                            Toast.makeText(MainActivity.this, "No products found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Product>> call, Throwable t) {
                        Toast.makeText(MainActivity.this, "Error loading products: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchCategories() {
        RetrofitClient.getInstance(this).getCategoryApi().getCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Category> categories = response.body();

                    BrandAdapter brandAdapter = new BrandAdapter(MainActivity.this, categories, category -> {
                        // Fetch products by category ID
                        fetchProductsByCategory(category.getId(), null, null);
                        Toast.makeText(MainActivity.this, "Lọc theo: " + category.getName(), Toast.LENGTH_SHORT).show();
                    });

                    if (rvBrands != null) {
                        rvBrands.setAdapter(brandAdapter);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserProfile() {
        String token = RetrofitClient.getInstance(this).getToken();
        if (token != null) {
            RetrofitClient.getInstance(this).getUserApi().getProfile("Bearer " + token).enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        currentUser = response.body();
                        if (tvHeaderName != null)
                            tvHeaderName.setText(currentUser.getFullName());

                        // Also update Drawer
                        if (navigationView != null) {
                            View headerView = navigationView.getHeaderView(0);
                            if (headerView != null) {
                                TextView tvName = headerView.findViewById(R.id.tvUserName);
                                TextView tvEmail = headerView.findViewById(R.id.tvUserEmail);
                                if (tvName != null)
                                    tvName.setText(currentUser.getFullName());
                                if (tvEmail != null)
                                    tvEmail.setText(currentUser.getEmail());
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                }
            });
        }

    }

    private void addToFavorite(Long productId, String token) {
        RetrofitClient.getInstance(this).getFavoriteApi().addToFavorites(productId, "Bearer " + token)
                .enqueue(new Callback<java.util.Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<java.util.Map<String, Object>> call,
                            Response<java.util.Map<String, Object>> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Đã thêm vào yêu thích ❤️", Toast.LENGTH_SHORT).show();
                            if (productAdapter != null) {
                                productAdapter.toggleFavorite(productId, true);
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Thất bại: " + response.code(), Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }

                    @Override
                    public void onFailure(Call<java.util.Map<String, Object>> call, Throwable t) {
                        Toast.makeText(MainActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void removeFromFavorite(Long productId, String token) {
        RetrofitClient.getInstance(this).getFavoriteApi().removeFromFavorites(productId, "Bearer " + token)
                .enqueue(new Callback<java.util.Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<java.util.Map<String, Object>> call,
                            Response<java.util.Map<String, Object>> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Đã xóa khỏi yêu thích 💔", Toast.LENGTH_SHORT).show();
                            if (productAdapter != null) {
                                productAdapter.toggleFavorite(productId, false);
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Thất bại: " + response.code(), Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }

                    @Override
                    public void onFailure(Call<java.util.Map<String, Object>> call, Throwable t) {
                        Toast.makeText(MainActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
        } else if (id == R.id.nav_cart) {
            startActivity(new Intent(this, CartActivity.class));
        } else if (id == R.id.nav_favorites) {
            startActivity(new Intent(this, com.example.nike_fe.ui.favorite.FavoriteActivity.class));
        } else if (id == R.id.nav_orders) {
            startActivity(new Intent(this, com.example.nike_fe.ui.order.OrderHistoryActivity.class));
        } else if (id == R.id.nav_notifications) {
            startActivity(new Intent(this, com.example.nike_fe.ui.notification.NotificationActivity.class));
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, com.example.nike_fe.ui.settings.SettingsActivity.class));
        } else if (id == R.id.nav_sign_out) {
            showSignOutDialog();
        } else {
            Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show();
        }

        if (drawerLayout != null)
            drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showSignOutDialog() {
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