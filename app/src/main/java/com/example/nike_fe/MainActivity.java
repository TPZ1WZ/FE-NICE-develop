package com.example.nike_fe;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nike_fe.data.api.RetrofitClient;
import com.example.nike_fe.ui.auth.LoginActivity;
import com.example.nike_fe.ui.cart.CartActivity;
import com.example.nike_fe.ui.product.ProductListActivity;
import com.example.nike_fe.ui.profile.ProfileActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * MainActivity - Trang chủ (HomePage)
 * Hiển thị Hero banner, Featured products, Bottom navigation
 */
public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageView ivCart, ivProfile, ivMenu;
    private Button btnShopShoes, btnViewAll;
    private RecyclerView rvFeaturedProducts;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo views
        initViews();
        setupListeners();
        setupRecyclerView();
        setupBottomNavigation();
    }

    /**
     * Khởi tạo các views
     */
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        ivCart = findViewById(R.id.ivCart);
        ivProfile = findViewById(R.id.ivProfile);
        ivMenu = findViewById(R.id.ivMenu);
        btnShopShoes = findViewById(R.id.btnShopShoes);
        btnViewAll = findViewById(R.id.btnViewAll);
        rvFeaturedProducts = findViewById(R.id.rvFeaturedProducts);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }
    /**
     * Setup event listeners
     */
    private void setupListeners() {
        // Cart button
        ivCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CartActivity.class);
                startActivity(intent);
            }
        });

        // Profile button
        ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        // Menu button
        ivMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Menu (đang phát triển)", Toast.LENGTH_SHORT).show();
            }
        });

        // Shop Shoes button
        btnShopShoes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProductListActivity.class);
                startActivity(intent);
            }
        });

        // View All button
        btnViewAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProductListActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Setup RecyclerView cho Featured Products
     */
    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        rvFeaturedProducts.setLayoutManager(layoutManager);
        
        // TODO: Thêm adapter khi có API products
        // FeaturedProductsAdapter adapter = new FeaturedProductsAdapter(products);
        // rvFeaturedProducts.setAdapter(adapter);
    }

    /**
     * Setup Bottom Navigation
     */
    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_home);
        
        bottomNavigation.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                
                if (itemId == R.id.nav_home) {
                    // Already on home
                    return true;
                } else if (itemId == R.id.nav_products) {
                    Intent intent = new Intent(MainActivity.this, ProductListActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_cart) {
                    Intent intent = new Intent(MainActivity.this, CartActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    return true;
                }
                
                return false;
            }
        });
    }

    /**
     * Đăng xuất
     */
    private void logout() {
        RetrofitClient retrofitClient = RetrofitClient.getInstance(this);
        retrofitClient.clearToken();
        
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}