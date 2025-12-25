package com.example.nike_fe.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nike_fe.R;
import com.example.nike_fe.adapter.OrderItemAdapter;
import com.example.nike_fe.data.api.AdminApi;
import com.example.nike_fe.data.api.RetrofitClient;
import com.example.nike_fe.data.model.Order;

import java.text.NumberFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminOrderDetailActivity extends AppCompatActivity {
    
    private TextView tvOrderId, tvStatus, tvCustomerName, tvPhone, tvAddress;
    private TextView tvPaymentMethod, tvTotalAmount, tvDiscount, tvShippingFee, tvFinalAmount;
    private TextView tvCreatedAt, tvQuantity;
    private TextView tvCustomerNote, tvCustomerNoteLabel;
    private RecyclerView recyclerViewItems;
    private ProgressBar progressBar;
    private ScrollView layoutContent;
    private LinearLayout layoutActions;
    private Button btnConfirm, btnShipping, btnComplete, btnCancel;
    private ImageView ivBack;
    
    private AdminApi adminApi;
    private String token;
    private Long orderId;
    private Order currentOrder;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_admin_order_detail);
            
            orderId = getIntent().getLongExtra("order_id", -1);
            if (orderId == -1) {
                Toast.makeText(this, "Lỗi: Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            
            initViews();
            loadOrderDetail();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khởi tạo: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }
    
    private void initViews() {
        tvOrderId = findViewById(R.id.tvOrderId);
        tvStatus = findViewById(R.id.tvStatus);
        tvCustomerName = findViewById(R.id.tvCustomerName);
        tvPhone = findViewById(R.id.tvPhone);
        tvAddress = findViewById(R.id.tvAddress);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvDiscount = findViewById(R.id.tvDiscount);
        tvShippingFee = findViewById(R.id.tvShippingFee);
        tvFinalAmount = findViewById(R.id.tvFinalAmount);
        tvCreatedAt = findViewById(R.id.tvCreatedAt);
        tvQuantity = findViewById(R.id.tvQuantity);
        
        tvCustomerNote = findViewById(R.id.tvCustomerNote);
        tvCustomerNoteLabel = findViewById(R.id.tvCustomerNoteLabel);
        
        recyclerViewItems = findViewById(R.id.recyclerViewItems);
        progressBar = findViewById(R.id.progressBar);
        layoutContent = findViewById(R.id.layoutContent);
        layoutActions = findViewById(R.id.layoutActions);
        
        btnConfirm = findViewById(R.id.btnConfirm);
        btnShipping = findViewById(R.id.btnShipping);
        btnComplete = findViewById(R.id.btnComplete);
        btnCancel = findViewById(R.id.btnCancel);
        ivBack = findViewById(R.id.ivBack);
        
        RetrofitClient retrofitClient = RetrofitClient.getInstance(this);
        adminApi = retrofitClient.getAdminApi();
        token = retrofitClient.getToken();
        
        ivBack.setOnClickListener(v -> finish());
        
        btnConfirm.setOnClickListener(v -> updateOrderStatus("confirmed", "Xác nhận"));
        btnShipping.setOnClickListener(v -> updateOrderStatus("shipping", "Giao hàng"));
        btnComplete.setOnClickListener(v -> updateOrderStatus("completed", "Hoàn thành"));
        btnCancel.setOnClickListener(v -> showCancelDialog());
    }
    
    private void loadOrderDetail() {
        progressBar.setVisibility(View.VISIBLE);
        layoutContent.setVisibility(View.GONE);
        
        adminApi.getOrderById("Bearer " + token, orderId).enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    currentOrder = response.body();
                    displayOrderDetails(currentOrder);
                    layoutContent.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(AdminOrderDetailActivity.this, 
                            "Lỗi tải đơn hàng: " + response.code(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            
            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminOrderDetailActivity.this, 
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
    
    private void displayOrderDetails(Order order) {
        try {
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            
            tvOrderId.setText("Đơn hàng #" + (order.getId() != null ? order.getId() : "N/A"));
            
            // Chuyển status về lowercase để xử lý thống nhất
            String status = order.getStatus() != null ? order.getStatus().toLowerCase() : "pending";
            tvStatus.setText(getStatusText(status));
            tvStatus.setBackgroundResource(getStatusColor(status));
            
            // Hiển thị tên người nhận hàng (không phải tên user account)
            String receiverName = order.getReceiverName() != null ? order.getReceiverName() : "N/A";
            
            // Hiển thị thêm thông tin user account nếu cần
            String email = order.getEmail() != null ? order.getEmail() : "";
            if (!email.isEmpty()) {
                tvCustomerName.setText(receiverName + " (" + email + ")");
            } else {
                tvCustomerName.setText(receiverName);
            }
            
            tvPhone.setText(order.getPhone() != null ? order.getPhone() : "N/A");
            
            // Địa chỉ đã được format: "address, district, city"
            tvAddress.setText(order.getShippingAddress() != null ? order.getShippingAddress() : "N/A");
            
            // Hiển thị phương thức thanh toán
            String paymentMethod = order.getPaymentMethod() != null ? order.getPaymentMethod() : "N/A";
            tvPaymentMethod.setText(paymentMethod);
            
            tvCreatedAt.setText(order.getCreatedAt() != null ? order.getCreatedAt() : "N/A");
            tvQuantity.setText(String.valueOf(order.getQuantity() != null ? order.getQuantity() : 0));
            
            // Hiển thị số tiền
            Double totalAmount = order.getTotalAmount() != null ? order.getTotalAmount() : 0.0;
            Double totalDiscount = order.getTotalDiscount() != null ? order.getTotalDiscount() : 0.0;
            Double shippingFee = order.getShippingFee() != null ? order.getShippingFee() : 0.0;
            Double finalAmount = order.getFinalAmount() != null ? order.getFinalAmount() : 0.0;
            
            tvTotalAmount.setText(currencyFormat.format(totalAmount));
            tvDiscount.setText(currencyFormat.format(totalDiscount));
            tvShippingFee.setText(currencyFormat.format(shippingFee));
            tvFinalAmount.setText(currencyFormat.format(finalAmount));
            
            // Setup RecyclerView for order items
            if (order.getItems() != null && !order.getItems().isEmpty()) {
                OrderItemAdapter adapter = new OrderItemAdapter(this, order.getItems());
                recyclerViewItems.setLayoutManager(new LinearLayoutManager(this));
                recyclerViewItems.setAdapter(adapter);
            }
            
            // Display customer note if available
            if (order.getCustomerNote() != null && !order.getCustomerNote().trim().isEmpty()) {
                tvCustomerNote.setText(order.getCustomerNote());
                tvCustomerNote.setVisibility(View.VISIBLE);
                tvCustomerNoteLabel.setVisibility(View.VISIBLE);
            } else {
                tvCustomerNote.setVisibility(View.GONE);
                tvCustomerNoteLabel.setVisibility(View.GONE);
            }
            
            // Show/hide action buttons based on status
            updateActionButtons(status);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi hiển thị: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void updateActionButtons(String status) {
        btnConfirm.setVisibility(View.GONE);
        btnShipping.setVisibility(View.GONE);
        btnComplete.setVisibility(View.GONE);
        btnCancel.setVisibility(View.VISIBLE);
        
        // Chuyển về lowercase để so sánh
        String statusLower = status != null ? status.toLowerCase() : "";
        
        switch (statusLower) {
            case "pending":
                // Chờ xác nhận -> Hiển thị nút "Xác nhận"
                btnConfirm.setVisibility(View.VISIBLE);
                break;
            case "confirmed":
                // Đã xác nhận -> Hiển thị nút "Giao hàng"
                btnShipping.setVisibility(View.VISIBLE);
                break;
            case "shipping":
                // Đang giao -> Hiển thị nút "Hoàn thành"
                btnComplete.setVisibility(View.VISIBLE);
                break;
            case "completed":
            case "canceled":
                // Đã hoàn thành hoặc đã hủy -> Ẩn tất cả các nút
                btnCancel.setVisibility(View.GONE);
                layoutActions.setVisibility(View.GONE);
                break;
        }
    }
    
    private void updateOrderStatus(String newStatus, String actionName) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận " + actionName)
                .setMessage("Bạn có chắc chắn muốn " + actionName.toLowerCase() + " đơn hàng này?")
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    progressBar.setVisibility(View.VISIBLE);
                    
                    adminApi.updateOrderStatus("Bearer " + token, orderId, newStatus)
                            .enqueue(new Callback<Order>() {
                                @Override
                                public void onResponse(Call<Order> call, Response<Order> response) {
                                    progressBar.setVisibility(View.GONE);
                                    
                                    if (response.isSuccessful()) {
                                        Toast.makeText(AdminOrderDetailActivity.this, 
                                                actionName + " đơn hàng thành công", Toast.LENGTH_SHORT).show();
                                        loadOrderDetail();
                                    } else {
                                        Toast.makeText(AdminOrderDetailActivity.this, 
                                                "Lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                                
                                @Override
                                public void onFailure(Call<Order> call, Throwable t) {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(AdminOrderDetailActivity.this, 
                                            "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
    
    private void showCancelDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Hủy đơn hàng")
                .setMessage("Bạn có chắc chắn muốn hủy đơn hàng này?")
                .setPositiveButton("Hủy đơn", (dialog, which) -> updateOrderStatus("canceled", "Hủy"))
                .setNegativeButton("Quay lại", null)
                .show();
    }
    
    private String getStatusText(String status) {
        String statusLower = status != null ? status.toLowerCase() : "";
        switch (statusLower) {
            case "pending": return "Chờ xác nhận";
            case "confirmed": return "Đã xác nhận";
            case "shipping": return "Đang giao";
            case "completed": return "Hoàn tất";
            case "canceled": return "Đã hủy";
            default: return status;
        }
    }
    
    private int getStatusColor(String status) {
        String statusLower = status != null ? status.toLowerCase() : "";
        switch (statusLower) {
            case "pending": return R.drawable.status_pending;
            case "confirmed": return R.drawable.status_confirmed;
            case "shipping": return R.drawable.status_shipping;
            case "completed": return R.drawable.status_completed;
            case "canceled": return R.drawable.status_canceled;
            default: return R.drawable.status_pending;
        }
    }
}
