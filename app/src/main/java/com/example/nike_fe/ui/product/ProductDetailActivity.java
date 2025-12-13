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
import com.example.nike_fe.data.api.ProductApi;
import com.example.nike_fe.data.api.RetrofitClient;
import com.example.nike_fe.data.model.AddToCartRequest;
import com.example.nike_fe.data.model.ProductDetail;
import com.example.nike_fe.ui.cart.CartActivity;

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

    // Quantity Controls
    private ImageView btnMinus, btnPlus;
    private TextView tvQuantityHolder;

    // Size Chips
    private ChipGroup chipGroupSizes;

    private ImageGalleryAdapter imageAdapter;
    private ProductApi productApi;
    private CartApi cartApi;
    private String token;
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

        // Quantity Controls
        btnMinus = findViewById(R.id.btnMinus);
        btnPlus = findViewById(R.id.btnPlus);
        tvQuantityHolder = findViewById(R.id.tvQuantity);

        // Retrofit
        RetrofitClient retrofitClient = RetrofitClient.getInstance(this);
        productApi = retrofitClient.getProductApi();
        cartApi = retrofitClient.getCartApi();
        token = retrofitClient.getToken();
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> onBackPressed());

        ivFavorite.setOnClickListener(v -> {
            Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show();
            ivFavorite.setImageResource(R.drawable.ic_heart_filled); // visual feedback
            ivFavorite.setColorFilter(getResources().getColor(android.R.color.holo_red_dark));
        });

        btnAddToCart.setOnClickListener(v -> {
            if (token == null || token.isEmpty()) {
                Toast.makeText(this, "Please login to add to cart", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, com.example.nike_fe.ui.auth.LoginActivity.class));
                return;
            }
            if (productDetail == null)
                return;
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

        // Dynamically add size chips if they were dynamic, but for now they are static
        // XML
    }

    private void addToCart() {
        int selectedChipId = chipGroupSizes.getCheckedChipId();
        String selectedSize = "41"; // Default fall back
        if (selectedChipId != -1) {
            Chip selectedChip = findViewById(selectedChipId);
            selectedSize = selectedChip.getText().toString();
        }

        AddToCartRequest request = new AddToCartRequest(productId, quantity, selectedSize);

        // Disable button momentarily
        btnAddToCart.setEnabled(false);
        btnAddToCart.setAlpha(0.7f);

        cartApi.addToCart("Bearer " + token, request).enqueue(new Callback<java.util.Map<String, String>>() {
            @Override
            public void onResponse(Call<java.util.Map<String, String>> call,
                    Response<java.util.Map<String, String>> response) {
                btnAddToCart.setEnabled(true);
                btnAddToCart.setAlpha(1.0f);
                if (response.isSuccessful()) {
                    Toast.makeText(ProductDetailActivity.this, "✅ Added to Cart", Toast.LENGTH_SHORT).show();
                    // Optional: Navigate to Cart
                    // startActivity(new Intent(ProductDetailActivity.this, CartActivity.class));
                } else {
                    Toast.makeText(ProductDetailActivity.this, "Failed to add to cart", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<java.util.Map<String, String>> call, Throwable t) {
                btnAddToCart.setEnabled(true);
                btnAddToCart.setAlpha(1.0f);
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
}
