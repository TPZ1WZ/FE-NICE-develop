package com.example.nike_fe.ui.product;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import com.example.nike_fe.R;
import com.example.nike_fe.adapter.ImageGalleryAdapter;
import com.example.nike_fe.data.api.CartApi;
import com.example.nike_fe.data.api.FavoriteApi;
import com.example.nike_fe.data.api.ProductApi;
import com.example.nike_fe.data.api.RetrofitClient;
import com.example.nike_fe.data.model.AddToCartRequest;
import com.example.nike_fe.data.model.ProductDetail;
import com.example.nike_fe.ui.cart.CartActivity;
import com.example.nike_fe.adapter.ProductReviewAdapter;
import com.example.nike_fe.data.api.UserReviewApi;
import com.example.nike_fe.data.model.Review;
import com.example.nike_fe.data.model.ReviewSummary;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT_ID = "product_id";

    // UI Components
    private ViewPager2 vpProductImages;
    private LinearLayout llImageIndicator;
    private TextView tvProductName, tvPrice;
    private TextView tvDescription, tvTotalPrice;
    private View btnAddToCart; // Changed to View/LinearLayout
    private ImageView ivBack, ivFavorite;
    private ProgressBar progressBar;

    // Reviews
    private RecyclerView rvReviews;
    private TextView tvRatingSummary;
    private TextView tvNoReviews;

    // Quantity Controls
    private ImageView btnMinus, btnPlus;
    private TextView tvQuantityHolder;

    // Size Chips
    private ChipGroup chipGroupSizes;

    private ImageGalleryAdapter imageAdapter;
    private ProductReviewAdapter reviewAdapter;
    private ProductApi productApi;
    private CartApi cartApi;
    private FavoriteApi favoriteApi;
    private UserReviewApi userReviewApi;
    private String token;
    private boolean isFavorite = false;
    private Long productId;
    private ProductDetail productDetail;

    private int quantity = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Get product ID from intent
        productId = getIntent().getLongExtra(EXTRA_PRODUCT_ID, -1);
        if (productId == -1) {
            Toast.makeText(this, "Lỗi: Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupListeners(); // Separated setup
        setupImageGallery();
        loadProductDetail();
        loadReviews();
        loadReviewSummary();
    }

    private void setupReviewList() {
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        reviewAdapter = new ProductReviewAdapter(this, new ArrayList<>());
        rvReviews.setAdapter(reviewAdapter);
    }

    private void loadReviews() {
        userReviewApi.getProductReviews(productId).enqueue(new Callback<List<Review>>() {
            @Override
            public void onResponse(Call<List<Review>> call, Response<List<Review>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Review> reviews = response.body();
                    if (reviews.isEmpty()) {
                        tvNoReviews.setVisibility(View.VISIBLE);
                        rvReviews.setVisibility(View.GONE);
                    } else {
                        tvNoReviews.setVisibility(View.GONE);
                        rvReviews.setVisibility(View.VISIBLE);
                        reviewAdapter.setReviews(reviews);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Review>> call, Throwable t) {
                // Ignore API failures for reviews to not block UI
                android.util.Log.e("ProductDetail", "Failed to load reviews", t);
            }
        });
    }

    private void loadReviewSummary() {
        userReviewApi.getReviewSummary(productId).enqueue(new Callback<ReviewSummary>() {
            @Override
            public void onResponse(Call<ReviewSummary> call, Response<ReviewSummary> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ReviewSummary summary = response.body();
                    tvRatingSummary.setText(String.format(Locale.getDefault(), "%.1f (%d đánh giá)",
                            summary.getAverageRating(), summary.getTotalReviews()));
                }
            }

            @Override
            public void onFailure(Call<ReviewSummary> call, Throwable t) {
                // Ignore
            }
        });
    }

    private void initViews() {
        vpProductImages = findViewById(R.id.vpProductImages);
        llImageIndicator = findViewById(R.id.llImageIndicator);
        tvProductName = findViewById(R.id.tvProductName);
        // tvProductSubtitle removed in new design or not strictly needed to bind if
        // mocked
        tvPrice = findViewById(R.id.tvPrice); // Unit price (hidden or used for calc)
        tvTotalPrice = findViewById(R.id.tvTotalPrice); // Displayed at bottom
        tvDescription = findViewById(R.id.tvDescription);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        chipGroupSizes = findViewById(R.id.chipGroupSizes);
        ivBack = findViewById(R.id.ivBack);
        ivFavorite = findViewById(R.id.ivFavorite);
        progressBar = findViewById(R.id.progressBar);

        // Reviews
        rvReviews = findViewById(R.id.rvReviews);
        tvRatingSummary = findViewById(R.id.tvRatingSummary);
        tvNoReviews = findViewById(R.id.tvNoReviews);

        setupReviewList();

        // Quantity Controls
        btnMinus = findViewById(R.id.btnMinus);
        btnPlus = findViewById(R.id.btnPlus);
        tvQuantityHolder = findViewById(R.id.tvQuantity);

        // Retrofit
        RetrofitClient retrofitClient = RetrofitClient.getInstance(this);
        productApi = retrofitClient.getProductApi();
        favoriteApi = retrofitClient.getFavoriteApi();
        cartApi = retrofitClient.getCartApi();
        userReviewApi = retrofitClient.getUserReviewApi();
        String rawToken = retrofitClient.getToken();
        token = (rawToken != null && !rawToken.startsWith("Bearer ")) ? "Bearer " + rawToken : rawToken;
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> onBackPressed());

        ivFavorite.setOnClickListener(v -> {
            if (token == null || token.isEmpty()) {
                Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, com.example.nike_fe.ui.auth.LoginActivity.class));
                return;
            }
            toggleFavorite();
        });

        btnAddToCart.setOnClickListener(v -> {
            android.util.Log.d("ProductDetail", "🔘 Add to Cart button clicked");
            android.util.Log.d("ProductDetail", "🔐 Token status: " + (token != null ? "Present" : "NULL"));
            android.util.Log.d("ProductDetail",
                    "📦 Product Detail status: " + (productDetail != null ? "Loaded" : "NULL"));

            if (token == null || token.isEmpty()) {
                android.util.Log.w("ProductDetail", "⚠️ No token found, redirecting to login");
                Toast.makeText(this, "Vui lòng đăng nhập để mua hàng", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, com.example.nike_fe.ui.auth.LoginActivity.class));
                return;
            }
            if (productDetail == null) {
                android.util.Log.w("ProductDetail", "⚠️ Product detail not loaded yet");
                Toast.makeText(this, "Vui lòng đợi sản phẩm tải xong", Toast.LENGTH_SHORT).show();
                return;
            }
            addToCart();
        });

        // Quantity Logic
        btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                updateQuantityAndPrice();
            }
        });

        btnPlus.setOnClickListener(v -> {
            // Optional: Check stock limit
            quantity++;
            updateQuantityAndPrice();
        });
    }

    private void updateQuantityAndPrice() {
        tvQuantityHolder.setText(String.valueOf(quantity));
        if (productDetail != null) {
            double total = productDetail.getPrice() * quantity;
            NumberFormat formatter = NumberFormat.getInstance(Locale.US);
            // Assuming currency is USD for design match, or allow Locale based
            // For now, matching the design "$105.00" style if possible, or keeping VND if
            // backend is VND
            // Design shows dollar sign, but existing code used VND. Let's stick to
            // formatted price.
            if (total > 10000) { // Likely VND
                NumberFormat vnFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
                tvTotalPrice.setText(vnFormat.format(total) + "₫");
            } else {
                tvTotalPrice.setText("$" + formatter.format(total));
            }
        }
    }

    private void setupImageGallery() {
        imageAdapter = new ImageGalleryAdapter(this);
        vpProductImages.setAdapter(imageAdapter);

        vpProductImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateImageIndicator(position);
            }
        });
    }

    private void loadProductDetail() {
        progressBar.setVisibility(View.VISIBLE);

        productApi.getProductById(productId).enqueue(new Callback<ProductDetail>() {
            @Override
            public void onResponse(Call<ProductDetail> call, Response<ProductDetail> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    productDetail = response.body();
                    displayProductDetail();
                } else {
                    Toast.makeText(ProductDetailActivity.this, "Error loading product", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProductDetail> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ProductDetailActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayProductDetail() {
        tvProductName.setText(productDetail.getName());

        // Format price
        updateQuantityAndPrice(); // Init price view

        if (productDetail.getDescription() != null && !productDetail.getDescription().isEmpty()) {
            tvDescription.setText(productDetail.getDescription());
        }

        // Load images
        if (productDetail.getImages() != null && !productDetail.getImages().isEmpty()) {
            imageAdapter.setImages(productDetail.getImages());
            createImageIndicators(productDetail.getImages().size());
            updateImageIndicator(0);
        } else {
            List<String> placeholderImages = new ArrayList<>();
            placeholderImages.add("");
            imageAdapter.setImages(placeholderImages);
        }

        // Dynamically add size chips
        chipGroupSizes.removeAllViews();
        List<String> sizes = productDetail.getSizes();
        if (sizes == null || sizes.isEmpty()) {
            sizes = new ArrayList<>();
            sizes.add("40");
            sizes.add("41");
            sizes.add("42");
            sizes.add("43");
        }

        for (String size : sizes) {
            addSizeChip(size);
        }

        // Auto-select first size
        if (chipGroupSizes.getChildCount() > 0) {
            ((Chip) chipGroupSizes.getChildAt(0)).setChecked(true);
        }
    }

    private void addSizeChip(String size) {
        Chip chip = new Chip(this);
        chip.setId(View.generateViewId());
        chip.setText(size);
        chip.setCheckable(true);
        chip.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        // Basic Styling
        chip.setTextColor(android.graphics.Color.BLACK);
        chip.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE));

        // Corner Radius (25dp)
        float cornerRadiusPx = dpToPx(25);
        chip.setChipCornerRadius(cornerRadiusPx);

        // Stroke
        chip.setChipStrokeWidth((int) dpToPx(1));
        chip.setChipStrokeColor(
                android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E0E0E0")));

        // Layout: 45dp x 45dp
        int sizePx = (int) dpToPx(45);
        ChipGroup.LayoutParams params = new ChipGroup.LayoutParams(sizePx, sizePx);
        chip.setLayoutParams(params);

        // Center text
        chip.setGravity(android.view.Gravity.CENTER);

        // Checked State Logic
        chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                chip.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.BLACK));
                chip.setTextColor(android.graphics.Color.WHITE);
            } else {
                chip.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE));
                chip.setTextColor(android.graphics.Color.BLACK);
            }
        });

        chipGroupSizes.addView(chip);
    }

    private float dpToPx(int dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private void addToCart() {
        // Check if product detail is loaded
        if (productDetail == null) {
            Toast.makeText(this, "Vui lòng đợi sản phẩm tải xong", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedChipId = chipGroupSizes.getCheckedChipId();
        String selectedSize = "41"; // Default fall back
        if (selectedChipId != -1) {
            Chip selectedChip = findViewById(selectedChipId);
            selectedSize = selectedChip.getText().toString();
        }

        // Use productDetail.getId() to ensure we have the correct ID
        Long actualProductId = productDetail.getId();
        if (actualProductId == null) {
            actualProductId = productId; // Fallback to intent ID
        }

        AddToCartRequest request = new AddToCartRequest(actualProductId, quantity, selectedSize);

        android.util.Log.d("ProductDetail", "🛒 Adding to cart - ProductId: " + actualProductId + ", Quantity: "
                + quantity + ", Size: " + selectedSize);
        android.util.Log.d("ProductDetail",
                "📝 Token: " + (token != null ? "Present (length: " + token.length() + ")" : "NULL"));
        android.util.Log.d("ProductDetail",
                "📦 Request Body: productId=" + actualProductId + ", quantity=" + quantity + ", size=" + selectedSize);

        // Disable button momentarily
        btnAddToCart.setEnabled(false);
        btnAddToCart.setAlpha(0.7f);

        cartApi.addToCart(token, request).enqueue(new Callback<java.util.Map<String, String>>() {
            @Override
            public void onResponse(Call<java.util.Map<String, String>> call,
                    Response<java.util.Map<String, String>> response) {
                btnAddToCart.setEnabled(true);
                btnAddToCart.setAlpha(1.0f);

                android.util.Log.d("ProductDetail", "📦 Add to cart response: " + response.code());
                android.util.Log.d("ProductDetail", "🔗 Request URL: " + call.request().url());

                if (response.isSuccessful()) {
                    android.util.Log.d("ProductDetail", "✅ Successfully added to cart");
                    if (response.body() != null) {
                        android.util.Log.d("ProductDetail", "Response body: " + response.body());
                    }
                    Toast.makeText(ProductDetailActivity.this, "✅ Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                    // Optional: Navigate to Cart
                    // startActivity(new Intent(ProductDetailActivity.this, CartActivity.class));
                } else {
                    android.util.Log.e("ProductDetail", "❌ Add to cart failed: " + response.code());
                    android.util.Log.e("ProductDetail", "❌ Response message: " + response.message());
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            android.util.Log.e("ProductDetail", "Error body: " + errorBody);
                            Toast.makeText(ProductDetailActivity.this, "Lỗi: " + errorBody, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(ProductDetailActivity.this, "Thêm vào giỏ thất bại: " + response.code(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        android.util.Log.e("ProductDetail", "Error reading error body", e);
                        Toast.makeText(ProductDetailActivity.this, "Thêm vào giỏ thất bại: " + response.code(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<java.util.Map<String, String>> call, Throwable t) {
                btnAddToCart.setEnabled(true);
                btnAddToCart.setAlpha(1.0f);
                android.util.Log.e("ProductDetail", "💥 Add to cart failure", t);
                Toast.makeText(ProductDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createImageIndicators(int count) {
        llImageIndicator.removeAllViews();
        for (int i = 0; i < count; i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(20, 20); // Slightly larger for new design
            params.setMargins(8, 0, 8, 0);
            dot.setLayoutParams(params);
            dot.setBackground(getResources().getDrawable(R.drawable.bg_circle_gray)); // Use existing drawable or new
                                                                                      // one
            llImageIndicator.addView(dot);
        }
    }

    private void updateImageIndicator(int position) {
        int childCount = llImageIndicator.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View dot = llImageIndicator.getChildAt(i);
            if (i == position) {
                // Active color (Black or Dark Gray)
                dot.getBackground().setTint(getResources().getColor(R.color.black));
            } else {
                // Inactive color (Light Gray)
                dot.getBackground().setTint(getResources().getColor(android.R.color.darker_gray));
            }
        }
    }

    private boolean isTogglingFavorite = false;

    /**
     * Toggle favorite status (add/remove)
     */
    private void toggleFavorite() {
        if (productDetail == null || isTogglingFavorite)
            return;

        isTogglingFavorite = true;

        if (isFavorite) {
            // Remove from favorites
            favoriteApi.removeFromFavorites(productDetail.getId(), token)
                    .enqueue(new Callback<java.util.Map<String, Object>>() {
                        @Override
                        public void onResponse(Call<java.util.Map<String, Object>> call,
                                Response<java.util.Map<String, Object>> response) {
                            isTogglingFavorite = false;
                            if (response.isSuccessful()) {
                                isFavorite = false;
                                updateFavoriteUI();
                                Toast.makeText(ProductDetailActivity.this,
                                        "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<java.util.Map<String, Object>> call, Throwable t) {
                            isTogglingFavorite = false;
                            Toast.makeText(ProductDetailActivity.this,
                                    "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Add to favorites
            favoriteApi.addToFavorites(productDetail.getId(), token)
                    .enqueue(new Callback<java.util.Map<String, Object>>() {

                        @Override
                        public void onResponse(Call<java.util.Map<String, Object>> call,
                                Response<java.util.Map<String, Object>> response) {
                            isTogglingFavorite = false;
                            if (response.isSuccessful()) {
                                isFavorite = true;
                                updateFavoriteUI();
                                Toast.makeText(ProductDetailActivity.this,
                                        "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<java.util.Map<String, Object>> call, Throwable t) {
                            isTogglingFavorite = false;
                            Toast.makeText(ProductDetailActivity.this,
                                    "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                        }

                    });
        }
    }

    /**
     * Update favorite icon based on status
     */
    private void updateFavoriteUI() {
        if (isFavorite) {
            ivFavorite.setImageResource(R.drawable.ic_heart_filled);
            ivFavorite.setColorFilter(getResources().getColor(android.R.color.holo_red_dark));
        } else {
            ivFavorite.setImageResource(R.drawable.ic_heart_outline);
            ivFavorite.clearColorFilter();
        }
    }
}
