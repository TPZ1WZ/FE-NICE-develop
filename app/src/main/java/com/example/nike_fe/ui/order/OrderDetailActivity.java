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
    private TextView tvPaymentMethod, tvSubtotal, tvDiscount, tvShipping, tvTotal;
    private TextView tvNikeCoinDiscount;
    private LinearLayout layoutNikeCoinDiscount;
    private TextView tvCustomerNote, tvCustomerNoteLabel;
    private TextView tvAdminNote, tvAdminNoteLabel;
    private RecyclerView rvOrderItems;
    private FrameLayout layoutLoading;
    private LinearLayout layoutActionButtons;
    private LinearLayout layoutCancelButton;
    private Button btnReorder, btnReview, btnCancelOrder;

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
        tvDiscount = findViewById(R.id.tvDiscount);
        tvNikeCoinDiscount = findViewById(R.id.tvNikeCoinDiscount);
        layoutNikeCoinDiscount = findViewById(R.id.layoutNikeCoinDiscount);
        tvShipping = findViewById(R.id.tvShipping);
        tvTotal = findViewById(R.id.tvTotal);
        tvCustomerNote = findViewById(R.id.tvCustomerNote);
        tvCustomerNoteLabel = findViewById(R.id.tvCustomerNoteLabel);
        tvAdminNote = findViewById(R.id.tvAdminNote);
        tvAdminNoteLabel = findViewById(R.id.tvAdminNoteLabel);
        rvOrderItems = findViewById(R.id.rvOrderItems);
        layoutLoading = findViewById(R.id.layoutLoading);
        layoutActionButtons = findViewById(R.id.layoutActionButtons);
        layoutCancelButton = findViewById(R.id.layoutCancelButton);
        btnReorder = findViewById(R.id.btnReorder);
        btnReview = findViewById(R.id.btnReview);
        btnCancelOrder = findViewById(R.id.btnCancelOrder);

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
        btnCancelOrder.setOnClickListener(v -> handleCancelOrder());
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

        // Admin note
        if (order.getAdminNote() != null && !order.getAdminNote().trim().isEmpty()) {
            tvAdminNote.setText(order.getAdminNote());
            tvAdminNote.setVisibility(View.VISIBLE);
            tvAdminNoteLabel.setVisibility(View.VISIBLE);
        } else {
            tvAdminNote.setVisibility(View.GONE);
            tvAdminNoteLabel.setVisibility(View.GONE);
        }

        // Order items
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            orderItemAdapter = new OrderItemAdapter(this, order.getItems());
            rvOrderItems.setAdapter(orderItemAdapter);
        }

        // Pricing
        tvSubtotal.setText(formatPrice(order.getTotalAmount()));

        // Discount (coupon only - backend đã tách riêng)
        Double discount = order.getTotalDiscount() != null ? order.getTotalDiscount() : 0.0;
        
        if (discount > 0) {
            tvDiscount.setText("-" + formatPrice(discount));
            tvDiscount.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        } else {
            tvDiscount.setText("0 ₫");
            tvDiscount.setTextColor(getResources().getColor(R.color.black));
        }
        
        // Nike Coin discount (field riêng từ backend)
        Integer nikeCoin = order.getNikeCoinUsed() != null ? order.getNikeCoinUsed() : 0;
        android.util.Log.d("OrderDetailActivity", "Nike Coin Used: " + nikeCoin);
        if (nikeCoin > 0) {
            layoutNikeCoinDiscount.setVisibility(View.VISIBLE);
            tvNikeCoinDiscount.setText("-" + formatPrice(nikeCoin.doubleValue()));
            android.util.Log.d("OrderDetailActivity", "Nike Coin section VISIBLE");
        } else {
            layoutNikeCoinDiscount.setVisibility(View.GONE);
            android.util.Log.d("OrderDetailActivity", "Nike Coin section GONE");
        }

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
            case "CANCELED":
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
            layoutCancelButton.setVisibility(View.GONE);
            return;
        }

        String upperStatus = status.toUpperCase();
        
        // Hiển thị nút Mua lại và Đánh giá cho đơn hàng đã hoàn thành
        if (upperStatus.equals("COMPLETED") || upperStatus.equals("DELIVERED")) {
            layoutActionButtons.setVisibility(View.VISIBLE);
            layoutCancelButton.setVisibility(View.GONE);
        } 
        // Hiển thị nút Hủy đơn hàng cho đơn hàng Chờ xác nhận hoặc Đã xác nhận
        else if (upperStatus.equals("PENDING") || upperStatus.equals("CONFIRMED")) {
            layoutActionButtons.setVisibility(View.GONE);
            layoutCancelButton.setVisibility(View.VISIBLE);
        } 
        else {
            layoutActionButtons.setVisibility(View.GONE);
            layoutCancelButton.setVisibility(View.GONE);
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

    private void handleCancelOrder() {
        if (currentOrder == null) {
            Toast.makeText(this, "Không thể hủy đơn hàng này", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hiển thị dialog xác nhận
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Xác nhận hủy đơn hàng")
            .setMessage("Bạn có chắc chắn muốn hủy đơn hàng #" + currentOrder.getId() + " không?")
            .setPositiveButton("Hủy đơn hàng", (dialog, which) -> {
                showLoading(true);
                orderApi.cancelOrder("Bearer " + token, currentOrder.getId())
                    .enqueue(new Callback<Map<String, Object>>() {
                        @Override
                        public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                            showLoading(false);
                            
                            if (response.isSuccessful() && response.body() != null) {
                                Map<String, Object> result = response.body();
                                String message = (String) result.get("message");
                                Toast.makeText(OrderDetailActivity.this, 
                                    message != null ? message : "Đơn hàng đã được hủy thành công", 
                                    Toast.LENGTH_SHORT).show();
                                
                                // Tải lại chi tiết đơn hàng để cập nhật trạng thái
                                loadOrderDetail();
                                
                                // Thông báo cho Activity trước đó biết cần refresh danh sách
                                setResult(RESULT_OK);
                            } else {
                                Toast.makeText(OrderDetailActivity.this, 
                                    "Không thể hủy đơn hàng. Vui lòng thử lại", 
                                    Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                            showLoading(false);
                            Toast.makeText(OrderDetailActivity.this, 
                                "Lỗi: " + t.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                        }
                    });
            })
            .setNegativeButton("Không", null)
            .show();
    }
}
