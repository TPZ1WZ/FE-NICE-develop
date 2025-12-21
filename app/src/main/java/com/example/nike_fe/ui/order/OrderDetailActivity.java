package com.example.nike_fe.ui.order;

import android.content.Intent;
import android.os.Bundle;
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

import com.example.nike_fe.R;
import com.example.nike_fe.adapter.OrderItemAdapter;
import com.example.nike_fe.data.api.CartApi;
import com.example.nike_fe.data.api.OrderApi;
import com.example.nike_fe.data.api.RetrofitClient;
import com.example.nike_fe.data.model.Order;
import com.example.nike_fe.ui.auth.LoginActivity;
import com.example.nike_fe.ui.cart.CartActivity;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderDetailActivity extends AppCompatActivity {

    private ImageView ivBack;
    private TextView tvOrderId, tvOrderDate, tvOrderStatus;
    private TextView tvFullName, tvPhone, tvAddress;
    private TextView tvPaymentMethod, tvSubtotal, tvShipping, tvTotal;
    private TextView tvCustomerNote, tvCustomerNoteLabel;
    private RecyclerView rvOrderItems;
    private FrameLayout layoutLoading;
    private LinearLayout layoutActionButtons;
    private Button btnReorder, btnReview;

    private OrderItemAdapter orderItemAdapter;
    private OrderApi orderApi;
    private CartApi cartApi;
    private String token;
    private Long orderId;
    private Order currentOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        orderId = getIntent().getLongExtra("order_id", -1);
        if (orderId == -1) {
            Toast.makeText(this, "Lỗi: Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        loadOrderDetail();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        tvOrderId = findViewById(R.id.tvOrderId);
        tvOrderDate = findViewById(R.id.tvOrderDate);
        tvOrderStatus = findViewById(R.id.tvOrderStatus);
        tvFullName = findViewById(R.id.tvFullName);
        tvPhone = findViewById(R.id.tvPhone);
        tvAddress = findViewById(R.id.tvAddress);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvShipping = findViewById(R.id.tvShipping);
        tvTotal = findViewById(R.id.tvTotal);
        tvCustomerNote = findViewById(R.id.tvCustomerNote);
        tvCustomerNoteLabel = findViewById(R.id.tvCustomerNoteLabel);
        rvOrderItems = findViewById(R.id.rvOrderItems);
        layoutLoading = findViewById(R.id.layoutLoading);
        layoutActionButtons = findViewById(R.id.layoutActionButtons);
        btnReorder = findViewById(R.id.btnReorder);
        btnReview = findViewById(R.id.btnReview);

        RetrofitClient retrofitClient = RetrofitClient.getInstance(this);
        orderApi = retrofitClient.getOrderApi();
        cartApi = retrofitClient.getCartApi();
        token = retrofitClient.getToken();

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return;
        }

        ivBack.setOnClickListener(v -> finish());
        btnReorder.setOnClickListener(v -> handleReorder());
        btnReview.setOnClickListener(v -> handleReview());
    }

    private void setupRecyclerView() {
        rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadOrderDetail() {
        showLoading(true);

        orderApi.getOrderById("Bearer " + token, orderId).enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    displayOrderDetail(response.body());
                } else {
                    Toast.makeText(OrderDetailActivity.this,
                            "Không thể tải chi tiết đơn hàng", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                showLoading(false);
                Toast.makeText(OrderDetailActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void displayOrderDetail(Order order) {
        currentOrder = order;

        tvOrderId.setText("Đơn hàng #" + order.getId());
        tvOrderDate.setText(formatDate(order.getCreatedAt()));
        tvOrderStatus.setText(getStatusText(order.getStatus()));

        // Display receiver name (not user account name)
        String receiverName = order.getReceiverName() != null ? order.getReceiverName() : "N/A";
        tvFullName.setText(receiverName);

        tvPhone.setText(order.getPhone() != null ? order.getPhone() : "N/A");

        // Shipping address (already formatted: "address, district, city")
        String shippingAddress = order.getShippingAddress();
        if (shippingAddress != null && !shippingAddress.isEmpty()) {
            tvAddress.setText(shippingAddress);
        } else {
            tvAddress.setText("N/A");
        }

        // Payment method
        String paymentMethod = order.getPaymentMethod();
        if ("COD".equalsIgnoreCase(paymentMethod)) {
            tvPaymentMethod.setText("Thanh toán khi nhận hàng (COD)");
        } else if ("VNPAY".equalsIgnoreCase(paymentMethod)) {
            tvPaymentMethod.setText("Thanh toán qua VNPay");
        } else {
            tvPaymentMethod.setText(paymentMethod != null ? paymentMethod : "N/A");
        }

        // Customer note
        if (order.getCustomerNote() != null && !order.getCustomerNote().trim().isEmpty()) {
            tvCustomerNote.setText(order.getCustomerNote());
            tvCustomerNote.setVisibility(View.VISIBLE);
            tvCustomerNoteLabel.setVisibility(View.VISIBLE);
        } else {
            tvCustomerNote.setVisibility(View.GONE);
            tvCustomerNoteLabel.setVisibility(View.GONE);
        }

        // Order items
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            orderItemAdapter = new OrderItemAdapter(this, order.getItems());
            rvOrderItems.setAdapter(orderItemAdapter);
        }

        // Pricing
        tvSubtotal.setText(formatPrice(order.getTotalAmount()));

        // Shipping fee
        Double shippingFee = order.getShippingFee() != null ? order.getShippingFee() : 0.0;
        tvShipping.setText(formatPrice(shippingFee));

        tvTotal.setText(formatPrice(order.getFinalAmount()));

        // Hiển thị nút hành động nếu đơn hàng đã hoàn thành
        updateActionButtons(order.getStatus());
    }

    private String formatPrice(Double price) {
        if (price == null)
            return "0 ₫";
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(price) + " ₫";
    }

    private String formatDate(String dateStr) {
        if (dateStr == null)
            return "";

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateStr;
        }
    }

    private String getStatusText(String status) {
        if (status == null)
            return "Không xác định";

        switch (status.toUpperCase()) {
            case "PENDING":
                return "Chờ xác nhận";
            case "CONFIRMED":
                return "Đã xác nhận";
            case "PROCESSING":
                return "Đang xử lý";
            case "SHIPPING":
                return "Đang giao hàng";
            case "DELIVERED":
                return "Đã giao hàng";
            case "COMPLETED":
                return "Hoàn thành";
            case "CANCELLED":
                return "Đã hủy";
            default:
                return status;
        }
    }

    private void showLoading(boolean show) {
        layoutLoading.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void updateActionButtons(String status) {
        if (status == null) {
            layoutActionButtons.setVisibility(View.GONE);
            return;
        }

        String upperStatus = status.toUpperCase();
        if (upperStatus.equals("COMPLETED") || upperStatus.equals("DELIVERED")) {
            layoutActionButtons.setVisibility(View.VISIBLE);
        } else {
            layoutActionButtons.setVisibility(View.GONE);
        }
    }

    private void handleReorder() {
        if (currentOrder == null) {
            Toast.makeText(this, "Không thể mua lại đơn hàng này", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        cartApi.reorderFromOrder("Bearer " + token, currentOrder.getId())
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
                                StringBuilder fullMessage = new StringBuilder(message);
                                fullMessage.append("\n\nSản phẩm không thể thêm:");
                                for (String product : unavailableProducts) {
                                    fullMessage.append("\n- ").append(product);
                                }
                                Toast.makeText(OrderDetailActivity.this, fullMessage.toString(), Toast.LENGTH_LONG)
                                        .show();
                            } else {
                                Toast.makeText(OrderDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                            }

                            // Chuyển sang màn giỏ hàng
                            Intent intent = new Intent(OrderDetailActivity.this, CartActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(OrderDetailActivity.this,
                                    "Không thể mua lại đơn hàng", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        showLoading(false);
                        Toast.makeText(OrderDetailActivity.this,
                                "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleReview() {
        Intent intent = new Intent(this, OrderReviewActivity.class);
        intent.putExtra("order_id", orderId);
        startActivity(intent);
    }
}
