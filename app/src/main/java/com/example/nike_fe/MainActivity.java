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
import com.example.nike_fe.data.model.Product;
import com.example.nike_fe.data.model.User;
import com.example.nike_fe.ui.auth.LoginActivity;
import com.example.nike_fe.ui.home.BrandAdapter;
import com.example.nike_fe.ui.home.FilterAdapter;
import com.example.nike_fe.ui.profile.ProfileActivity;
import com.example.nike_fe.ui.product.ProductDetailActivity;
import com.example.nike_fe.ui.cart.CartActivity;
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
    private ImageView btnNotification, btnWishlist;
    private User currentUser;

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
        btnWishlist = findViewById(R.id.btnWishlist);

        bottomNavigation = findViewById(R.id.bottomNavigation);
        fabCart = findViewById(R.id.fabCart);

        // Filter Button Logic
        View btnFilter = findViewById(R.id.btnFilter);
        if (btnFilter != null) {
            btnFilter.setOnClickListener(v -> {
                com.example.nike_fe.ui.home.FilterBottomSheetFragment filterFragment = new com.example.nike_fe.ui.home.FilterBottomSheetFragment();
                filterFragment.show(getSupportFragmentManager(), "filter_dialog");
                Toast.makeText(this, "Opening Filters...", Toast.LENGTH_SHORT).show(); // Debug toast
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

    private void setupBottomNavigation() {
        if (bottomNavigation != null) {
            bottomNavigation.setBackground(null); // Clear background for FAB curve
            bottomNavigation.getMenu().getItem(2).setEnabled(false); // Disable placeholder item for FAB

            bottomNavigation.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    return true;
                } else if (id == R.id.nav_favorites) {
                    Toast.makeText(this, "Wishlist", Toast.LENGTH_SHORT).show();
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

    private void setupRecyclerViews() {
        // 1. Brands
        List<String> brands = Arrays.asList("Nike", "Adidas", "Puma", "Asics", "Reebok", "New Balance", "Converse",
                "More..");
        BrandAdapter brandAdapter = new BrandAdapter(this, brands, brandName -> {
            Toast.makeText(this, "Filter: " + brandName, Toast.LENGTH_SHORT).show();
            fetchProducts(brandName.equals("More..") ? null : brandName);
        });

        if (rvBrands != null) {
            rvBrands.setLayoutManager(new GridLayoutManager(this, 4));
            rvBrands.setAdapter(brandAdapter);
        }

        // 2. Filters
        List<String> filters = Arrays.asList("All", "Nike", "Adidas", "Puma", "Asics", "Reebok");
        FilterAdapter filterAdapter = new FilterAdapter(this, filters, filter -> {
            fetchProducts(filter.equals("All") ? null : filter);
        });

        if (rvFilters != null) {
            rvFilters.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            rvFilters.setAdapter(filterAdapter);
        }

        // 3. Product Grid
        if (rvProductGrid != null) {
            rvProductGrid.setLayoutManager(new GridLayoutManager(this, 2));
            rvProductGrid.setNestedScrollingEnabled(false); // Let the parent NestedScrollView handle scrolling
        }
    }

    private void fetchProducts(String brandQuery) {
        // Log querying for debug
        Log.d("MainActivity", "Fetching products for: " + brandQuery);

        RetrofitClient.getInstance(this).getProductApi().getProducts(brandQuery, null, null)
                .enqueue(new Callback<List<Product>>() {
                    @Override
                    public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Product> products = response.body();

                            HomeProductAdapter adapter = new HomeProductAdapter(MainActivity.this,
                                    new HomeProductAdapter.OnProductClickListener() {
                                        @Override
                                        public void onProductClick(Product product) {
                                            Intent intent = new Intent(MainActivity.this, ProductDetailActivity.class);
                                            intent.putExtra("product_id", product.getId());
                                            startActivity(intent);
                                        }

                                        @Override
                                        public void onAddClick(Product product) {
                                            Toast.makeText(MainActivity.this, "Added to cart", Toast.LENGTH_SHORT)
                                                    .show();
                                        }
                                    });

                            adapter.useGridLayout(true);
                            adapter.setProducts(products);

                            if (rvProductGrid != null) {
                                rvProductGrid.setAdapter(adapter);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Product>> call, Throwable t) {
                        Toast.makeText(MainActivity.this, "Error loading products", Toast.LENGTH_SHORT).show();
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
        } else if (id == R.id.nav_sign_out) {
            RetrofitClient.getInstance(this).clearToken();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show();
        }

        if (drawerLayout != null)
            drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}