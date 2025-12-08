package com.example.nike_fe.ui.product;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nike_fe.R;
import com.example.nike_fe.adapter.ProductAdapter;
import com.example.nike_fe.data.api.ProductApi;
import com.example.nike_fe.data.api.RetrofitClient;
import com.example.nike_fe.data.model.Product;
import com.example.nike_fe.ui.cart.CartActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductListActivity extends AppCompatActivity {
    
    private RecyclerView rvProducts;
    private ProgressBar progressBar;
    private ProductAdapter adapter;
    private ProductApi productApi;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);
        
        initViews();
        setupToolbar();
        setupRecyclerView();
        loadProducts();
    }
    
    private void initViews() {
        rvProducts = findViewById(R.id.rvProducts);
        progressBar = findViewById(R.id.progressBar);
        productApi = RetrofitClient.getInstance(this).getProductApi();
    }
    
    private void setupToolbar() {
        ImageView ivBack = findViewById(R.id.ivBack);
        ImageView ivCart = findViewById(R.id.ivCart);
        ImageView ivMenu = findViewById(R.id.ivMenu);
        
        ivBack.setOnClickListener(v -> onBackPressed());
        
        ivCart.setOnClickListener(v -> {
            Intent intent = new Intent(ProductListActivity.this, CartActivity.class);
            startActivity(intent);
        });
        
        ivMenu.setOnClickListener(v -> {
            // TODO: Open menu drawer
            Toast.makeText(this, "Menu", Toast.LENGTH_SHORT).show();
        });
    }
    
    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        rvProducts.setLayoutManager(layoutManager);
        
        adapter = new ProductAdapter(this, product -> {
            // Navigate to ProductDetailActivity
            Intent intent = new Intent(ProductListActivity.this, ProductDetailActivity.class);
            intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, product.getId());
            startActivity(intent);
        });
        
        rvProducts.setAdapter(adapter);
    }
    
    private void loadProducts() {
        progressBar.setVisibility(View.VISIBLE);
        rvProducts.setVisibility(View.GONE);
        
        productApi.getProducts(null, null, null).enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                progressBar.setVisibility(View.GONE);
                rvProducts.setVisibility(View.VISIBLE);
                
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> products = response.body();
                    adapter.setProducts(products);
                    
                    if (products.isEmpty()) {
                        Toast.makeText(ProductListActivity.this, 
                                "Không có sản phẩm nào", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ProductListActivity.this, 
                            "Lỗi tải danh sách sản phẩm: " + response.code(), 
                            Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                rvProducts.setVisibility(View.VISIBLE);
                Toast.makeText(ProductListActivity.this, 
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
