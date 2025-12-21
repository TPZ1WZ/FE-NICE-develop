package com.example.nike_fe.ui.product;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nike_fe.R;
import com.example.nike_fe.ui.home.FilterAdapter;
import com.example.nike_fe.adapter.HomeProductAdapter;
import com.example.nike_fe.data.api.RetrofitClient;
import com.example.nike_fe.data.model.AddToCartRequest;
import com.example.nike_fe.data.model.Category;
import com.example.nike_fe.data.model.Product;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllProductsActivity extends AppCompatActivity {

    private RecyclerView rvAllProducts;
    private HomeProductAdapter productAdapter;
    private RecyclerView rvCategories;
    private FilterAdapter filterAdapter;
    private EditText etSearch;
    private View btnSort;

    private List<Product> allProducts = new ArrayList<>();
    private List<Product> filteredProducts = new ArrayList<>();
    private String currentCategory = "Tất cả";
    private String currentSearchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_products);

        initViews();
        fetchProducts();
        fetchCategories();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Search
        etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString();
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Filter/Sort Button
        btnSort = findViewById(R.id.btnSort);
        btnSort.setOnClickListener(v -> showSortDialog());

        // Categories
        rvCategories = findViewById(R.id.rvCategories);
        rvCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        filterAdapter = new FilterAdapter(this, new ArrayList<>(), category -> {
            currentCategory = category;
            applyFilters();
        });
        rvCategories.setAdapter(filterAdapter);

        // Products
        rvAllProducts = findViewById(R.id.rvAllProducts);
        rvAllProducts.setLayoutManager(new GridLayoutManager(this, 2));

        productAdapter = new HomeProductAdapter(this, new HomeProductAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(Product product) {
                Intent intent = new Intent(AllProductsActivity.this, ProductDetailActivity.class);
                intent.putExtra("product_id", product.getId());
                startActivity(intent);
            }

            @Override
            public void onAddClick(Product product) {
                addToCart(product);
            }

            @Override
            public void onFavoriteClick(Product product) {
                toggleFavorite(product);
            }
        });
        productAdapter.useGridLayout(true);
        rvAllProducts.setAdapter(productAdapter);
    }

    private void fetchProducts() {
        RetrofitClient.getInstance(this).getProductApi().getProducts(null, null, null)
                .enqueue(new Callback<List<Product>>() {
                    @Override
                    public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            allProducts = response.body();
                            applyFilters();
                        } else {
                            Toast.makeText(AllProductsActivity.this, "Không thể tải sản phẩm", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Product>> call, Throwable t) {
                        Toast.makeText(AllProductsActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchCategories() {
        RetrofitClient.getInstance(this).getCategoryApi().getCategories()
                .enqueue(new Callback<List<Category>>() {
                    @Override
                    public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<String> categoryNames = new ArrayList<>();
                            categoryNames.add("Tất cả");
                            for (Category cat : response.body()) {
                                categoryNames.add(cat.getName());
                            }
                            filterAdapter.updateFilters(categoryNames);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Category>> call, Throwable t) {
                    }
                });
    }

    private void applyFilters() {
        filteredProducts = new ArrayList<>();

        for (Product product : allProducts) {
            boolean matchesCategory = currentCategory.equals("Tất cả")
                    || (product.getCategory() != null && product.getCategory().getName().equals(currentCategory));
            boolean matchesSearch = currentSearchQuery.isEmpty()
                    || product.getName().toLowerCase().contains(currentSearchQuery.toLowerCase());

            if (matchesCategory && matchesSearch) {
                filteredProducts.add(product);
            }
        }
        productAdapter.setProducts(filteredProducts);
    }

    private void showSortDialog() {
        String[] options = { "Mới nhất", "Giá tăng dần", "Giá giảm dần", "Tên A-Z" };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sắp xếp theo");
        builder.setItems(options, (dialog, which) -> sortProducts(which));
        builder.show();
    }

    private void sortProducts(int sortOption) {
        switch (sortOption) {
            case 0: // Newest
                Collections.sort(filteredProducts, (p1, p2) -> Long.compare(p2.getId(), p1.getId()));
                break;
            case 1: // Price Low to High
                Collections.sort(filteredProducts, (p1, p2) -> Double.compare(p1.getPrice(), p2.getPrice()));
                break;
            case 2: // Price High to Low
                Collections.sort(filteredProducts, (p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));
                break;
            case 3: // Name A-Z
                Collections.sort(filteredProducts, (p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName()));
                break;
        }
        productAdapter.setProducts(filteredProducts);
    }

    private void addToCart(Product product) {
        String token = RetrofitClient.getInstance(this).getToken();
        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập trước", Toast.LENGTH_SHORT).show();
            return;
        }

        String tempSize = "42";
        if (product.getSizes() != null && !product.getSizes().isEmpty()) {
            tempSize = product.getSizes().get(0);
        }

        AddToCartRequest request = new AddToCartRequest(product.getId(), 1, tempSize);
        RetrofitClient.getInstance(this).getCartApi().addToCart("Bearer " + token, request)
                .enqueue(new Callback<Map<String, String>>() {
                    @Override
                    public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(AllProductsActivity.this, "Đã thêm vào giỏ", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AllProductsActivity.this, "Thêm thất bại", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, String>> call, Throwable t) {
                        Toast.makeText(AllProductsActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void toggleFavorite(Product product) {
        String token = RetrofitClient.getInstance(this).getToken();
        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        RetrofitClient.getInstance(this).getFavoriteApi().addToFavorites(product.getId(), "Bearer " + token)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(AllProductsActivity.this, "Đã thêm yêu thích", Toast.LENGTH_SHORT).show();
                            productAdapter.toggleFavorite(product.getId(), true);
                        } else {
                            removeFromFavorite(product.getId(), token);
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    }
                });
    }

    private void removeFromFavorite(Long productId, String token) {
        RetrofitClient.getInstance(this).getFavoriteApi().removeFromFavorites(productId, "Bearer " + token)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(AllProductsActivity.this, "Đã xóa yêu thích", Toast.LENGTH_SHORT).show();
                            productAdapter.toggleFavorite(productId, false);
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    }
                });
    }
}
