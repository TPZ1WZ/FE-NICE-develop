package com.example.nike_fe.ui.admin;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nike_fe.R;
import com.example.nike_fe.data.api.AdminCouponApi;
import com.example.nike_fe.data.api.RetrofitClient;
import com.example.nike_fe.data.model.Coupon;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminCouponFormActivity extends AppCompatActivity {

    private static final String TAG = "AdminCouponForm";

    private ImageView ivBack;
    private TextView tvTitle;
    private TextInputEditText etCode, etDescription, etDiscountValue, etMinOrderValue, etUsageLimit;
    private RadioGroup rgDiscountType;
    private TextView tvStartDate, tvEndDate;
    private SwitchMaterial switchActive;
    private Button btnSave;
    private FrameLayout layoutLoading;

    private AdminCouponApi adminCouponApi;
    private String token;
    private Long couponId = -1L;
    private Coupon currentCoupon;

    // Format for server: YYYY-MM-DD
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_coupon_form); // Correction: used existing XML name

        initViews();
        checkIntent();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        tvTitle = findViewById(R.id.tvTitle);
        etCode = findViewById(R.id.etCode);
        etDescription = findViewById(R.id.etDescription);
        etDiscountValue = findViewById(R.id.etDiscountValue);
        etMinOrderValue = findViewById(R.id.etMinOrderValue);
        etUsageLimit = findViewById(R.id.etUsageLimit);
        rgDiscountType = findViewById(R.id.rgDiscountType);
        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);
        switchActive = findViewById(R.id.switchActive);
        btnSave = findViewById(R.id.btnSave);
        layoutLoading = findViewById(R.id.layoutLoading);

        RetrofitClient retrofitClient = RetrofitClient.getInstance(this);
        adminCouponApi = retrofitClient.getAdminCouponApi();
        token = retrofitClient.getToken();

        ivBack.setOnClickListener(v -> finish());

        tvStartDate.setOnClickListener(v -> showDatePicker(tvStartDate));
        tvEndDate.setOnClickListener(v -> showDatePicker(tvEndDate));

        btnSave.setOnClickListener(v -> saveCoupon());
    }

    private void checkIntent() {
        couponId = getIntent().getLongExtra("coupon_id", -1);
        if (couponId != -1) {
            tvTitle.setText("Cập Nhật Coupon");
            loadCouponDetails();
        } else {
            tvTitle.setText("Thêm Mã Giảm Giá");
            // Set default dates (Today and +30 days)
            tvStartDate.setText(dateFormat.format(calendar.getTime()));
            Calendar endCal = (Calendar) calendar.clone();
            endCal.add(Calendar.DAY_OF_YEAR, 30);
            tvEndDate.setText(dateFormat.format(endCal.getTime()));
        }
    }

    private void showDatePicker(TextView textView) {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year, month, dayOfMonth);
            textView.setText(dateFormat.format(selectedDate.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void loadCouponDetails() {
        layoutLoading.setVisibility(View.VISIBLE);
        adminCouponApi.getCouponById("Bearer " + token, couponId).enqueue(new Callback<Coupon>() {
            @Override
            public void onResponse(Call<Coupon> call, Response<Coupon> response) {
                layoutLoading.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    currentCoupon = response.body();
                    populateForm(currentCoupon);
                } else {
                    Toast.makeText(AdminCouponFormActivity.this, "Lỗi tải thông tin: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Coupon> call, Throwable t) {
                layoutLoading.setVisibility(View.GONE);
                Toast.makeText(AdminCouponFormActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void populateForm(Coupon coupon) {
        etCode.setText(coupon.getCode());
        etDescription.setText(coupon.getDescription());
        etDiscountValue.setText(String.valueOf(coupon.getDiscountValue()));
        etMinOrderValue.setText(String.valueOf(coupon.getMinOrderValue()));
        etUsageLimit.setText(String.valueOf(coupon.getUsageLimit()));

        if ("FIXED_AMOUNT".equals(coupon.getDiscountType()) || "AMOUNT".equals(coupon.getDiscountType())) {
            rgDiscountType.check(R.id.rbAmount);
        } else {
            rgDiscountType.check(R.id.rbPercent);
        }

        tvStartDate.setText(coupon.getStartDate());
        tvEndDate.setText(coupon.getEndDate());
        switchActive.setChecked(Boolean.TRUE.equals(coupon.getIsActive()));

        // Disable Code editing in Update mode (usually unique)
        etCode.setEnabled(false);
    }

    private void saveCoupon() {
        String code = etCode.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String discountValueStr = etDiscountValue.getText().toString().trim();
        String minOrderStr = etMinOrderValue.getText().toString().trim();
        String usageLimitStr = etUsageLimit.getText().toString().trim();
        String startDate = tvStartDate.getText().toString();
        String endDate = tvEndDate.getText().toString();

        if (code.isEmpty() || discountValueStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập Mã và Giá trị giảm", Toast.LENGTH_SHORT).show();
            return;
        }

        Coupon coupon = new Coupon();
        if (currentCoupon != null)
            coupon.setId(currentCoupon.getId());

        coupon.setCode(code.toUpperCase());
        coupon.setDescription(description);
        coupon.setDiscountType(
                rgDiscountType.getCheckedRadioButtonId() == R.id.rbPercent ? "PERCENTAGE" : "FIXED_AMOUNT");

        try {
            coupon.setDiscountValue(Double.parseDouble(discountValueStr));
            coupon.setMinOrderValue(minOrderStr.isEmpty() ? 0.0 : Double.parseDouble(minOrderStr));
            coupon.setUsageLimit(usageLimitStr.isEmpty() ? 0 : Integer.parseInt(usageLimitStr));
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số liệu không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        coupon.setStartDate(startDate);
        coupon.setEndDate(endDate);
        coupon.setIsActive(switchActive.isChecked());

        layoutLoading.setVisibility(View.VISIBLE);

        if (couponId == -1) {
            createCoupon(coupon);
        } else {
            updateCoupon(couponId, coupon);
        }
    }

    private void createCoupon(Coupon coupon) {
        adminCouponApi.createCoupon("Bearer " + token, coupon).enqueue(new Callback<Coupon>() {
            @Override
            public void onResponse(Call<Coupon> call, Response<Coupon> response) {
                layoutLoading.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(AdminCouponFormActivity.this, "Tạo coupon thành công", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AdminCouponFormActivity.this, "Lỗi tạo: " + response.code(), Toast.LENGTH_SHORT)
                            .show();
                }
            }

            @Override
            public void onFailure(Call<Coupon> call, Throwable t) {
                layoutLoading.setVisibility(View.GONE);
                Toast.makeText(AdminCouponFormActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCoupon(Long id, Coupon coupon) {
        adminCouponApi.updateCoupon("Bearer " + token, id, coupon).enqueue(new Callback<Coupon>() {
            @Override
            public void onResponse(Call<Coupon> call, Response<Coupon> response) {
                layoutLoading.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(AdminCouponFormActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AdminCouponFormActivity.this, "Lỗi cập nhật: " + response.code(), Toast.LENGTH_SHORT)
                            .show();
                }
            }

            @Override
            public void onFailure(Call<Coupon> call, Throwable t) {
                layoutLoading.setVisibility(View.GONE);
                Toast.makeText(AdminCouponFormActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
