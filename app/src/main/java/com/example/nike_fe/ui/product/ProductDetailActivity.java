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
    
    private ViewPager2 vpProductImages;
    private LinearLayout llImageIndicator;
    private TextView tvProductName, tvProductSubtitle, tvPrice, tvStock, tvDescription;
    private Button btnAddToCart;
    private ImageView ivBack, ivFavorite, ivCart;
    private ProgressBar progressBar;
    private ChipGroup chipGroupSizes;
    
    private ImageGalleryAdapter imageAdapter;
    private ProductApi productApi;
    private CartApi cartApi;
    private String token;
    private Long productId;
    private ProductDetail productDetail;
    
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
        setupToolbar();
        setupImageGallery();
        loadProductDetail();
    }
    
    private void initViews() {
        vpProductImages = findViewById(R.id.vpProductImages);
        llImageIndicator = findViewById(R.id.llImageIndicator);
        tvProductName = findViewById(R.id.tvProductName);
        tvProductSubtitle = findViewById(R.id.tvProductSubtitle);
        tvPrice = findViewById(R.id.tvPrice);
        tvStock = findViewById(R.id.tvStock);
        tvDescription = findViewById(R.id.tvDescription);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        chipGroupSizes = findViewById(R.id.chipGroupSizes);
        ivBack = findViewById(R.id.ivBack);
        ivFavorite = findViewById(R.id.ivFavorite);
        ivCart = findViewById(R.id.ivCart);
        progressBar = findViewById(R.id.progressBar);
        
        RetrofitClient retrofitClient = RetrofitClient.getInstance(this);
        productApi = retrofitClient.getProductApi();
        cartApi = retrofitClient.getCartApi();
        token = retrofitClient.getToken();
    }
    
    private void setupToolbar() {
        ivBack.setOnClickListener(v -> onBackPressed());
        
        ivFavorite.setOnClickListener(v -> {
            // TODO: Toggle favorite
            Toast.makeText(this, "Yêu thích", Toast.LENGTH_SHORT).show();
        });
        
        ivCart.setOnClickListener(v -> {
            Intent intent = new Intent(ProductDetailActivity.this, CartActivity.class);
            startActivity(intent);
        });
        
        btnAddToCart.setOnClickListener(v -> {
            // Debug log
            android.util.Log.d("ProductDetail", "Add to cart clicked. Token: " + (token != null ? "exists" : "null"));
            
            if (token == null || token.isEmpty()) {
                Toast.makeText(this, "Vui lòng đăng nhập để thêm vào giỏ hàng", Toast.LENGTH_LONG).show();
                // Redirect to login
                Intent loginIntent = new Intent(this, com.example.nike_fe.ui.auth.LoginActivity.class);
                startActivity(loginIntent);
                return;
            }
            if (productDetail == null) {
                Toast.makeText(this, "Đang tải thông tin sản phẩm", Toast.LENGTH_SHORT).show();
                return;
            }
            addToCart();
        });
    }
    
    private void setupImageGallery() {
        imageAdapter = new ImageGalleryAdapter(this);
        vpProductImages.setAdapter(imageAdapter);
        
        // Setup image indicator dots
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
                    Toast.makeText(ProductDetailActivity.this, 
                            "Lỗi tải chi tiết sản phẩm: " + response.code(), 
                            Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ProductDetail> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ProductDetailActivity.this, 
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void displayProductDetail() {
        tvProductName.setText(productDetail.getName());
        
        if (productDetail.getSubTitle() != null) {
            tvProductSubtitle.setText(productDetail.getSubTitle());
        } else {
            tvProductSubtitle.setVisibility(View.GONE);
        }
        
        // Format price
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        String formattedPrice = formatter.format(productDetail.getPrice()) + "₫";
        tvPrice.setText(formattedPrice);
        
        // Stock status
        if (productDetail.getStock() != null) {
            if (productDetail.getStock() > 0) {
                tvStock.setText("Còn hàng: " + productDetail.getStock());
                tvStock.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                tvStock.setText("Hết hàng");
                tvStock.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                btnAddToCart.setEnabled(false);
            }
        }
        
        // Description
        if (productDetail.getDescription() != null && !productDetail.getDescription().isEmpty()) {
            tvDescription.setText(productDetail.getDescription());
        } else {
            tvDescription.setText("Chưa có mô tả");
        }
        
        // Load images
        if (productDetail.getImages() != null && !productDetail.getImages().isEmpty()) {
            imageAdapter.setImages(productDetail.getImages());
            createImageIndicators(productDetail.getImages().size());
            updateImageIndicator(0);
        } else {
            // No images, show placeholder
            List<String> placeholderImages = new ArrayList<>();
            placeholderImages.add("");
            imageAdapter.setImages(placeholderImages);
        }
    }
    
    private void addToCart() {
        // Get selected size
        int selectedChipId = chipGroupSizes.getCheckedChipId();
        if (selectedChipId == -1) {
            Toast.makeText(this, "Vui lòng chọn size", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Chip selectedChip = findViewById(selectedChipId);
        String selectedSize = selectedChip.getText().toString();
        
        AddToCartRequest request = new AddToCartRequest(productId, 1, selectedSize);
        
        btnAddToCart.setEnabled(false);
        btnAddToCart.setText("Đang thêm...");
        
        cartApi.addToCart("Bearer " + token, request).enqueue(new Callback<java.util.Map<String, String>>() {
            @Override
            public void onResponse(Call<java.util.Map<String, String>> call, Response<java.util.Map<String, String>> response) {
                btnAddToCart.setEnabled(true);
                btnAddToCart.setText("Thêm vào giỏ hàng");
                
                if (response.isSuccessful()) {
                    Toast.makeText(ProductDetailActivity.this, 
                            "✅ Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProductDetailActivity.this, 
                            "Lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<java.util.Map<String, String>> call, Throwable t) {
                btnAddToCart.setEnabled(true);
                btnAddToCart.setText("Thêm vào giỏ hàng");
                Toast.makeText(ProductDetailActivity.this, 
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void createImageIndicators(int count) {
        llImageIndicator.removeAllViews();
        
        for (int i = 0; i < count; i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(16, 16);
            params.setMargins(8, 0, 8, 0);
            dot.setLayoutParams(params);
            dot.setBackground(getResources().getDrawable(R.drawable.indicator_dot_inactive));
            llImageIndicator.addView(dot);
        }
    }
    
    private void updateImageIndicator(int position) {
        int childCount = llImageIndicator.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View dot = llImageIndicator.getChildAt(i);
            if (i == position) {
                dot.setBackground(getResources().getDrawable(R.drawable.indicator_dot_active));
            } else {
                dot.setBackground(getResources().getDrawable(R.drawable.indicator_dot_inactive));
            }
        }
    }
}
