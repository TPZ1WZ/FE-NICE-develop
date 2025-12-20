package com.example.nike_fe.ui.order;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nike_fe.R;
import com.example.nike_fe.adapter.OrderItemAdapter;
import com.example.nike_fe.data.api.OrderApi;
import com.example.nike_fe.data.api.RetrofitClient;
import com.example.nike_fe.data.model.Order;
import com.example.nike_fe.ui.auth.LoginActivity;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderDetailActivity extends AppCompatActivity {
    
    private ImageView ivBack;
    private TextView tvOrderId, tvOrderDate, tvOrderStatus;
    private TextView tvFullName, tvPhone, tvAddress;
    private TextView tvPaymentMethod, tvSubtotal, tvShipping, tvTotal;
    private RecyclerView rvOrderItems;
    private FrameLayout layoutLoading;
    
    private OrderItemAdapter orderItemAdapter;
    private OrderApi orderApi;
    private String token;
    private Long orderId;
    
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
        rvOrderItems = findViewById(R.id.rvOrderItems);
        layoutLoading = findViewById(R.id.layoutLoading);
        
        RetrofitClient retrofitClient = RetrofitClient.getInstance(this);
        orderApi = retrofitClient.getOrderApi();
        token = retrofitClient.getToken();
        
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return;
        }
        
        ivBack.setOnClickListener(v -> finish());
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
        
        // Order items
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            orderItemAdapter = new OrderItemAdapter(this, order.getItems());
            rvOrderItems.setAdapter(orderItemAdapter);
        }
        
        // Pricing
        tvSubtotal.setText(formatPrice(order.getTotalAmount()));
        
        // Calculate shipping from finalAmount - totalAmount + totalDiscount
        Double shipping = 0.0;
        if (order.getFinalAmount() != null && order.getTotalAmount() != null) {
            shipping = order.getFinalAmount() - order.getTotalAmount();
            if (order.getTotalDiscount() != null) {
                shipping += order.getTotalDiscount();
            }
        }
        tvShipping.setText(formatPrice(shipping));
        tvTotal.setText(formatPrice(order.getFinalAmount()));
    }
    
    private String formatPrice(Double price) {
        if (price == null) return "0 ₫";
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(price) + " ₫";
    }
    
    private String formatDate(String dateStr) {
        if (dateStr == null) return "";
        
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
        if (status == null) return "Không xác định";
        
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
}
