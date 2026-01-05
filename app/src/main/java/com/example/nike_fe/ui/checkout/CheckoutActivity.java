package com.example.nike_fe.ui.checkout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nike_fe.R;
import com.example.nike_fe.data.api.AddressApi;
import com.example.nike_fe.data.api.OrderApi;
import com.example.nike_fe.data.api.RetrofitClient;
import com.example.nike_fe.data.api.UserCouponApi;
import com.example.nike_fe.data.api.LoyaltyApi;
import com.example.nike_fe.data.model.Address;
import com.example.nike_fe.data.model.Coupon;
import com.example.nike_fe.data.model.OrderRequest;
import com.example.nike_fe.data.model.OrderResponse;
import com.example.nike_fe.data.model.LoyaltyPointsResponse;
import com.example.nike_fe.ui.address.SelectAddressActivity;
import com.example.nike_fe.ui.auth.LoginActivity;
import com.example.nike_fe.ui.checkout.adapter.CouponSelectionAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutActivity extends AppCompatActivity {

    private ImageView ivBack;
    private LinearLayout cvAddressCard;
    private LinearLayout layoutAddressContent, layoutAddressEmpty;
    private TextView tvSelectedRecipientName, tvSelectedPhoneNumber, tvSelectedAddress;
    private TextInputEditText etNote;
    private RadioGroup rgPaymentMethod;
    private RadioButton rbCOD, rbVNPay;
    private TextView tvSubtotal, tvDiscount, tvShippingFee, tvTotal;
    private Button btnConfirmOrder;
    private FrameLayout layoutLoading;
    private NestedScrollView layoutCheckoutContent;

    // Nike Coin views
    private androidx.appcompat.widget.SwitchCompat switchNikeCoin;
    private TextView tvNikeCoinBalance;
    private TextView tvNikeCoinMessage;
    private TextView tvNikeCoinDiscount;
    private LinearLayout layoutNikeCoinDiscount;

    private OrderApi orderApi;
    private AddressApi addressApi;
    private LoyaltyApi loyaltyApi;
    private String token;
    
    // Address data
    private Long selectedAddressId;
    private String recipientName;
    private String phoneNumber;
    private String fullAddress;

    private double subtotal = 0.0;
    private static final double SHIPPING_FEE = 30000.0;
    private static final int REQUEST_SELECT_ADDRESS = 100;

    // Nike Coin data
    private int userNikeCoinBalance = 0;
    private int nikeCoinUsed = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        initViews();
        setupToolbar();
        loadCartData();
        loadNikeCoinBalance();
        setupNikeCoinSwitch();
        setupPaymentMethod();
        setupConfirmButton();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        
        // Address card views
        cvAddressCard = findViewById(R.id.cvAddressCard);
        layoutAddressContent = findViewById(R.id.layoutAddressContent);
        layoutAddressEmpty = findViewById(R.id.layoutAddressEmpty);
        tvSelectedRecipientName = findViewById(R.id.tvSelectedRecipientName);
        tvSelectedPhoneNumber = findViewById(R.id.tvSelectedPhoneNumber);
        tvSelectedAddress = findViewById(R.id.tvSelectedAddress);
        
        etNote = findViewById(R.id.etNote);
        rgPaymentMethod = findViewById(R.id.rgPaymentMethod);
        rbCOD = findViewById(R.id.rbCOD);
        rbVNPay = findViewById(R.id.rbVNPay);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvDiscount = findViewById(R.id.tvDiscount);
        tvShippingFee = findViewById(R.id.tvShippingFee);
        tvTotal = findViewById(R.id.tvTotal);
        btnConfirmOrder = findViewById(R.id.btnConfirmOrder);
        layoutLoading = findViewById(R.id.layoutLoading);
        layoutCheckoutContent = findViewById(R.id.layoutCheckoutContent);

        // Coupon views
        etCouponCode = findViewById(R.id.etCouponCode);
        btnApplyCoupon = findViewById(R.id.btnApplyCoupon);
        tvCouponMessage = findViewById(R.id.tvCouponMessage);

        // Nike Coin views
        switchNikeCoin = findViewById(R.id.switchNikeCoin);
        tvNikeCoinBalance = findViewById(R.id.tvNikeCoinBalance);
        tvNikeCoinMessage = findViewById(R.id.tvNikeCoinMessage);
        tvNikeCoinDiscount = findViewById(R.id.tvNikeCoinDiscount);
        layoutNikeCoinDiscount = findViewById(R.id.layoutNikeCoinDiscount);

        RetrofitClient retrofitClient = RetrofitClient.getInstance(this);
        orderApi = retrofitClient.getOrderApi();
        addressApi = retrofitClient.getAddressApi();
        userCouponApi = retrofitClient.getUserCouponApi();
        loyaltyApi = retrofitClient.getLoyaltyApi();
        token = retrofitClient.getToken();

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return;
        }

        // Setup address card click
        cvAddressCard.setOnClickListener(v -> openSelectAddressScreen());

        loadValidCoupons();
        loadDefaultAddress();
    }

    private void loadValidCoupons() {
        userCouponApi.getValidCoupons("Bearer " + token).enqueue(new Callback<List<Coupon>>() {
            @Override
            public void onResponse(Call<List<Coupon>> call, Response<List<Coupon>> response) {
                android.util.Log.d("CheckoutActivity", "Coupons API response: " + response.code());
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    List<Coupon> coupons = response.body();
                    android.util.Log.d("CheckoutActivity", "Found " + coupons.size() + " valid coupons");
                    tvCouponMessage.setVisibility(View.VISIBLE);
                    tvCouponMessage.setText("Bạn có " + coupons.size() + " mã giảm giá khả dụng. Nhấn để xem.");
                    tvCouponMessage.setTextColor(getResources().getColor(android.R.color.holo_purple));

                    tvCouponMessage.setOnClickListener(v -> showCouponSelectionDialog(coupons));

                    // Also auto-show dialog if no coupon applied yet?
                    // Maybe just showing the message is enough "Auto-display suggestion"
                } else {
                    android.util.Log.d("CheckoutActivity", "No valid coupons found");
                }
            }

            @Override
            public void onFailure(Call<List<Coupon>> call, Throwable t) {
                android.util.Log.e("CheckoutActivity", "Failed to load coupons: " + t.getMessage());
                // Ignore
            }
        });
    }

    private void showCouponSelectionDialog(List<Coupon> coupons) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setDismissWithAnimation(true);
        
        // Inflate layout từ file XML
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_coupon_selection, null);
        
        // Tìm các views
        RecyclerView rvCoupons = bottomSheetView.findViewById(R.id.rvCoupons);
        ImageView ivCloseSheet = bottomSheetView.findViewById(R.id.ivCloseSheet);
        View layoutEmptyState = bottomSheetView.findViewById(R.id.layoutEmptyState);
        
        // Đóng bottom sheet khi click nút X
        ivCloseSheet.setOnClickListener(v -> dialog.dismiss());
        
        // Setup RecyclerView
        if (coupons != null && !coupons.isEmpty()) {
            rvCoupons.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
            
            rvCoupons.setLayoutManager(new LinearLayoutManager(this));
            CouponSelectionAdapter adapter = new CouponSelectionAdapter(coupons, subtotal, coupon -> {
                etCouponCode.setText(coupon.getCode());
                validateAndApplyCoupon(coupon);
                dialog.dismiss();
            });
            
            // Set selected coupon nếu đã có
            if (currentCouponCode != null) {
                adapter.setSelectedCouponCode(currentCouponCode);
            }
            
            rvCoupons.setAdapter(adapter);
        } else {
            rvCoupons.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
        }
        
        dialog.setContentView(bottomSheetView);
        dialog.show();
    }

    private void setupToolbar() {
        ivBack.setOnClickListener(v -> onBackPressed());
    }

    private void loadDefaultAddress() {
        addressApi.getDefaultAddress("Bearer " + token).enqueue(new Callback<Address>() {
            @Override
            public void onResponse(Call<Address> call, Response<Address> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Address address = response.body();
                    updateAddressDisplay(address);
                } else {
                    showEmptyAddressState();
                }
            }

            @Override
            public void onFailure(Call<Address> call, Throwable t) {
                // Show empty state on error
                showEmptyAddressState();
            }
        });
    }

    private void updateAddressDisplay(Address address) {
        selectedAddressId = address.getId();
        recipientName = address.getRecipientName();
        phoneNumber = address.getPhoneNumber();
        fullAddress = address.getFullAddress();

        layoutAddressContent.setVisibility(View.VISIBLE);
        layoutAddressEmpty.setVisibility(View.GONE);

        tvSelectedRecipientName.setText(recipientName);
        tvSelectedPhoneNumber.setText(phoneNumber);
        tvSelectedAddress.setText(fullAddress);
    }

    private void showEmptyAddressState() {
        layoutAddressContent.setVisibility(View.GONE);
        layoutAddressEmpty.setVisibility(View.VISIBLE);
        
        selectedAddressId = null;
        recipientName = null;
        phoneNumber = null;
        fullAddress = null;
    }

    private void openSelectAddressScreen() {
        Intent intent = new Intent(this, SelectAddressActivity.class);
        startActivityForResult(intent, REQUEST_SELECT_ADDRESS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_SELECT_ADDRESS && resultCode == RESULT_OK && data != null) {
            // Update address display with selected address
            selectedAddressId = data.getLongExtra("selected_address_id", -1);
            if (selectedAddressId == -1) selectedAddressId = null;
            
            recipientName = data.getStringExtra("recipient_name");
            phoneNumber = data.getStringExtra("phone_number");
            fullAddress = data.getStringExtra("full_address");

            if (recipientName != null && phoneNumber != null && fullAddress != null) {
                layoutAddressContent.setVisibility(View.VISIBLE);
                layoutAddressEmpty.setVisibility(View.GONE);

                tvSelectedRecipientName.setText(recipientName);
                tvSelectedPhoneNumber.setText(phoneNumber);
                tvSelectedAddress.setText(fullAddress);
            }
        }
    }

    private void loadCartData() {
        // Get subtotal from Intent
        subtotal = getIntent().getDoubleExtra("subtotal", 0.0);
        updatePriceSummary();
    }

    private void setupPaymentMethod() {
        // Default is COD
        rbCOD.setChecked(true);
    }

    private void setupConfirmButton() {
        btnConfirmOrder.setOnClickListener(v -> validateAndPlaceOrder());
        btnApplyCoupon.setOnClickListener(v -> applyCoupon());
    }

    private void applyCoupon() {
        String code = etCouponCode.getText().toString().trim().toUpperCase();
        if (code.isEmpty()) {
            etCouponCode.setError("Vui lòng nhập mã");
            return;
        }

        showLoading(true);
        userCouponApi.getCouponByCode("Bearer " + token, code).enqueue(new Callback<Coupon>() {
            @Override
            public void onResponse(Call<Coupon> call, Response<Coupon> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    Coupon coupon = response.body();
                    validateAndApplyCoupon(coupon);
                } else {
                    showCouponError("Mã giảm giá không tồn tại hoặc đã hết hạn");
                }
            }

            @Override
            public void onFailure(Call<Coupon> call, Throwable t) {
                showLoading(false);
                showCouponError("Lỗi kiểm tra mã: " + t.getMessage());
            }
        });
    }

    private void validateAndApplyCoupon(Coupon coupon) {
        // Client-side validation (optional but good for UX)
        if (coupon.getIsActive() != null && !coupon.getIsActive()) {
            showCouponError("Mã giảm giá đang bị khóa");
            return;
        }

        // Check min order value
        if (coupon.getMinOrderValue() != null && subtotal < coupon.getMinOrderValue()) {
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            showCouponError("Đơn hàng tối thiểu phải từ " + currencyFormat.format(coupon.getMinOrderValue()));
            return;
        }

        // Check dates
        // (Simplified check, backend does usually verifies this too)

        // Calculate discount
        double discountAmount = 0;
        Double discountVal = coupon.getDiscountValue();
        if (discountVal == null)
            discountVal = 0.0;

        if ("PERCENTAGE".equals(coupon.getDiscountType()) || "PERCENT".equals(coupon.getDiscountType())) {
            discountAmount = subtotal * (discountVal / 100.0);
            if (coupon.getMaxDiscountAmount() != null && coupon.getMaxDiscountAmount() > 0) {
                discountAmount = Math.min(discountAmount, coupon.getMaxDiscountAmount());
            }
        } else {
            discountAmount = discountVal;
        }

        // Ensure discount doesn't exceed subtotal
        currentDiscountAmount = Math.min(discountAmount, subtotal);
        currentCouponCode = coupon.getCode();

        showCouponSuccess("Áp dụng mã thành công: -" + formatCurrency(currentDiscountAmount));
        updatePriceSummary();
    }

    private void showCouponError(String message) {
        tvCouponMessage.setVisibility(View.VISIBLE);
        tvCouponMessage.setText(message);
        tvCouponMessage.setTextColor(getResources().getColor(android.R.color.holo_red_dark));

        // Reset current coupon
        currentCouponCode = null;
        currentDiscountAmount = 0.0;
        updatePriceSummary();
    }

    private void showCouponSuccess(String message) {
        tvCouponMessage.setVisibility(View.VISIBLE);
        tvCouponMessage.setText(message);
        tvCouponMessage.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
    }

    private String formatCurrency(double amount) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(amount) + " ₫";
    }

    // Update Init Declarations
    private TextInputEditText etCouponCode;
    private Button btnApplyCoupon;
    private TextView tvCouponMessage;
    private UserCouponApi userCouponApi;
    private String currentCouponCode = null;
    private double currentDiscountAmount = 0.0;

    // ... existing methods ...

    private void updatePriceSummary() {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));

        tvSubtotal.setText(formatter.format(subtotal) + " ₫");
        
        // Show discount with minus sign and orange color when applied
        if (currentDiscountAmount > 0) {
            tvDiscount.setText("-" + formatter.format(currentDiscountAmount) + " ₫");
            tvDiscount.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        } else {
            tvDiscount.setText("0 ₫");
            tvDiscount.setTextColor(getResources().getColor(R.color.black));
        }
        
        tvShippingFee.setText(formatter.format(SHIPPING_FEE) + " ₫");

        // Show Nike Coin discount if used
        if (nikeCoinUsed > 0) {
            layoutNikeCoinDiscount.setVisibility(View.VISIBLE);
            tvNikeCoinDiscount.setText("-" + formatter.format(nikeCoinUsed) + " ₫");
        } else {
            layoutNikeCoinDiscount.setVisibility(View.GONE);
        }

        // Calculate final total: subtotal + shipping - coupon discount - nike coin
        double finalTotal = subtotal + SHIPPING_FEE - currentDiscountAmount - nikeCoinUsed;
        if (finalTotal < 0)
            finalTotal = 0;

        tvTotal.setText(formatter.format(finalTotal) + " ₫");
    }

    // Modify ValidateAndPlaceOrder to use currentCouponCode
    private void validateAndPlaceOrder() {
        // Validate address is selected
        if (recipientName == null || phoneNumber == null || fullAddress == null) {
            Toast.makeText(this, "Vui lòng chọn địa chỉ giao hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get note
        String note = etNote != null ? etNote.getText().toString().trim() : "";

        // Get payment method
        String paymentMethod = rbCOD.isChecked() ? "COD" : "VNPAY";

        // Place order with address info
        placeOrder(recipientName, fullAddress, paymentMethod, phoneNumber, note);
    }

    private void placeOrder(String receiverName, String shippingAddress, String paymentMethod, String phone, String customerNote) {
        showLoading(true);

        OrderRequest request = new OrderRequest(receiverName, shippingAddress, paymentMethod, phone, currentCouponCode);
        request.setCustomerNote(customerNote);
        request.setNikeCoinUsed(nikeCoinUsed);

        orderApi.createOrder("Bearer " + token, request).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    OrderResponse orderResponse = response.body();

                    if (orderResponse.requiresPayment()) {
                        String paymentUrl = orderResponse.getPaymentUrl();
                        if (paymentUrl != null && !paymentUrl.isEmpty()) {
                            // Mở VNPay trong WebView Activity
                            Toast.makeText(CheckoutActivity.this, "Chuyển đến trang thanh toán VNPay", Toast.LENGTH_SHORT).show();
                            android.content.Intent intent = new android.content.Intent(CheckoutActivity.this, com.example.nike_fe.ui.payment.VNPayActivity.class);
                            intent.putExtra("PAYMENT_URL", paymentUrl);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(CheckoutActivity.this, "Không thể lấy link thanh toán", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    } else if (orderResponse.isSuccess()) {
                        showOrderSuccess();
                    } else {
                        Toast.makeText(CheckoutActivity.this, orderResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CheckoutActivity.this, "Đặt hàng thất bại: " + response.code(), Toast.LENGTH_SHORT)
                            .show();
                }
            }

            @Override
            public void onFailure(Call<OrderResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(CheckoutActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showOrderSuccess() {
        // Create dialog from custom layout
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_order_success);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(false);
        
        // Get buttons from dialog layout
        Button btnViewOrder = dialog.findViewById(R.id.btn_view_order);
        Button btnHome = dialog.findViewById(R.id.btn_home);
        
        // Set click listeners
        btnViewOrder.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(CheckoutActivity.this, com.example.nike_fe.ui.order.OrderHistoryActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
        
        btnHome.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(CheckoutActivity.this, com.example.nike_fe.MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("REFRESH_BADGE", true);
            startActivity(intent);
            finish();
        });
        
        dialog.show();
    }

    private void showLoading(boolean show) {
        layoutLoading.setVisibility(show ? View.VISIBLE : View.GONE);
        layoutCheckoutContent.setVisibility(show ? View.GONE : View.VISIBLE);
        btnConfirmOrder.setEnabled(!show);
        btnApplyCoupon.setEnabled(!show); // Disable Apply button too
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Nike Coin Methods
    private void loadNikeCoinBalance() {
        if (loyaltyApi == null || token == null) return;

        loyaltyApi.getLoyaltyPoints("Bearer " + token).enqueue(new Callback<LoyaltyPointsResponse>() {
            @Override
            public void onResponse(Call<LoyaltyPointsResponse> call, Response<LoyaltyPointsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    userNikeCoinBalance = response.body().getCurrentPoints();
                    NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
                    tvNikeCoinBalance.setText("Số dư: " + formatter.format(userNikeCoinBalance) + " Nike Coin");
                    
                    // Disable switch if no balance
                    if (userNikeCoinBalance <= 0) {
                        switchNikeCoin.setEnabled(false);
                        tvNikeCoinMessage.setVisibility(View.VISIBLE);
                        tvNikeCoinMessage.setText("Bạn chưa có Nike Coin");
                        tvNikeCoinMessage.setTextColor(getResources().getColor(android.R.color.darker_gray));
                    }
                }
            }

            @Override
            public void onFailure(Call<LoyaltyPointsResponse> call, Throwable t) {
                // Silent fail - user can still checkout without Nike Coin
                switchNikeCoin.setEnabled(false);
            }
        });
    }

    private void setupNikeCoinSwitch() {
        switchNikeCoin.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Calculate max Nike Coin can use (cannot exceed total bill)
                double currentTotal = subtotal + SHIPPING_FEE - currentDiscountAmount;
                int maxUsable = (int) Math.min(userNikeCoinBalance, currentTotal);
                
                if (maxUsable <= 0) {
                    switchNikeCoin.setChecked(false);
                    Toast.makeText(this, "Không thể sử dụng Nike Coin cho đơn hàng này", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                nikeCoinUsed = maxUsable;
                NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
                tvNikeCoinMessage.setVisibility(View.VISIBLE);
                tvNikeCoinMessage.setText("Sử dụng " + formatter.format(nikeCoinUsed) + " Nike Coin (1 Coin = 1 ₫)");
                tvNikeCoinMessage.setTextColor(getResources().getColor(R.color.black));
            } else {
                nikeCoinUsed = 0;
                tvNikeCoinMessage.setVisibility(View.GONE);
            }
            
            updatePriceSummary();
        });
    }
}
