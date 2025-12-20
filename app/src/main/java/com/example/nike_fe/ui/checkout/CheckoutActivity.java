package com.example.nike_fe.ui.checkout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import com.example.nike_fe.R;
import com.example.nike_fe.data.api.OrderApi;
import com.example.nike_fe.data.api.RetrofitClient;
import com.example.nike_fe.data.model.OrderRequest;
import com.example.nike_fe.data.model.OrderResponse;
import com.example.nike_fe.ui.auth.LoginActivity;
import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutActivity extends AppCompatActivity {
    
    private ImageView ivBack;
    private TextInputEditText etFullName, etPhone, etAddress, etCity, etDistrict, etNote;
    private RadioGroup rgPaymentMethod;
    private RadioButton rbCOD, rbVNPay;
    private TextView tvSubtotal, tvShippingFee, tvTotal;
    private Button btnConfirmOrder;
    private FrameLayout layoutLoading;
    private NestedScrollView layoutCheckoutContent;
    
    private OrderApi orderApi;
    private String token;
    
    private double subtotal = 0.0;
    private static final double SHIPPING_FEE = 30000.0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        
        initViews();
        setupToolbar();
        loadCartData();
        setupPaymentMethod();
        setupConfirmButton();
    }
    
    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        etFullName = findViewById(R.id.etFullName);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        etCity = findViewById(R.id.etCity);
        etDistrict = findViewById(R.id.etDistrict);
        etNote = findViewById(R.id.etNote);
        rgPaymentMethod = findViewById(R.id.rgPaymentMethod);
        rbCOD = findViewById(R.id.rbCOD);
        rbVNPay = findViewById(R.id.rbVNPay);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvShippingFee = findViewById(R.id.tvShippingFee);
        tvTotal = findViewById(R.id.tvTotal);
        btnConfirmOrder = findViewById(R.id.btnConfirmOrder);
        layoutLoading = findViewById(R.id.layoutLoading);
        layoutCheckoutContent = findViewById(R.id.layoutCheckoutContent);
        
        RetrofitClient retrofitClient = RetrofitClient.getInstance(this);
        orderApi = retrofitClient.getOrderApi();
        token = retrofitClient.getToken();
        
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return;
        }
    }
    
    private void setupToolbar() {
        ivBack.setOnClickListener(v -> onBackPressed());
    }
    
    private void loadCartData() {
        // Get subtotal from Intent
        subtotal = getIntent().getDoubleExtra("subtotal", 0.0);
        updatePriceSummary();
    }
    
    private void updatePriceSummary() {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        
        tvSubtotal.setText(formatter.format(subtotal) + " ₫");
        tvShippingFee.setText(formatter.format(SHIPPING_FEE) + " ₫");
        tvTotal.setText(formatter.format(subtotal + SHIPPING_FEE) + " ₫");
    }
    
    private void setupPaymentMethod() {
        // Default is COD
        rbCOD.setChecked(true);
    }
    
    private void setupConfirmButton() {
        btnConfirmOrder.setOnClickListener(v -> validateAndPlaceOrder());
    }
    
    private void validateAndPlaceOrder() {
        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        
        // Validation
        if (fullName.isEmpty()) {
            etFullName.setError("Vui lòng nhập họ tên");
            etFullName.requestFocus();
            return;
        }
        
        if (phone.isEmpty()) {
            etPhone.setError("Vui lòng nhập số điện thoại");
            etPhone.requestFocus();
            return;
        }
        
        if (!phone.matches("^0[0-9]{9}$")) {
            etPhone.setError("Số điện thoại không hợp lệ");
            etPhone.requestFocus();
            return;
        }
        
        if (address.isEmpty()) {
            etAddress.setError("Vui lòng nhập địa chỉ");
            etAddress.requestFocus();
            return;
        }
        
        if (city.isEmpty()) {
            etCity.setError("Vui lòng nhập thành phố");
            etCity.requestFocus();
            return;
        }
        
        // Get district
        String district = etDistrict.getText().toString().trim();
        
        // Build shipping address: "address, district, city"
        StringBuilder addressBuilder = new StringBuilder(address);
        if (!district.isEmpty()) {
            addressBuilder.append(", ").append(district);
        }
        addressBuilder.append(", ").append(city);
        String shippingAddress = addressBuilder.toString();
        
        // Get payment method
        String paymentMethod = rbCOD.isChecked() ? "COD" : "VNPAY";
        
        // Place order with receiver name
        placeOrder(fullName, shippingAddress, paymentMethod, phone);
    }
    
    private void placeOrder(String receiverName, String shippingAddress, String paymentMethod, String phone) {
        showLoading(true);
        
        OrderRequest request = new OrderRequest(receiverName, shippingAddress, paymentMethod, phone, null);
        
        orderApi.createOrder("Bearer " + token, request).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    OrderResponse orderResponse = response.body();
                    
                    if (orderResponse.requiresPayment()) {
                        // VNPay payment - redirect to payment URL
                        String paymentUrl = orderResponse.getPaymentUrl();
                        if (paymentUrl != null && !paymentUrl.isEmpty()) {
                            // TODO: Open WebView or external browser for payment
                            Toast.makeText(CheckoutActivity.this, 
                                "Chuyển đến trang thanh toán VNPay", Toast.LENGTH_SHORT).show();
                            // For now, just show success
                            showOrderSuccess();
                        } else {
                            Toast.makeText(CheckoutActivity.this, 
                                "Không thể lấy link thanh toán", Toast.LENGTH_SHORT).show();
                        }
                    } else if (orderResponse.isSuccess()) {
                        // COD order success
                        showOrderSuccess();
                    } else {
                        Toast.makeText(CheckoutActivity.this, 
                            orderResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CheckoutActivity.this, 
                        "Đặt hàng thất bại", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<OrderResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(CheckoutActivity.this, 
                    "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showOrderSuccess() {
        Toast.makeText(this, "Đặt hàng thành công!", Toast.LENGTH_LONG).show();
        
        // Navigate to Order History to see order waiting for confirmation
        Intent intent = new Intent(this, com.example.nike_fe.ui.order.OrderHistoryActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
    
    private void showLoading(boolean show) {
        layoutLoading.setVisibility(show ? View.VISIBLE : View.GONE);
        layoutCheckoutContent.setVisibility(show ? View.GONE : View.VISIBLE);
        btnConfirmOrder.setEnabled(!show);
    }
    
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
