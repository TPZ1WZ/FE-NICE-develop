package com.example.nike_fe.ui.order;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nike_fe.R;
import com.example.nike_fe.adapter.OrderAdapter;
import com.example.nike_fe.data.api.OrderApi;
import com.example.nike_fe.data.api.RetrofitClient;
import com.example.nike_fe.data.model.Order;
import com.example.nike_fe.ui.auth.LoginActivity;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderHistoryActivity extends AppCompatActivity {

    private RecyclerView rvOrders;
    private FrameLayout layoutLoading;
    private TextView tvEmptyState;
    private TabLayout tabLayout;
    private ImageView ivSearch, ivMore;

    private OrderAdapter orderAdapter;
    private List<Order> allOrders = new ArrayList<>(); // Store all fetched orders
    private List<Order> displayedOrders = new ArrayList<>(); // Store filtered orders
    private OrderApi orderApi;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        initViews();
        setupRecyclerView();
        setupTabs();
        loadOrders();
    }

    private void initViews() {
        rvOrders = findViewById(R.id.rvOrders);
        layoutLoading = findViewById(R.id.layoutLoading);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        tabLayout = findViewById(R.id.tabLayout);
        ivSearch = findViewById(R.id.ivSearch);
        ivMore = findViewById(R.id.ivMore);

        RetrofitClient retrofitClient = RetrofitClient.getInstance(this);
        orderApi = retrofitClient.getOrderApi();
        token = retrofitClient.getToken();

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return;
        }

        // Search and More icons functionality can be added here
        ivSearch.setOnClickListener(v -> Toast.makeText(this, "Search clicked", Toast.LENGTH_SHORT).show());
        ivMore.setOnClickListener(v -> Toast.makeText(this, "More options clicked", Toast.LENGTH_SHORT).show());
    }

    private void setupRecyclerView() {
        orderAdapter = new OrderAdapter(this, displayedOrders, new OrderAdapter.OnOrderClickListener() {
            @Override
            public void onOrderClick(Order order) {
                Intent intent = new Intent(OrderHistoryActivity.this, OrderDetailActivity.class);
                intent.putExtra("order_id", order.getId());
                startActivity(intent);
            }

            @Override
            public void onBuyAgainClick(Order order) {
                handleBuyAgain(order);
            }

            @Override
            public void onReviewClick(Order order) {
                handleLeaveReview(order);
            }
        });

        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(orderAdapter);
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Đang hoạt động"));
        tabLayout.addTab(tabLayout.newTab().setText("Hoàn tất"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterOrders(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void filterOrders(int tabIndex) {
        displayedOrders.clear();
        if (allOrders.isEmpty()) {
            orderAdapter.notifyDataSetChanged();
            return;
        }

        for (Order order : allOrders) {
            String status = order.getStatus() != null ? order.getStatus().toUpperCase() : "";
            boolean isCompleted = status.equals("COMPLETED") || status.equals("CANCELLED")
                    || status.equals("DELIVERED");

            if (tabIndex == 0) { // Active
                if (!isCompleted) {
                    displayedOrders.add(order);
                }
            } else { // Completed
                if (isCompleted) {
                    displayedOrders.add(order);
                }
            }
        }

        orderAdapter.notifyDataSetChanged();
        showEmptyState(displayedOrders.isEmpty());
    }

    private void loadOrders() {
        showLoading(true);

        orderApi.getUserOrders("Bearer " + token).enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    allOrders.clear();
                    allOrders.addAll(response.body());
                    // Filter based on current tab selection
                    filterOrders(tabLayout.getSelectedTabPosition());
                } else {
                    Toast.makeText(OrderHistoryActivity.this,
                            "Failed to load orders", Toast.LENGTH_SHORT).show();
                    showEmptyState(true);
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(OrderHistoryActivity.this,
                        "Connection error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showEmptyState(true);
            }
        });
    }

    private void showLoading(boolean show) {
        layoutLoading.setVisibility(show ? View.VISIBLE : View.GONE);
        rvOrders.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmptyState(boolean show) {
        tvEmptyState.setVisibility(show ? View.VISIBLE : View.GONE);
        // Don't hide RecyclerView here strictly, because empty state is an overlay or
        // sibling.
        // But logic above hides RV if empty... wait.
        // If show is true, Hide RV?
        // Actually, if we filter and get 0 results, we show empty state.
        rvOrders.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void handleBuyAgain(Order order) {
        if (order == null || order.getId() == null) {
            Toast.makeText(this, "Không thể mua lại đơn hàng này", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        RetrofitClient.getInstance(this).getCartApi()
                .reorderFromOrder("Bearer " + token, order.getId())
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        showLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            Map<String, Object> result = response.body();
                            String message = (String) result.get("message");

                            // Kiểm tra nếu có sản phẩm không thể thêm
                            List<String> unavailableProducts = (List<String>) result.get("unavailableProducts");
                            if (unavailableProducts != null && !unavailableProducts.isEmpty()) {
                                StringBuilder fullMessage = new StringBuilder(
                                        message != null ? message : "Đã thêm vào giỏ hàng");
                                fullMessage.append("\n\nSản phẩm không thể thêm:");
                                for (String product : unavailableProducts) {
                                    fullMessage.append("\n- ").append(product);
                                }
                                Toast.makeText(OrderHistoryActivity.this, fullMessage.toString(), Toast.LENGTH_LONG)
                                        .show();
                            } else {
                                Toast.makeText(OrderHistoryActivity.this,
                                        message != null ? message : "Đã thêm vào giỏ hàng thành công",
                                        Toast.LENGTH_SHORT).show();
                            }

                            // Chuyển sang màn giỏ hàng
                            Intent intent = new Intent(OrderHistoryActivity.this,
                                    com.example.nike_fe.ui.cart.CartActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(OrderHistoryActivity.this,
                                    "Không thể mua lại đơn hàng", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        showLoading(false);
                        Toast.makeText(OrderHistoryActivity.this,
                                "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleLeaveReview(Order order) {
        if (order == null || order.getId() == null) {
            Toast.makeText(this, "Không thể đánh giá đơn hàng này", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, OrderReviewActivity.class);
        intent.putExtra("order_id", order.getId());
        startActivity(intent);
    }
}
