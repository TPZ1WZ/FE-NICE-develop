package com.example.nike_fe.ui.cart;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nike_fe.R;
import com.example.nike_fe.adapter.CartAdapter;
import com.example.nike_fe.data.api.CartApi;
import com.example.nike_fe.data.api.RetrofitClient;
import com.example.nike_fe.data.model.AddToCartRequest;
import com.example.nike_fe.data.model.CartItem;
import com.example.nike_fe.data.model.CartResponse;
import com.example.nike_fe.ui.product.ProductListActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;

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
    // private MaterialCardView cardOrderSummary; // Removed from UI

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
        // cardOrderSummary = findViewById(R.id.cardOrderSummary);

        RetrofitClient retrofitClient = RetrofitClient.getInstance(this);
        cartApi = retrofitClient.getCartApi();
        token = retrofitClient.getToken();

        // Debug log
        android.util.Log.d("CartActivity",
                "Token: " + (token != null ? "exists (length=" + token.length() + ")" : "null"));

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
        android.util.Log.d("CartActivity",
                "🛒 Loading cart with token: Bearer " + token.substring(0, Math.min(20, token.length())) + "...");

        cartApi.getCart("Bearer " + token).enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(Call<CartResponse> call, Response<CartResponse> response) {
                layoutLoading.setVisibility(View.GONE);
                android.util.Log.d("CartActivity", "📦 Cart response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    CartResponse cart = response.body();
                    android.util.Log.d("CartActivity", "✅ Cart loaded - Items count: " +
                            (cart.getItems() != null ? cart.getItems().size() : "null") +
                            ", Total quantity: " + cart.getTotalQuantity());
                    displayCart(cart);
                } else {
                    android.util.Log.e("CartActivity",
                            "❌ Cart load failed: " + response.code() + " - " + response.message());
                    try {
                        if (response.errorBody() != null) {
                            android.util.Log.e("CartActivity", "Error body: " + response.errorBody().string());
                        }
                    } catch (Exception e) {
                        android.util.Log.e("CartActivity", "Error reading error body", e);
                    }
                    Toast.makeText(CartActivity.this,
                            "Lỗi tải giỏ hàng: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CartResponse> call, Throwable t) {
                layoutLoading.setVisibility(View.GONE);
                android.util.Log.e("CartActivity", "💥 Cart load failure", t);
                Toast.makeText(CartActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private CartResponse currentCart;

    private void displayCart(CartResponse cart) {
        this.currentCart = cart;
        android.util.Log.d("CartActivity", "📊 displayCart called - Items: " +
                (cart.getItems() != null ? cart.getItems().size() : "null") +
                ", isEmpty: " + (cart.getItems() == null || cart.getItems().isEmpty()));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            // Show empty cart
            android.util.Log.d("CartActivity", "🛒 Showing empty cart");
            layoutEmptyCart.setVisibility(View.VISIBLE);
            rvCartItems.setVisibility(View.GONE);
            // cardOrderSummary.setVisibility(View.GONE);
            tvTotalQuantity.setText("Sản phẩm (0)");
            tvTotalPrice.setText("$0.00");
        } else {
            // Show cart items
            android.util.Log.d("CartActivity", "✅ Showing cart with " + cart.getItems().size() + " items");
            layoutEmptyCart.setVisibility(View.GONE);
            rvCartItems.setVisibility(View.VISIBLE);
            // cardOrderSummary.setVisibility(View.VISIBLE);

            adapter.setCartItems(cart.getItems());

            updateCartSummaryUI();
        }
    }

    private void updateCartSummaryUI() {
        if (currentCart != null) {
            tvTotalQuantity.setText("Sản phẩm (" + currentCart.getTotalQuantity() + ")");
            NumberFormat vnFormatter = NumberFormat.getInstance(new Locale("vi", "VN"));
            String formattedPrice = vnFormatter.format(currentCart.getTotalPrice()) + "₫";
            tvTotalPrice.setText(formattedPrice);
        }
    }

    private void updateCartItemQuantity(CartItem item, int newQuantity) {
        AddToCartRequest request = new AddToCartRequest(
                item.getProduct().getId(),
                newQuantity,
                item.getSize());

        cartApi.updateQuantity("Bearer " + token, request)
                .enqueue(new Callback<Map<String, String>>() {
                    @Override
                    public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                        if (response.isSuccessful()) {
                            loadCart(); // Reload cart for quantity updates as price calc is complex
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
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        View bottomSheetView = LayoutInflater.from(this).inflate(
                R.layout.layout_bottom_sheet_remove_cart,
                null // Do not attach to root
        );

        // Bind data to bottom sheet view
        ImageView ivProductImage = bottomSheetView.findViewById(R.id.ivProductImage);
        TextView tvProductName = bottomSheetView.findViewById(R.id.tvProductName);
        TextView tvSize = bottomSheetView.findViewById(R.id.tvSize);
        TextView tvPrice = bottomSheetView.findViewById(R.id.tvPrice);
        TextView tvQuantity = bottomSheetView.findViewById(R.id.tvQuantity);

        tvProductName.setText(item.getProduct().getName());
        tvSize.setText("Size: " + item.getSize()); // Adjust format if needed

        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        tvPrice.setText(formatter.format(item.getTotalPrice()) + "₫");

        tvQuantity.setText(String.valueOf(item.getQuantity()));

        if (item.getProduct().getImages() != null && !item.getProduct().getImages().isEmpty()) {
            Glide.with(this).load(item.getProduct().getImages().get(0)).into(ivProductImage);
        }

        // Handle buttons
        Button btnCancel = bottomSheetView.findViewById(R.id.btnCancel);
        Button btnConfirmRemove = bottomSheetView.findViewById(R.id.btnConfirmRemove);

        btnCancel.setOnClickListener(v -> bottomSheetDialog.dismiss());

        btnConfirmRemove.setOnClickListener(v -> {
            removeCartItem(item);
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    private void removeCartItem(CartItem item) {
        cartApi.removeItem("Bearer " + token, item.getProduct().getId(), item.getSize())
                .enqueue(new Callback<Map<String, String>>() {
                    @Override
                    public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(CartActivity.this, "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show();

                            // Remove locally first for immediate feedback
                            if (adapter != null && currentCart != null) {
                                adapter.getItems().remove(item);
                                adapter.notifyDataSetChanged();

                                // Update summary locally
                                double itemTotal = item.getTotalPrice();
                                int itemQty = item.getQuantity();

                                currentCart.setTotalPrice(currentCart.getTotalPrice() - itemTotal);
                                currentCart.setTotalQuantity(currentCart.getTotalQuantity() - itemQty);

                                updateCartSummaryUI();

                                btnCheckout.setEnabled(adapter.getItemCount() > 0);

                                // If cart empty //
                                if (adapter.getItemCount() == 0) {
                                    layoutEmptyCart.setVisibility(View.VISIBLE);
                                    rvCartItems.setVisibility(View.GONE);
                                }
                            }

                            // DO NOT call loadCart() here to avoid stale data
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
