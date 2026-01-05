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
import com.example.nike_fe.data.model.ChatMessage;
import com.example.nike_fe.service.ChatWebSocketService;
import com.example.nike_fe.service.WebSocketChatManager;
import com.example.nike_fe.ui.auth.LoginActivity;
import com.example.nike_fe.ui.chat.ChatBoxFragment;

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
    private RecyclerView rvFilters, rvProductGrid;
    private CircleImageView ivHeaderAvatar;
    private ImageView btnNotification;
    private TextView tvNotificationBadge; // New Badge TextView
    private User currentUser;
    private HomeProductAdapter productAdapter; // Promoted to class level
    private FilterAdapter filterAdapter; // Để cập nhật brands động
    private String currentBrandQuery = null;
    private java.util.Map<String, Category> categoryMap = new java.util.HashMap<>(); // Map tên -> Category

    // Bottom Navigation
    private BottomNavigationView bottomNavigation;
    private FloatingActionButton fabCart;

    // WebSocket Chat (removed old popup variables)
    private FloatingActionButton fabWebSocketChat;
    private TextView tvChatBadge; // Badge for unread messages
    private WebSocketChatManager.UnreadMessageListener unreadListener;

    // Search Fields
    private RecyclerView rvSearchResults;
    private com.example.nike_fe.adapter.SearchProductAdapter searchAdapter;
    private View layoutSearchResults;
    private View layoutMainContent;
    private List<Product> allProducts = new java.util.ArrayList<>();

    // Banner Carousel
    private androidx.viewpager2.widget.ViewPager2 bannerViewPager;
    private com.example.nike_fe.adapter.BannerAdapter bannerAdapter;
    private java.util.List<com.example.nike_fe.data.model.Banner> bannerList;
    private android.os.Handler bannerHandler = new android.os.Handler();
    private Runnable bannerRunnable = new Runnable() {
        @Override
        public void run() {
            if (bannerViewPager != null && bannerList != null && !bannerList.isEmpty()) {
                performSlowScroll();
                // Animation takes 1s. To pause for 3s, total interval must be 1s + 3s = 4s
                bannerHandler.postDelayed(this, 4000);
            }
        }
    };

    // Helper to perform slow scroll
    private void performSlowScroll() {
        if (bannerViewPager == null)
            return;

        // Safety check: Don't drag if already dragging (manually or fake)
        if (bannerViewPager.isFakeDragging())
            return;

        final int width = bannerViewPager.getWidth();
        if (width == 0)
            return;

        // Try to begin fake drag. If returns false, something is preventing it (e.g.
        // user touch)
        if (!bannerViewPager.beginFakeDrag())
            return;

        // Animate from 0 to width over 1 second (1000ms)
        android.animation.ValueAnimator animator = android.animation.ValueAnimator.ofFloat(0f, (float) width);
        animator.setDuration(1000);
        animator.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());

        final float[] previousValue = { 0f };

        animator.addUpdateListener(animation -> {
            float currentValue = (float) animation.getAnimatedValue();
            float delta = currentValue - previousValue[0];
            previousValue[0] = currentValue;

            if (bannerViewPager != null && bannerViewPager.isFakeDragging()) {
                bannerViewPager.fakeDragBy(-delta); // Negative to scroll right
            }
        });

        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                super.onAnimationEnd(animation);
                if (bannerViewPager != null && bannerViewPager.isFakeDragging()) {
                    bannerViewPager.endFakeDrag();
                }
            }

            @Override
            public void onAnimationCancel(android.animation.Animator animation) {
                super.onAnimationCancel(animation);
                if (bannerViewPager != null && bannerViewPager.isFakeDragging()) {
                    bannerViewPager.endFakeDrag();
                }
            }
        });

        animator.start();
    }

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
        // setupFab(); // FAB Removed
        setupHeader();
        // setupBottomNavigation(); // Duplicate remove
        setupBannerCarousel(); // Init Banner
        setupWebSocketChatFab(); // Setup WebSocket Chat FAB
        fetchProducts(null);
        loadUserProfile();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check if coming back from order - need delay for backend to save notification
        boolean needRefresh = getIntent().getBooleanExtra("REFRESH_BADGE", false);
        if (needRefresh) {
            getIntent().removeExtra("REFRESH_BADGE");
            android.util.Log.d("MainActivity", "⏳ Will refresh badge in 3 seconds...");
            // Wait 3000ms for backend to complete async notification creation and commit to
            // DB
            new android.os.Handler().postDelayed(() -> {
                android.util.Log.d("MainActivity", "🔔 Refreshing badge after order (1st attempt)...");
                updateUnreadCount();
                // Try again after another 2 seconds in case first attempt was too early
                new android.os.Handler().postDelayed(() -> {
                    android.util.Log.d("MainActivity", "🔔 Refreshing badge (2nd attempt - safety check)...");
                    updateUnreadCount();
                }, 2000);
            }, 3000);
        } else {
            updateUnreadCount();
        }

        loadUserProfile(); // Refresh profile if needed
        loadCategoriesFromApi(); // Refresh categories when returning to MainActivity

        if (bannerHandler != null && bannerRunnable != null) {
            bannerHandler.postDelayed(bannerRunnable, 3000); // 3s delay initially
        }

        // Register WebSocket listener for badge updates
        setupWebSocketListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop banner auto-scroll to save resources
        if (bannerHandler != null && bannerRunnable != null) {
            bannerHandler.removeCallbacks(bannerRunnable);
        }
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        rvFilters = findViewById(R.id.rvFilters);
        rvProductGrid = findViewById(R.id.rvProductGrid);
        rvSearchResults = findViewById(R.id.rvSearchResults);
        layoutSearchResults = findViewById(R.id.layoutSearchResults);
        layoutMainContent = findViewById(R.id.layoutMainContent);

        ivHeaderAvatar = findViewById(R.id.ivHeaderAvatar);
        btnNotification = findViewById(R.id.btnNotification);
        tvNotificationBadge = findViewById(R.id.tvNotificationBadge); // Init Badge
        tvChatBadge = findViewById(R.id.tvChatBadge); // Init Chat Badge

        // Notification button click
        btnNotification.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
            startActivity(intent);
        });

        bottomNavigation = findViewById(R.id.bottomNavigation);
        fabCart = findViewById(R.id.fabCart);

        // Chat Bot Assistant FAB - Draggable
        FloatingActionButton fabChatBot = findViewById(R.id.fabChat);
        if (fabChatBot != null) {
            setupDraggableFAB(fabChatBot, () -> {
                ChatBoxFragment chatBox = new ChatBoxFragment();
                chatBox.show(getSupportFragmentManager(), "ChatBox");
            });
        }

        // Filter Button Logic
        View btnFilter = findViewById(R.id.btnFilter);
        if (btnFilter != null) {
            btnFilter.setOnClickListener(v -> {
                com.example.nike_fe.ui.home.FilterBottomSheetFragment filterFragment = new com.example.nike_fe.ui.home.FilterBottomSheetFragment();
                filterFragment.setOnApplyFilterListener((min, max, sortOption, selectedSizes) -> {
                    // Fetch products with price filter
                    fetchProducts(currentBrandQuery, (double) min, (double) max);

                    // Wait a bit for products to load, then apply size filter and sort
                    new android.os.Handler().postDelayed(() -> {
                        // Filter by sizes if any selected
                        if (selectedSizes != null && !selectedSizes.isEmpty()) {
                            filterProductsBySize(selectedSizes);
                        }

                        // Apply sort if selected
                        if (sortOption >= 0) {
                            sortProducts(sortOption);
                        }
                    }, 500);
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
                    filterProducts(s.toString());
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {
                }
            });
        }

        // View All Logic
        TextView tvViewAllSpecial = findViewById(R.id.tvViewAllSpecialOffers);
        TextView tvViewAllPopular = findViewById(R.id.tvViewAllPopular);

        View.OnClickListener viewAllListener = v -> {
            Intent intent = new Intent(MainActivity.this, com.example.nike_fe.ui.product.AllProductsActivity.class);
            startActivity(intent);
        };

        if (tvViewAllSpecial != null)
            tvViewAllSpecial.setOnClickListener(v -> startActivity(
                    new Intent(MainActivity.this, com.example.nike_fe.ui.coin.NikeCoinActivity.class)));
        if (tvViewAllPopular != null)
            tvViewAllPopular.setOnClickListener(viewAllListener);

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

    private void setupWebSocketChatFab() {
        fabWebSocketChat = findViewById(R.id.fabWebSocketChat);

        if (fabWebSocketChat != null) {
            setupDraggableFAB(fabWebSocketChat, () -> {
                // Notify manager that chat window is opening
                WebSocketChatManager.getInstance().setChatWindowOpen(true);

                // Check if user is admin
                if (currentUser != null && "ADMIN".equals(currentUser.getRole())) {
                    // Show user list for admin
                    com.example.nike_fe.ui.chat.ChatRoomListFragment chatRoomList = new com.example.nike_fe.ui.chat.ChatRoomListFragment();
                    chatRoomList.show(getSupportFragmentManager(), "ChatRoomList");
                } else {
                    // Show direct chat for regular users
                    com.example.nike_fe.ui.chat.WebSocketChatFragment chatFragment = new com.example.nike_fe.ui.chat.WebSocketChatFragment();
                    chatFragment.show(getSupportFragmentManager(), "WebSocketChat");
                }
            });
        }
    }

    // Helper method to make FAB draggable with click support
    private void setupDraggableFAB(FloatingActionButton fab, Runnable onClick) {
        final float[] dX = { 0 };
        final float[] dY = { 0 };
        final float[] downRawX = { 0 };
        final float[] downRawY = { 0 };
        final boolean[] isDragging = { false };

        fab.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    downRawX[0] = event.getRawX();
                    downRawY[0] = event.getRawY();
                    dX[0] = v.getX() - downRawX[0];
                    dY[0] = v.getY() - downRawY[0];
                    isDragging[0] = false;
                    return true;

                case android.view.MotionEvent.ACTION_MOVE:
                    float moveDeltaX = Math.abs(event.getRawX() - downRawX[0]);
                    float moveDeltaY = Math.abs(event.getRawY() - downRawY[0]);

                    if (moveDeltaX > 10 || moveDeltaY > 10) {
                        isDragging[0] = true;
                    }

                    if (isDragging[0]) {
                        v.animate()
                                .x(event.getRawX() + dX[0])
                                .y(event.getRawY() + dY[0])
                                .setDuration(0)
                                .start();
                    }
                    return true;

                case android.view.MotionEvent.ACTION_UP:
                    if (!isDragging[0]) {
                        // Click - run onClick action
                        onClick.run();
                    }
                    v.performClick();
                    return true;

                default:
                    return false;
            }
        });
    }

    private void setupHeader() {
        // Header logic handled in initViews and loadUserProfile
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
                    tvName.setText("Khách");
                if (tvEmail != null)
                    tvEmail.setText("guest@nike.com");
            }
        }
    }

    private void setupRecyclerViews() {
        // 1. Brands - Removed as per user request

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

        // 3. Search Results
        searchAdapter = new com.example.nike_fe.adapter.SearchProductAdapter(this, product -> {
            Intent intent = new Intent(MainActivity.this, ProductDetailActivity.class);
            intent.putExtra("product_id", product.getId());
            startActivity(intent);
        });
        if (rvSearchResults != null) {
            rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
            rvSearchResults.setAdapter(searchAdapter);
        }
    }

    private void setupBannerCarousel() {
        bannerViewPager = findViewById(R.id.bannerViewPager);
        bannerList = new java.util.ArrayList<>();

        // Dummy Data - Mimicking the original design
        bannerList.add(new com.example.nike_fe.data.model.Banner(
                R.drawable.banner_background,
                "25%",
                "Đặc biệt hôm nay!",
                "Nhận giảm giá cho mỗi đơn hàng,\nchỉ áp dụng hôm nay",
                R.drawable.banner1));

        bannerList.add(new com.example.nike_fe.data.model.Banner(
                R.drawable.banner_background, // Could use different bg if available
                "30%",
                "Bộ sưu tập mới",
                "Khám phá phong cách mới nhất\ntừ Nike Summer Collection",
                R.drawable.banner2));

        bannerList.add(new com.example.nike_fe.data.model.Banner(
                R.drawable.banner_background,
                "40%",
                "Flash Sale",
                "Giảm giá cực sốc trong 24h\nĐừng bỏ lỡ cơ hội!",
                R.drawable.banner3));

        bannerAdapter = new com.example.nike_fe.adapter.BannerAdapter(bannerList);
        bannerViewPager.setAdapter(bannerAdapter);

        // Start in the middle to allow "infinite" scrolling in both directions
        // Ensure it starts at the first item of the sequence
        int middle = Integer.MAX_VALUE / 2;
        int startIndex = middle - (middle % bannerList.size());
        bannerViewPager.setCurrentItem(startIndex, false);

        // Apply Diagonal Transition (Bottom-Left <-> Top-Right)
        bannerViewPager.setPageTransformer(new com.example.nike_fe.ui.animation.DiagonalPageTransformer());
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
        // Save for search
        this.allProducts = new java.util.ArrayList<>(products);

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

        RetrofitClient.getInstance(this).getProductApi()
                .getProducts(brandQuery == null ? "" : brandQuery, minPrice, maxPrice)
                .enqueue(new Callback<List<Product>>() {
                    @Override
                    public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Product> products = response.body();

                            // Log số lượng sản phẩm nhận được
                            Log.d("MainActivity", "Received " + products.size() + " products from API");

                            updateProductList(products);
                        } else {
                            Toast.makeText(MainActivity.this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Product>> call, Throwable t) {
                        Toast.makeText(MainActivity.this, "Lỗi tải sản phẩm: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
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

                        // Initialize WebSocket connection for this user
                        WebSocketChatManager.getInstance().initialize(MainActivity.this, currentUser);
                        Log.d("MainActivity", "✅ WebSocket initialized for user: " + currentUser.getFullName());

                        // Load avatar in main header
                        if (ivHeaderAvatar != null && currentUser.getAvatar() != null
                                && !currentUser.getAvatar().isEmpty()) {
                            String avatarUrl = currentUser.getAvatar();
                            if (!avatarUrl.startsWith("http")) {
                                if (avatarUrl.startsWith("/")) {
                                    avatarUrl = avatarUrl.substring(1);
                                }
                                avatarUrl = RetrofitClient.getInstance(MainActivity.this).getBaseUrl() + avatarUrl;
                            }
                            Glide.with(MainActivity.this)
                                    .load(avatarUrl)
                                    .placeholder(R.drawable.ic_user_placeholder)
                                    .error(R.drawable.ic_user_placeholder)
                                    .into(ivHeaderAvatar);
                        }

                        // Also update Drawer
                        if (navigationView != null) {
                            View headerView = navigationView.getHeaderView(0);
                            if (headerView != null) {
                                TextView tvName = headerView.findViewById(R.id.tvUserName);
                                TextView tvEmail = headerView.findViewById(R.id.tvUserEmail);
                                CircleImageView ivDrawerAvatar = headerView.findViewById(R.id.ivUserAvatar);

                                if (tvName != null)
                                    tvName.setText(currentUser.getFullName());
                                if (tvEmail != null)
                                    tvEmail.setText(currentUser.getEmail());

                                // Load avatar in drawer
                                if (ivDrawerAvatar != null && currentUser.getAvatar() != null
                                        && !currentUser.getAvatar().isEmpty()) {
                                    String drawerAvatarUrl = currentUser.getAvatar();
                                    if (!drawerAvatarUrl.startsWith("http")) {
                                        if (drawerAvatarUrl.startsWith("/")) {
                                            drawerAvatarUrl = drawerAvatarUrl.substring(1);
                                        }
                                        drawerAvatarUrl = RetrofitClient.getInstance(MainActivity.this).getBaseUrl()
                                                + drawerAvatarUrl;
                                    }
                                    Glide.with(MainActivity.this)
                                            .load(drawerAvatarUrl)
                                            .placeholder(R.drawable.ic_user_placeholder)
                                            .error(R.drawable.ic_user_placeholder)
                                            .into(ivDrawerAvatar);
                                }
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

    private void filterProducts(String query) {
        if (query == null || query.trim().isEmpty()) {
            if (layoutSearchResults != null)
                layoutSearchResults.setVisibility(View.GONE);
            if (layoutMainContent != null)
                layoutMainContent.setVisibility(View.VISIBLE);
            return;
        }

        if (layoutSearchResults != null)
            layoutSearchResults.setVisibility(View.VISIBLE);
        if (layoutMainContent != null)
            layoutMainContent.setVisibility(View.GONE);

        if (allProducts != null) {
            List<Product> filtered = new java.util.ArrayList<>();
            String lower = query.toLowerCase().trim();
            for (Product p : allProducts) {
                if (p.getName().toLowerCase().contains(lower)) {
                    filtered.add(p);
                }
            }
            if (searchAdapter != null) {
                searchAdapter.setProducts(filtered);
            }
        }
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
            Toast.makeText(this, "Sắp ra mắt", Toast.LENGTH_SHORT).show();
        }

        if (drawerLayout != null)
            drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void sortProducts(int sortOption) {
        if (allProducts == null || allProducts.isEmpty())
            return;

        java.util.Collections.sort(allProducts, (p1, p2) -> {
            switch (sortOption) {
                case 0: // Mới nhất (Newest) - Mock by ID descending
                    return Long.compare(p2.getId(), p1.getId());
                case 1: // Bán chạy nhất - Mock
                    return 0; // No sales data yet
                case 2: // Giá: Thấp đến Cao
                    return Double.compare(p1.getPrice(), p2.getPrice());
                case 3: // Giá: Cao đến Thấp
                    return Double.compare(p2.getPrice(), p1.getPrice());
                default:
                    return 0;
            }
        });

        // Re-apply filter if any text in search box
        android.widget.EditText etSearch = findViewById(R.id.etSearch);
        if (etSearch != null && etSearch.getText().length() > 0) {
            filterProducts(etSearch.getText().toString());
        } else {
            // Update main list
            if (productAdapter != null) {
                productAdapter.setProducts(allProducts);
            }
        }
    }

    private void filterProductsBySize(java.util.List<String> selectedSizes) {
        if (allProducts == null || allProducts.isEmpty() || selectedSizes == null || selectedSizes.isEmpty())
            return;

        Log.d("MainActivity", "Filtering by sizes: " + selectedSizes.toString());
        Log.d("MainActivity", "Total products to filter: " + allProducts.size());

        // Filter products that have at least one of the selected sizes
        java.util.List<Product> filteredProducts = new java.util.ArrayList<>();
        for (Product product : allProducts) {
            Log.d("MainActivity", "Product: " + product.getName() + " has sizes: " +
                    (product.getSizes() != null ? product.getSizes().toString() : "null"));

            if (product.getSizes() != null && !product.getSizes().isEmpty()) {
                // Check if product has any of the selected sizes
                for (String selectedSize : selectedSizes) {
                    if (product.getSizes().contains(selectedSize)) {
                        filteredProducts.add(product);
                        Log.d("MainActivity", "  -> MATCH! Found size: " + selectedSize);
                        break; // Found a match, add product and move to next
                    }
                }
            }
        }

        Log.d("MainActivity", "Filtered " + filteredProducts.size() + " products with selected sizes");

        // Update adapter with filtered list
        if (productAdapter != null) {
            productAdapter.setProducts(filteredProducts);
        }
    }

    private void updateUnreadCount() {
        android.util.Log.d("MainActivity", "========================================");
        android.util.Log.d("MainActivity", "🔔 updateUnreadCount() called");
        android.util.Log.d("MainActivity", "   Badge view is " + (tvNotificationBadge == null ? "NULL ❌" : "OK ✅"));

        String token = RetrofitClient.getInstance(this).getToken();
        android.util.Log.d("MainActivity", "   Token: " + (token == null ? "NULL" : "EXISTS ✅"));

        if (token == null) {
            android.util.Log.d("MainActivity", "   No token - hiding badge");
            if (tvNotificationBadge != null)
                tvNotificationBadge.setVisibility(View.GONE);
            return;
        }

        android.util.Log.d("MainActivity", "🌐 Calling API: /api/notifications/count-unread");

        RetrofitClient.getInstance(this).getNotificationApi().getUnreadCount("Bearer " + token)
                .enqueue(new Callback<com.example.nike_fe.data.model.UnreadCountResponse>() {
                    @Override
                    public void onResponse(Call<com.example.nike_fe.data.model.UnreadCountResponse> call,
                            Response<com.example.nike_fe.data.model.UnreadCountResponse> response) {
                        android.util.Log.d("MainActivity", "📥 API Response code: " + response.code());

                        if (response.isSuccessful() && response.body() != null) {
                            long count = response.body().getCount();
                            android.util.Log.d("MainActivity", "✅ API Response - Unread count: " + count);

                            if (tvNotificationBadge != null) {
                                android.util.Log.d("MainActivity", "   Setting badge text to: " + count);
                                if (count > 0) {
                                    tvNotificationBadge.setText(String.valueOf(count));
                                    tvNotificationBadge.setVisibility(View.VISIBLE);
                                    android.util.Log.d("MainActivity", "🔴 Badge set to VISIBLE with number: " + count);
                                } else {
                                    tvNotificationBadge.setVisibility(View.GONE);
                                    android.util.Log.d("MainActivity", "⭕ Badge HIDDEN (no notifications)");
                                }
                            } else {
                                android.util.Log.e("MainActivity", "❌ Badge view NULL after API response!");
                            }
                        } else {
                            android.util.Log.e("MainActivity", "❌ API failed: " + response.code());
                            try {
                                if (response.errorBody() != null) {
                                    android.util.Log.e("MainActivity", "Error: " + response.errorBody().string());
                                }
                            } catch (Exception e) {
                                android.util.Log.e("MainActivity", "Can't read error body");
                            }
                        }
                        android.util.Log.d("MainActivity", "========================================");
                    }

                    @Override
                    public void onFailure(Call<com.example.nike_fe.data.model.UnreadCountResponse> call, Throwable t) {
                        android.util.Log.e("MainActivity", "❌ Network error: " + t.getMessage());
                        t.printStackTrace();
                        // Ignore error, just hide badge
                        if (tvNotificationBadge != null)
                            tvNotificationBadge.setVisibility(View.GONE);
                        android.util.Log.d("MainActivity", "========================================");
                    }
                });
    }

    private void showSignOutDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    // Clear chat data before logout
                    com.example.nike_fe.ui.chat.WebSocketChatFragment.clearChatHistory();
                    WebSocketChatManager.getInstance().disconnect();

                    RetrofitClient.getInstance(this).clearToken();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void setupWebSocketListener() {
        if (unreadListener == null) {
            unreadListener = new WebSocketChatManager.UnreadMessageListener() {
                @Override
                public void onUnreadCountChanged(int count) {
                    runOnUiThread(() -> updateChatBadge(count));
                }

                @Override
                public void onNewMessage(ChatMessage message) {
                    // Can add notification sound/vibration here if needed
                    Log.d("MainActivity", "📩 New message: " + message.getContent());
                }

                @Override
                public void onUserUnreadChanged(Long userId, int count) {
                    // This is for ChatRoomListFragment to update per-user badges
                    // MainActivity only shows total count
                }
            };
            WebSocketChatManager.getInstance().addListener(unreadListener);
        }

        // Update badge with current count
        updateChatBadge(WebSocketChatManager.getInstance().getUnreadCount());
    }

    private void updateChatBadge(int count) {
        if (tvChatBadge != null) {
            if (count > 0) {
                tvChatBadge.setText(String.valueOf(count));
                tvChatBadge.setVisibility(View.VISIBLE);
            } else {
                tvChatBadge.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Remove WebSocket listener
        if (unreadListener != null) {
            WebSocketChatManager.getInstance().removeListener(unreadListener);
        }

        // Stop banner animation
        if (bannerHandler != null) {
            bannerHandler.removeCallbacks(bannerRunnable);
        }
    }
}