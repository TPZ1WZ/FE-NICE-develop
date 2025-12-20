package com.example.nike_fe.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nike_fe.R;
import com.example.nike_fe.data.api.AdminProductApi;
import com.example.nike_fe.data.api.RetrofitClient;
import com.example.nike_fe.data.model.AdminProduct;
import com.example.nike_fe.data.model.ProductStats;
import com.example.nike_fe.ui.admin.adapter.AdminProductAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminProductsActivity extends AppCompatActivity {
    
    private static final String TAG = "AdminProducts";
    
    private ImageView ivBack;
    private FloatingActionButton fabAddProduct;
    private TextInputEditText etSearch;
    private TextView tvTotalProducts, tvLowStock, tvOutOfStock;
    private RecyclerView rvProducts;
    private FrameLayout layoutLoading;
    private LinearLayout layoutContent, layoutEmpty;
    
    private AdminProductAdapter adapter;
    private AdminProductApi productApi;
    private String token;
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_products);
        
        initViews();
        setupRecyclerView();
        setupClickListeners();
        loadData();
    }
    
    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        fabAddProduct = findViewById(R.id.fabAddProduct);
        etSearch = findViewById(R.id.etSearch);
        tvTotalProducts = findViewById(R.id.tvTotalProducts);
        tvLowStock = findViewById(R.id.tvLowStock);
        tvOutOfStock = findViewById(R.id.tvOutOfStock);
        rvProducts = findViewById(R.id.rvProducts);
        layoutLoading = findViewById(R.id.layoutLoading);
        layoutContent = findViewById(R.id.layoutContent);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        
        RetrofitClient retrofitClient = RetrofitClient.getInstance(this);
        productApi = retrofitClient.getAdminProductApi();
        token = retrofitClient.getToken();
        
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }
    
    private void setupRecyclerView() {
        adapter = new AdminProductAdapter();
        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        rvProducts.setAdapter(adapter);
        
        adapter.setOnProductClickListener(new AdminProductAdapter.OnProductClickListener() {
            @Override
            public void onEditClick(AdminProduct product) {
                try {
                    if (product == null || product.getId() == null) {
                        Toast.makeText(AdminProductsActivity.this, "Lỗi: Không tìm thấy thông tin sản phẩm", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent intent = new Intent(AdminProductsActivity.this, AdminProductFormActivity.class);
                    intent.putExtra("product_id", product.getId());
                    intent.putExtra("is_edit_mode", true);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error opening edit product: " + e.getMessage(), e);
                    Toast.makeText(AdminProductsActivity.this, "Lỗi mở trang chỉnh sửa", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onDeleteClick(AdminProduct product) {
                try {
                    if (product == null) {
                        Toast.makeText(AdminProductsActivity.this, "Lỗi: Không tìm thấy thông tin sản phẩm", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    showDeleteConfirmDialog(product);
                } catch (Exception e) {
                    Log.e(TAG, "Error opening delete dialog: " + e.getMessage(), e);
                    Toast.makeText(AdminProductsActivity.this, "Lỗi hiển thị xác nhận xóa", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> onBackPressed());
        
        fabAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminProductFormActivity.class);
            intent.putExtra("is_edit_mode", false);
            startActivity(intent);
        });
        
        // Search with debounce
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                searchRunnable = () -> loadProducts(s.toString());
                searchHandler.postDelayed(searchRunnable, 300);
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private void loadData() {
        loadStats();
        loadProducts("");
    }
    
    private void loadStats() {
        Log.d(TAG, "Loading stats...");
        productApi.getStats("Bearer " + token).enqueue(new Callback<ProductStats>() {
            @Override
            public void onResponse(Call<ProductStats> call, Response<ProductStats> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProductStats stats = response.body();
                    Log.d(TAG, "Stats loaded: total=" + stats.getTotal() + ", lowStock=" + stats.getLowStock() + ", outOfStock=" + stats.getOutOfStock());
                    tvTotalProducts.setText(String.valueOf(stats.getTotal()));
                    tvLowStock.setText(String.valueOf(stats.getLowStock()));
                    tvOutOfStock.setText(String.valueOf(stats.getOutOfStock()));
                } else {
                    Log.e(TAG, "Failed to load stats: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<ProductStats> call, Throwable t) {
                Log.e(TAG, "Error loading stats", t);
            }
        });
    }
    
    private void loadProducts(String query) {
        Log.d(TAG, "Loading products with query: " + query);
        layoutLoading.setVisibility(View.VISIBLE);
        layoutContent.setVisibility(View.GONE);
        
        productApi.getProducts("Bearer " + token, query).enqueue(new Callback<List<AdminProduct>>() {
            @Override
            public void onResponse(Call<List<AdminProduct>> call, Response<List<AdminProduct>> response) {
                layoutLoading.setVisibility(View.GONE);
                layoutContent.setVisibility(View.VISIBLE);
                
                if (response.isSuccessful() && response.body() != null) {
                    List<AdminProduct> products = response.body();
                    Log.d(TAG, "Products loaded: " + products.size());
                    
                    if (products.isEmpty()) {
                        layoutEmpty.setVisibility(View.VISIBLE);
                        rvProducts.setVisibility(View.GONE);
                    } else {
                        layoutEmpty.setVisibility(View.GONE);
                        rvProducts.setVisibility(View.VISIBLE);
                        adapter.setProducts(products);
                    }
                } else {
                    Log.e(TAG, "Failed to load products: " + response.code());
                    Toast.makeText(AdminProductsActivity.this, 
                            "Lỗi tải sản phẩm: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<List<AdminProduct>> call, Throwable t) {
                layoutLoading.setVisibility(View.GONE);
                layoutContent.setVisibility(View.VISIBLE);
                Log.e(TAG, "Error loading products", t);
                Toast.makeText(AdminProductsActivity.this, 
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showDeleteConfirmDialog(AdminProduct product) {
        if (product == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String productName = product.getName() != null ? product.getName() : "N/A";
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa sản phẩm \"" + productName + "\"?\n\nHành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> deleteProduct(product))
                .setNegativeButton("Hủy", null)
                .show();
    }
    
    private void deleteProduct(AdminProduct product) {
        if (product == null || product.getId() == null) {
            Toast.makeText(this, "Lỗi: Không thể xóa sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Log.d(TAG, "Deleting product: " + product.getId());
        layoutLoading.setVisibility(View.VISIBLE);
        
        productApi.deleteProduct("Bearer " + token, product.getId()).enqueue(
                new Callback<com.example.nike_fe.data.model.DeleteProductResponse>() {
            @Override
            public void onResponse(Call<com.example.nike_fe.data.model.DeleteProductResponse> call, 
                                 Response<com.example.nike_fe.data.model.DeleteProductResponse> response) {
                layoutLoading.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    com.example.nike_fe.data.model.DeleteProductResponse deleteResponse = response.body();
                    
                    if (deleteResponse.isSuccess()) {
                        // Tạo thông báo chi tiết
                        StringBuilder message = new StringBuilder("Đã xóa sản phẩm thành công");
                        
                        if (deleteResponse.getDetails() != null) {
                            com.example.nike_fe.data.model.DeleteProductResponse.Details details = 
                                    deleteResponse.getDetails();
                            
                            if (details.getCartItemsRemoved() > 0) {
                                message.append("\n• Đã xóa khỏi ")
                                       .append(details.getCartItemsRemoved())
                                       .append(" giỏ hàng");
                            }
                            
                            if (details.getFavoritesRemoved() > 0) {
                                message.append("\n• Đã xóa khỏi ")
                                       .append(details.getFavoritesRemoved())
                                       .append(" danh sách yêu thích");
                            }
                            
                            if (details.isSoftDelete()) {
                                message.append("\n• Sản phẩm được ẩn do có trong đơn hàng");
                            }
                        }
                        
                        Toast.makeText(AdminProductsActivity.this, 
                                message.toString(), Toast.LENGTH_LONG).show();
                        loadData(); // Reload all data
                    } else {
                        Toast.makeText(AdminProductsActivity.this, 
                                deleteResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AdminProductsActivity.this, 
                            "Xóa thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<com.example.nike_fe.data.model.DeleteProductResponse> call, Throwable t) {
                layoutLoading.setVisibility(View.GONE);
                Log.e(TAG, "Error deleting product", t);
                Toast.makeText(AdminProductsActivity.this, 
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadData(); // Reload when coming back from form
    }
}
