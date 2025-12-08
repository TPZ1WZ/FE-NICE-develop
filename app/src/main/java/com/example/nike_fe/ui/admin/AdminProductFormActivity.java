package com.example.nike_fe.ui.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nike_fe.R;
import com.example.nike_fe.data.api.AdminProductApi;
import com.example.nike_fe.data.api.RetrofitClient;
import com.example.nike_fe.data.model.AdminProduct;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminProductFormActivity extends AppCompatActivity {
    
    private static final String TAG = "AdminProductForm";
    
    private ImageView ivBack;
    private TextView tvTitle;
    private TextInputEditText etProductName, etProductSku, etPrice, etStock, etImageUrl, etDescription;
    private AutoCompleteTextView actvCategory, actvStatus;
    private Button btnCancel, btnSave;
    private FrameLayout layoutLoading;
    private LinearLayout layoutContent;
    
    private AdminProductApi productApi;
    private String token;
    private boolean isEditMode = false;
    private Long productId;
    private AdminProduct currentProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_product_form);
        
        initViews();
        setupDropdowns();
        setupClickListeners();
        
        // Check if edit mode
        isEditMode = getIntent().getBooleanExtra("is_edit_mode", false);
        productId = getIntent().getLongExtra("product_id", -1);
        
        if (isEditMode && productId != -1) {
            tvTitle.setText("Chỉnh sửa sản phẩm");
            btnSave.setText("Cập nhật");
            loadProductData();
        } else {
            tvTitle.setText("Thêm sản phẩm mới");
            btnSave.setText("Tạo mới");
        }
    }
    
    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        tvTitle = findViewById(R.id.tvTitle);
        etProductName = findViewById(R.id.etProductName);
        etProductSku = findViewById(R.id.etProductSku);
        actvCategory = findViewById(R.id.actvCategory);
        etPrice = findViewById(R.id.etPrice);
        etStock = findViewById(R.id.etStock);
        actvStatus = findViewById(R.id.actvStatus);
        etImageUrl = findViewById(R.id.etImageUrl);
        etDescription = findViewById(R.id.etDescription);
        btnCancel = findViewById(R.id.btnCancel);
        btnSave = findViewById(R.id.btnSave);
        layoutLoading = findViewById(R.id.layoutLoading);
        layoutContent = findViewById(R.id.layoutContent);
        
        RetrofitClient retrofitClient = RetrofitClient.getInstance(this);
        productApi = retrofitClient.getAdminProductApi();
        token = retrofitClient.getToken();
        
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }
    
    private void setupDropdowns() {
        // Category dropdown
        String[] categories = {"Lifestyle", "Running", "Basketball", "Football", "Training", "Jordan"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_dropdown_item_1line, categories);
        actvCategory.setAdapter(categoryAdapter);
        
        // Status dropdown
        String[] statuses = {"Hiển thị", "Đã ẩn"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_dropdown_item_1line, statuses);
        actvStatus.setAdapter(statusAdapter);
    }
    
    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> onBackPressed());
        btnCancel.setOnClickListener(v -> onBackPressed());
        btnSave.setOnClickListener(v -> saveProduct());
    }
    
    private void loadProductData() {
        Log.d(TAG, "Loading product data for ID: " + productId);
        layoutLoading.setVisibility(View.VISIBLE);
        layoutContent.setVisibility(View.GONE);
        
        productApi.getProductById("Bearer " + token, productId).enqueue(new Callback<AdminProduct>() {
            @Override
            public void onResponse(Call<AdminProduct> call, Response<AdminProduct> response) {
                layoutLoading.setVisibility(View.GONE);
                layoutContent.setVisibility(View.VISIBLE);
                
                if (response.isSuccessful() && response.body() != null) {
                    currentProduct = response.body();
                    populateForm(currentProduct);
                } else {
                    Toast.makeText(AdminProductFormActivity.this, 
                            "Không thể tải dữ liệu sản phẩm", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            
            @Override
            public void onFailure(Call<AdminProduct> call, Throwable t) {
                layoutLoading.setVisibility(View.GONE);
                layoutContent.setVisibility(View.VISIBLE);
                Log.e(TAG, "Error loading product", t);
                Toast.makeText(AdminProductFormActivity.this, 
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
    
    private void populateForm(AdminProduct product) {
        etProductName.setText(product.getName());
        etProductSku.setText(product.getSku());
        actvCategory.setText(product.getCategory(), false);
        etPrice.setText(String.valueOf((int) product.getPrice()));
        etStock.setText(String.valueOf(product.getStock()));
        actvStatus.setText(product.isActive() ? "Hiển thị" : "Đã ẩn", false);
        etImageUrl.setText(product.getImage());
        etDescription.setText(product.getDescription());
    }
    
    private void saveProduct() {
        // Validate
        if (!validateForm()) {
            return;
        }
        
        // Build product object
        AdminProduct product = buildProductFromForm();
        
        layoutLoading.setVisibility(View.VISIBLE);
        layoutContent.setVisibility(View.GONE);
        
        if (isEditMode) {
            updateProduct(product);
        } else {
            createProduct(product);
        }
    }
    
    private boolean validateForm() {
        String name = etProductName.getText().toString().trim();
        String sku = etProductSku.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String stockStr = etStock.getText().toString().trim();
        String imageUrl = etImageUrl.getText().toString().trim();
        
        if (name.isEmpty()) {
            etProductName.setError("Vui lòng nhập tên sản phẩm");
            etProductName.requestFocus();
            return false;
        }
        
        if (sku.isEmpty()) {
            etProductSku.setError("Vui lòng nhập mã SKU");
            etProductSku.requestFocus();
            return false;
        }
        
        if (priceStr.isEmpty()) {
            etPrice.setError("Vui lòng nhập giá");
            etPrice.requestFocus();
            return false;
        }
        
        if (stockStr.isEmpty()) {
            etStock.setError("Vui lòng nhập số lượng kho");
            etStock.requestFocus();
            return false;
        }
        
        if (imageUrl.isEmpty()) {
            etImageUrl.setError("Vui lòng nhập URL hình ảnh");
            etImageUrl.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private AdminProduct buildProductFromForm() {
        AdminProduct product = new AdminProduct();
        
        if (isEditMode && currentProduct != null) {
            product.setId(currentProduct.getId());
            product.setCreatedAt(currentProduct.getCreatedAt());
        }
        
        product.setName(etProductName.getText().toString().trim());
        product.setSku(etProductSku.getText().toString().trim());
        product.setCategory(actvCategory.getText().toString());
        product.setPrice(Double.parseDouble(etPrice.getText().toString().trim()));
        
        product.setSalePrice(null); // Không còn field giá khuyến mãi
        
        product.setStock(Integer.parseInt(etStock.getText().toString().trim()));
        product.setStatus(actvStatus.getText().toString().equals("Hiển thị") ? "active" : "inactive");
        product.setImage(etImageUrl.getText().toString().trim());
        product.setDescription(etDescription.getText().toString().trim());
        
        return product;
    }
    
    private void createProduct(AdminProduct product) {
        Log.d(TAG, "Creating new product: " + product.getName());
        
        productApi.createProduct("Bearer " + token, product).enqueue(new Callback<AdminProduct>() {
            @Override
            public void onResponse(Call<AdminProduct> call, Response<AdminProduct> response) {
                layoutLoading.setVisibility(View.GONE);
                layoutContent.setVisibility(View.VISIBLE);
                
                if (response.isSuccessful()) {
                    Toast.makeText(AdminProductFormActivity.this, 
                            "Tạo sản phẩm mới thành công", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AdminProductFormActivity.this, 
                            "Tạo thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<AdminProduct> call, Throwable t) {
                layoutLoading.setVisibility(View.GONE);
                layoutContent.setVisibility(View.VISIBLE);
                Log.e(TAG, "Error creating product", t);
                Toast.makeText(AdminProductFormActivity.this, 
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void updateProduct(AdminProduct product) {
        Log.d(TAG, "Updating product: " + productId);
        
        productApi.updateProduct("Bearer " + token, productId, product).enqueue(new Callback<AdminProduct>() {
            @Override
            public void onResponse(Call<AdminProduct> call, Response<AdminProduct> response) {
                layoutLoading.setVisibility(View.GONE);
                layoutContent.setVisibility(View.VISIBLE);
                
                if (response.isSuccessful()) {
                    Toast.makeText(AdminProductFormActivity.this, 
                            "Cập nhật sản phẩm thành công", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AdminProductFormActivity.this, 
                            "Cập nhật thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<AdminProduct> call, Throwable t) {
                layoutLoading.setVisibility(View.GONE);
                layoutContent.setVisibility(View.VISIBLE);
                Log.e(TAG, "Error updating product", t);
                Toast.makeText(AdminProductFormActivity.this, 
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
