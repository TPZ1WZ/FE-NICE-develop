package com.example.nike_fe.ui.admin;

import android.content.Intent;
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
import com.example.nike_fe.data.api.AdminApi;
import com.example.nike_fe.data.api.AdminProductApi;
import com.example.nike_fe.data.api.RetrofitClient;
import com.example.nike_fe.data.model.AdminProduct;
import com.example.nike_fe.data.model.Category;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminProductFormActivity extends AppCompatActivity {
    
    private static final String TAG = "AdminProductForm";
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PERMISSION_REQUEST_CODE = 100;
    
    private ImageView ivBack, ivImagePreview;
    private TextView tvTitle;
    private TextInputEditText etProductName, etPrice, etStock, etImageUrl, etDescription;
    private AutoCompleteTextView actvCategory, actvStatus;
    private com.google.android.material.chip.ChipGroup chipGroupSizes;
    private Button btnCancel, btnSave, btnSelectImage;
    private FrameLayout layoutLoading;
    private LinearLayout layoutContent;
    
    private AdminProductApi productApi;
    private AdminApi adminApi;
    private String token;
    private boolean isEditMode = false;
    private Long productId;
    private AdminProduct currentProduct;
    private android.net.Uri selectedImageUri;
    private String uploadedImageUrl;
    private List<Category> categories = new ArrayList<>();
    private ArrayAdapter<String> categoryAdapter;

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
        chipGroupSizes = findViewById(R.id.chipGroupSizes);
        actvCategory = findViewById(R.id.actvCategory);
        etPrice = findViewById(R.id.etPrice);
        etStock = findViewById(R.id.etStock);
        actvStatus = findViewById(R.id.actvStatus);
        etImageUrl = findViewById(R.id.etImageUrl);
        etDescription = findViewById(R.id.etDescription);
        btnCancel = findViewById(R.id.btnCancel);
        btnSave = findViewById(R.id.btnSave);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        ivImagePreview = findViewById(R.id.ivImagePreview);
        layoutLoading = findViewById(R.id.layoutLoading);
        layoutContent = findViewById(R.id.layoutContent);
        
        RetrofitClient retrofitClient = RetrofitClient.getInstance(this);
        productApi = retrofitClient.getAdminProductApi();
        adminApi = retrofitClient.getAdminApi();
        String rawToken = retrofitClient.getToken();
        token = (rawToken != null && !rawToken.startsWith("Bearer ")) ? "Bearer " + rawToken : rawToken;
        
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        Log.d(TAG, "Token: " + (token != null ? "Present" : "NULL"));
    }
    
    private void setupDropdowns() {
        // Category dropdown - Load từ API
        categoryAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        actvCategory.setAdapter(categoryAdapter);
        
        // Load categories từ server
        loadCategories();
        
        // Status dropdown
        String[] statuses = {"Hiển thị", "Đã ẩn"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_dropdown_item_1line, statuses);
        actvStatus.setAdapter(statusAdapter);
        
        // Setup size chips
        setupSizeChips();
    }
    
    private void setupSizeChips() {
        String[] sizes = {"35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45"};
        
        for (String size : sizes) {
            com.google.android.material.chip.Chip chip = new com.google.android.material.chip.Chip(this);
            chip.setText(size);
            chip.setCheckable(true);
            
            // Màu khi chưa chọn
            chip.setChipBackgroundColorResource(android.R.color.transparent);
            chip.setChipStrokeColorResource(android.R.color.darker_gray);
            chip.setChipStrokeWidth(2f);
            chip.setTextColor(getResources().getColor(android.R.color.white));
            
            // Sự kiện khi click để thay đổi màu
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    // Khi được chọn - màu nền đen, border trắng
                    chip.setChipBackgroundColorResource(android.R.color.black);
                    chip.setChipStrokeColorResource(android.R.color.white);
                    chip.setTextColor(getResources().getColor(android.R.color.white));
                } else {
                    // Khi bỏ chọn - trong suốt, border xám
                    chip.setChipBackgroundColorResource(android.R.color.transparent);
                    chip.setChipStrokeColorResource(android.R.color.darker_gray);
                    chip.setTextColor(getResources().getColor(android.R.color.white));
                }
            });
            
            chipGroupSizes.addView(chip);
        }
    }
    
    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> onBackPressed());
        btnCancel.setOnClickListener(v -> onBackPressed());
        btnSave.setOnClickListener(v -> saveProduct());
        btnSelectImage.setOnClickListener(v -> openImagePicker());
    }
    
    private void loadCategories() {
        Log.d(TAG, "Loading categories from API...");
        
        adminApi.getAllCategories(token).enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categories = response.body();
                    
                    // Cập nhật adapter với danh sách tên categories
                    List<String> categoryNames = new ArrayList<>();
                    for (Category category : categories) {
                        categoryNames.add(category.getName());
                    }
                    
                    categoryAdapter.clear();
                    categoryAdapter.addAll(categoryNames);
                    categoryAdapter.notifyDataSetChanged();
                    
                    Log.d(TAG, "Loaded " + categories.size() + " categories");
                } else {
                    Log.e(TAG, "Failed to load categories: " + response.code());
                    Toast.makeText(AdminProductFormActivity.this, 
                            "Không thể tải danh mục", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Log.e(TAG, "Error loading categories", t);
                Toast.makeText(AdminProductFormActivity.this, 
                        "Lỗi tải danh mục: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void openImagePicker() {
        // Kiểm tra permission
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            if (checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_REQUEST_CODE);
                return;
            }
        } else {
            // Android 12 và thấp hơn
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                return;
            }
        }
        
        // Nếu đã có permission, mở gallery
        openGallery();
    }
    
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Cần quyền truy cập ảnh để chọn hình ảnh", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            
            try {
                // Hiển thị preview với Glide
                if (ivImagePreview != null) {
                    com.bumptech.glide.Glide.with(this)
                        .load(selectedImageUri)
                        .centerCrop()
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_report_image)
                        .into(ivImagePreview);
                    ivImagePreview.setVisibility(View.VISIBLE);
                }
                
                // Convert ảnh sang Base64
                uploadedImageUrl = convertImageToBase64(selectedImageUri);
                
                if (uploadedImageUrl != null) {
                    etImageUrl.setText("Đã chọn ảnh");
                    Toast.makeText(this, "Đã chọn ảnh", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Lỗi chuyển đổi ảnh", Toast.LENGTH_SHORT).show();
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error loading image", e);
                Toast.makeText(this, "Lỗi khi tải ảnh", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private String convertImageToBase64(android.net.Uri imageUri) {
        try {
            java.io.InputStream inputStream = getContentResolver().openInputStream(imageUri);
            android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(inputStream);
            
            // Resize ảnh để giảm kích thước (max 800px)
            int maxSize = 800;
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            
            if (width > maxSize || height > maxSize) {
                float scale = Math.min(((float) maxSize / width), ((float) maxSize / height));
                int newWidth = Math.round(scale * width);
                int newHeight = Math.round(scale * height);
                bitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
            }
            
            // Convert sang Base64
            java.io.ByteArrayOutputStream byteArrayOutputStream = new java.io.ByteArrayOutputStream();
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String base64String = android.util.Base64.encodeToString(byteArray, android.util.Base64.NO_WRAP);
            
            // Thêm data URI prefix
            return "data:image/jpeg;base64," + base64String;
            
        } catch (Exception e) {
            Log.e(TAG, "Error converting image to base64", e);
            return null;
        }
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
        actvCategory.setText(product.getCategory(), false);
        
        // Set selected sizes
        if (product.getSizes() != null) {
            for (int i = 0; i < chipGroupSizes.getChildCount(); i++) {
                com.google.android.material.chip.Chip chip = (com.google.android.material.chip.Chip) chipGroupSizes.getChildAt(i);
                if (product.getSizes().contains(chip.getText().toString())) {
                    chip.setChecked(true);
                }
            }
        }
        etPrice.setText(String.valueOf((int) product.getPrice()));
        etStock.setText(String.valueOf(product.getStock()));
        actvStatus.setText(product.isActive() ? "Hiển thị" : "Đã ẩn", false);
        etImageUrl.setText(product.getImage());
        etDescription.setText(product.getDescription());
        
        // Load image preview if exists
        if (product.getImage() != null && !product.getImage().isEmpty()) {
            try {
                ivImagePreview.setVisibility(View.VISIBLE);
                // Use Glide or Picasso to load image from URL
                // For now, just show the placeholder
            } catch (Exception e) {
                Log.e(TAG, "Error loading product image", e);
            }
        }
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
    
    private java.util.List<String> getSelectedSizes() {
        java.util.List<String> selectedSizes = new java.util.ArrayList<>();
        for (int i = 0; i < chipGroupSizes.getChildCount(); i++) {
            com.google.android.material.chip.Chip chip = (com.google.android.material.chip.Chip) chipGroupSizes.getChildAt(i);
            if (chip.isChecked()) {
                selectedSizes.add(chip.getText().toString());
            }
        }
        return selectedSizes;
    }
    
    private boolean validateForm() {
        String name = etProductName.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String stockStr = etStock.getText().toString().trim();
        String imageUrl = etImageUrl.getText().toString().trim();
        
        if (name.isEmpty()) {
            etProductName.setError("Vui lòng nhập tên sản phẩm");
            etProductName.requestFocus();
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
        
        // Image URL không bắt buộc
        
        return true;
    }
    
    private AdminProduct buildProductFromForm() {
        AdminProduct product = new AdminProduct();
        
        if (isEditMode && currentProduct != null) {
            product.setId(currentProduct.getId());
            product.setCreatedAt(currentProduct.getCreatedAt());
        }
        
        product.setName(etProductName.getText().toString().trim());
        
        // Chuyển category name sang category name (backend sẽ xử lý)
        String selectedCategoryName = actvCategory.getText().toString();
        product.setCategory(selectedCategoryName);
        
        // Tìm category ID từ tên category
        Long categoryId = null;
        for (Category cat : categories) {
            if (cat.getName().equals(selectedCategoryName)) {
                categoryId = cat.getId();
                break;
            }
        }
        product.setCategoryId(categoryId);
        
        product.setSizes(getSelectedSizes());
        product.setPrice(Double.parseDouble(etPrice.getText().toString().trim()));
        
        product.setSalePrice(null); // Không còn field giá khuyến mãi
        
        product.setStock(Integer.parseInt(etStock.getText().toString().trim()));
        product.setStatus(actvStatus.getText().toString().equals("Hiển thị") ? "active" : "inactive");
        
        // Image URL - tạo list images
        String imageUrl = etImageUrl.getText().toString().trim();
        if (imageUrl.isEmpty()) {
            imageUrl = "https://via.placeholder.com/300x300?text=No+Image";
        }
        
        // Nếu có selectedImageUri (ảnh đã upload), sử dụng nó
        if (uploadedImageUrl != null && !uploadedImageUrl.isEmpty()) {
            imageUrl = uploadedImageUrl;
        }
        
        // Tạo images list cho backend
        java.util.List<String> imagesList = new java.util.ArrayList<>();
        imagesList.add(imageUrl);
        product.setImages(imagesList);
        product.setImage(imageUrl); // Giữ để backward compatible
        
        product.setDescription(etDescription.getText().toString().trim());
        
        return product;
    }
    
    private void createProduct(AdminProduct product) {
        Log.d(TAG, "Creating new product: " + product.getName());
        Log.d(TAG, "Product details: " + 
            "Name=" + product.getName() + 
            ", Category=" + product.getCategory() + 
            ", Price=" + product.getPrice() + 
            ", Stock=" + product.getStock() + 
            ", Sizes=" + product.getSizes() + 
            ", Status=" + product.getStatus());
        
        productApi.createProduct(token, product).enqueue(new Callback<AdminProduct>() {
            @Override
            public void onResponse(Call<AdminProduct> call, Response<AdminProduct> response) {
                layoutLoading.setVisibility(View.GONE);
                layoutContent.setVisibility(View.VISIBLE);
                
                if (response.isSuccessful()) {
                    Toast.makeText(AdminProductFormActivity.this, 
                            "Tạo sản phẩm mới thành công", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Log.e(TAG, "Create failed with code: " + response.code());
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Error body: " + errorBody);
                        Toast.makeText(AdminProductFormActivity.this, 
                                "Tạo thất bại: " + response.code() + " - " + errorBody, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(AdminProductFormActivity.this, 
                                "Tạo thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
            
            @Override
            public void onFailure(Call<AdminProduct> call, Throwable t) {
                layoutLoading.setVisibility(View.GONE);
                layoutContent.setVisibility(View.VISIBLE);
                Log.e(TAG, "Error creating product", t);
                Toast.makeText(AdminProductFormActivity.this, 
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
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
