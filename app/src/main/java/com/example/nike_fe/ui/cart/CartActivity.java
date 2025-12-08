package com.example.nike_fe.ui.cart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import com.example.nike_fe.R;
import com.example.nike_fe.adapter.CartAdapter;
import com.example.nike_fe.data.api.CartApi;
import com.example.nike_fe.data.api.RetrofitClient;
import com.example.nike_fe.data.model.AddToCartRequest;
import com.example.nike_fe.data.model.CartItem;
import com.example.nike_fe.data.model.CartResponse;
import com.example.nike_fe.ui.product.ProductListActivity;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartActivity extends AppCompatActivity {
    
    private RecyclerView rvCartItems;
    private Button btnCheckout, btnContinueShopping;
    private TextView tvTotalQuantity, tvTotalPrice;
    private FrameLayout layoutLoading;
    private LinearLayout layoutEmptyCart;
    private MaterialCardView cardOrderSummary;
    
    private CartAdapter adapter;
    private CartApi cartApi;
    private String token;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        
        initViews();
        setupToolbar();
        setupRecyclerView();
        loadCart();
    }
    private void initViews() {
        rvCartItems = findViewById(R.id.rvCartItems);
        btnCheckout = findViewById(R.id.btnCheckout);
        btnContinueShopping = findViewById(R.id.btnContinueShopping);
        tvTotalQuantity = findViewById(R.id.tvCartCount);
        tvTotalPrice = findViewById(R.id.tvTotal);
        layoutLoading = findViewById(R.id.layoutLoading);
        layoutEmptyCart = findViewById(R.id.layoutEmptyCart);
        cardOrderSummary = findViewById(R.id.cardOrderSummary);
        
        RetrofitClient retrofitClient = RetrofitClient.getInstance(this);
        cartApi = retrofitClient.getCartApi();
        token = retrofitClient.getToken();
        
        // Debug log
        android.util.Log.d("CartActivity", "Token: " + (token != null ? "exists (length=" + token.length() + ")" : "null"));
        
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem giỏ hàng", Toast.LENGTH_LONG).show();
            // Redirect to login
            Intent intent = new Intent(this, com.example.nike_fe.ui.auth.LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }
    }
    
    private void setupToolbar() {
        btnCheckout.setOnClickListener(v -> navigateToCheckout());
        
        btnContinueShopping.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProductListActivity.class);
            startActivity(intent);
        });
    }
    
    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvCartItems.setLayoutManager(layoutManager);
        
        adapter = new CartAdapter(this, new CartAdapter.OnCartItemListener() {
            @Override
            public void onQuantityChanged(CartItem item, int newQuantity) {
                updateCartItemQuantity(item, newQuantity);
            }
            
            @Override
            public void onItemRemoved(CartItem item) {
                showDeleteConfirmation(item);
            }
        });
        
        rvCartItems.setAdapter(adapter);
    }
    
    private void loadCart() {
        layoutLoading.setVisibility(View.VISIBLE);
        
        cartApi.getCart("Bearer " + token).enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(Call<CartResponse> call, Response<CartResponse> response) {
                layoutLoading.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    CartResponse cart = response.body();
                    displayCart(cart);
                } else {
                    Toast.makeText(CartActivity.this, 
                            "Lỗi tải giỏ hàng: " + response.code(), 
                            Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<CartResponse> call, Throwable t) {
                layoutLoading.setVisibility(View.GONE);
                Toast.makeText(CartActivity.this, 
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void displayCart(CartResponse cart) {
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            // Show empty cart
            layoutEmptyCart.setVisibility(View.VISIBLE);
            rvCartItems.setVisibility(View.GONE);
            cardOrderSummary.setVisibility(View.GONE);
            tvTotalQuantity.setText("Sản phẩm (0)");
            tvTotalPrice.setText("0₫");
        } else {
            // Show cart items
            layoutEmptyCart.setVisibility(View.GONE);
            rvCartItems.setVisibility(View.VISIBLE);
            cardOrderSummary.setVisibility(View.VISIBLE);
            
            adapter.setCartItems(cart.getItems());
            
            // Update summary
            tvTotalQuantity.setText("Sản phẩm (" + cart.getTotalQuantity() + ")");
            
            NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
            String formattedPrice = formatter.format(cart.getTotalPrice()) + "₫";
            tvTotalPrice.setText(formattedPrice);
        }
    }
    
    private void updateCartItemQuantity(CartItem item, int newQuantity) {
        AddToCartRequest request = new AddToCartRequest(
                item.getProduct().getId(),
                newQuantity,
                item.getSize()
        );
        
        cartApi.updateQuantity("Bearer " + token, request)
                .enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    loadCart(); // Reload cart
                } else {
                    Toast.makeText(CartActivity.this, 
                            "Lỗi cập nhật số lượng", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Toast.makeText(CartActivity.this, 
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showDeleteConfirmation(CartItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa sản phẩm")
                .setMessage("Bạn có chắc muốn xóa sản phẩm này khỏi giỏ hàng?")
                .setPositiveButton("Xóa", (dialog, which) -> removeCartItem(item))
                .setNegativeButton("Hủy", null)
                .show();
    }
    
    private void removeCartItem(CartItem item) {
        cartApi.removeItem("Bearer " + token, item.getProduct().getId(), item.getSize())
                .enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CartActivity.this, "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show();
                    loadCart(); // Reload cart
                } else {
                    Toast.makeText(CartActivity.this, 
                            "Lỗi xóa sản phẩm", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Toast.makeText(CartActivity.this, 
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void navigateToCheckout() {
        if (adapter == null || adapter.getItemCount() == 0) {
            Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Calculate subtotal from cart items
        double subtotal = 0.0;
        for (int i = 0; i < adapter.getItemCount(); i++) {
            CartItem item = adapter.getItems().get(i);
            subtotal += item.getTotalPrice();
        }
        
        Intent intent = new Intent(this, com.example.nike_fe.ui.checkout.CheckoutActivity.class);
        intent.putExtra("subtotal", subtotal);
        startActivity(intent);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Only reload cart if token exists
        if (token != null && !token.isEmpty()) {
            loadCart();
        }
    }
}
