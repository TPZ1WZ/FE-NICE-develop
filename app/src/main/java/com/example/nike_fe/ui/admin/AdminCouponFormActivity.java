package com.example.nike_fe.ui.admin;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    private TextInputEditText etCode, etDescription;
    private TextInputEditText etPercent, etMaxDiscount, etAmount;
    private TextInputEditText etMinOrderValue, etUsageLimit;
    private TextInputEditText etStartDate, etEndDate;
    private RadioGroup rgDiscountType;
    private LinearLayout layoutPercentFields, layoutAmountFields;
    private SwitchMaterial switchActive;
    private Button btnSave, btnCancel;
    private FrameLayout layoutLoading;

    private AdminCouponApi adminCouponApi;
    private String token;
    private Long couponId = -1L;
    private Coupon currentCoupon;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_coupon_form);

        initViews();
        setupListeners();
        checkIntent();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        tvTitle = findViewById(R.id.tvTitle);
        etCode = findViewById(R.id.etCode);
        etDescription = findViewById(R.id.etDescription);

        // Discount fields
        etPercent = findViewById(R.id.etPercent);
        etMaxDiscount = findViewById(R.id.etMaxDiscount);
        etAmount = findViewById(R.id.etAmount);
        layoutPercentFields = findViewById(R.id.layoutPercentFields);
        layoutAmountFields = findViewById(R.id.layoutAmountFields);
        rgDiscountType = findViewById(R.id.rgDiscountType);

        etMinOrderValue = findViewById(R.id.etMinOrderValue);
        etUsageLimit = findViewById(R.id.etUsageLimit);

        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);

        switchActive = findViewById(R.id.switchActive);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        layoutLoading = findViewById(R.id.layoutLoading);

        RetrofitClient retrofitClient = RetrofitClient.getInstance(this);
        adminCouponApi = retrofitClient.getAdminCouponApi();
        token = retrofitClient.getToken();
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());
        btnCancel.setOnClickListener(v -> finish());

        etStartDate.setOnClickListener(v -> showDatePicker(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePicker(etEndDate));

        rgDiscountType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbPercent) {
                layoutPercentFields.setVisibility(View.VISIBLE);
                layoutAmountFields.setVisibility(View.GONE);
            } else {
                layoutPercentFields.setVisibility(View.GONE);
                layoutAmountFields.setVisibility(View.VISIBLE);
            }
        });

        btnSave.setOnClickListener(v -> saveCoupon());
    }

    private void checkIntent() {
        couponId = getIntent().getLongExtra("coupon_id", -1);
        if (couponId != -1) {
            tvTitle.setText("Cập nhật coupon");
            btnSave.setText("Lưu thay đổi");
            loadCouponDetails();
        } else {
            tvTitle.setText("Thêm mã giảm giá");
            btnSave.setText("Tạo mới");

            // Set default dates
            etStartDate.setText(dateFormat.format(calendar.getTime()));
            Calendar endCal = (Calendar) calendar.clone();
            endCal.add(Calendar.DAY_OF_YEAR, 30);
            etEndDate.setText(dateFormat.format(endCal.getTime()));
        }
    }

    private void showDatePicker(TextInputEditText editText) {
        // Parse current date from field if possible
        Calendar dateCal = Calendar.getInstance();
        try {
            String dateStr = editText.getText().toString();
            if (!dateStr.isEmpty()) {
                dateCal.setTime(dateFormat.parse(dateStr));
            }
        } catch (Exception e) {
            // ignore
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, 
            android.R.style.Theme_DeviceDefault_Light_Dialog,
            (view, year, month, dayOfMonth) -> {
                Calendar selectedDate = Calendar.getInstance();
                selectedDate.set(year, month, dayOfMonth);
                editText.setText(dateFormat.format(selectedDate.getTime()));
            }, dateCal.get(Calendar.YEAR), dateCal.get(Calendar.MONTH), dateCal.get(Calendar.DAY_OF_MONTH));
        
        datePickerDialog.setOnShowListener(dialog -> {
            datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
            datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        });
        datePickerDialog.show();
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

        // Populate inputs based on type
        if ("FIXED_AMOUNT".equals(coupon.getDiscountType()) || "AMOUNT".equals(coupon.getDiscountType())) {
            rgDiscountType.check(R.id.rbAmount);
            // Integer cast for cleaner display if whole number, else double
            double val = coupon.getDiscountValue();
            if (val == (long) val) {
                etAmount.setText(String.format(Locale.US, "%d", (long) val));
            } else {
                etAmount.setText(String.valueOf(val));
            }
        } else {
            rgDiscountType.check(R.id.rbPercent);

            double val = coupon.getDiscountValue();
            if (val == (long) val) {
                etPercent.setText(String.format(Locale.US, "%d", (long) val));
            } else {
                etPercent.setText(String.valueOf(val));
            }

            if (coupon.getMaxDiscountAmount() != null) {
                double max = coupon.getMaxDiscountAmount();
                if (max == (long) max) {
                    etMaxDiscount.setText(String.format(Locale.US, "%d", (long) max));
                } else {
                    etMaxDiscount.setText(String.valueOf(max));
                }
            }
        }

        if (coupon.getMinOrderValue() != null) {
            double min = coupon.getMinOrderValue();
            if (min == (long) min) {
                etMinOrderValue.setText(String.format(Locale.US, "%d", (long) min));
            } else {
                etMinOrderValue.setText(String.valueOf(min));
            }
        }

        if (coupon.getUsageLimit() != null) {
            etUsageLimit.setText(String.valueOf(coupon.getUsageLimit()));
        }

        etStartDate.setText(coupon.getStartDate());
        etEndDate.setText(coupon.getEndDate());
        switchActive.setChecked(Boolean.TRUE.equals(coupon.getIsActive()));

        // Disable Code editing in Update mode
        etCode.setEnabled(false);
    }

    private void saveCoupon() {
        String code = etCode.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String minOrderStr = etMinOrderValue.getText().toString().trim();
        String usageLimitStr = etUsageLimit.getText().toString().trim();
        String startDate = etStartDate.getText().toString();
        String endDate = etEndDate.getText().toString();

        boolean isPercent = rgDiscountType.getCheckedRadioButtonId() == R.id.rbPercent;

        if (code.isEmpty()) {
            etCode.setError("Vui lòng nhập mã");
            return;
        }

        // Get value based on type
        String discountValueStr;
        String maxDiscountStr = null;

        if (isPercent) {
            discountValueStr = etPercent.getText().toString().trim();
            maxDiscountStr = etMaxDiscount.getText().toString().trim();
            if (discountValueStr.isEmpty()) {
                etPercent.setError("Nhập % giảm");
                return;
            }
        } else {
            discountValueStr = etAmount.getText().toString().trim();
            if (discountValueStr.isEmpty()) {
                etAmount.setError("Nhập số tiền giảm");
                return;
            }
        }

        Coupon coupon = new Coupon();
        if (currentCoupon != null)
            coupon.setId(currentCoupon.getId());

        coupon.setCode(code); // Giữ nguyên định dạng code
        // Nếu description không rỗng, dùng nó làm name (tên hiển thị)
        // Nếu description rỗng, dùng code làm name
        coupon.setName(description.isEmpty() ? code : description);
        coupon.setDescription(description);
        coupon.setDiscountType(isPercent ? "PERCENTAGE" : "FIXED_AMOUNT");

        try {
            double val = Double.parseDouble(discountValueStr);
            if (isPercent && (val <= 0 || val > 100)) {
                etPercent.setError("% không hợp lệ (1-100)");
                return;
            }
            coupon.setDiscountValue(val);

            if (isPercent && maxDiscountStr != null && !maxDiscountStr.isEmpty()) {
                coupon.setMaxDiscountAmount(Double.parseDouble(maxDiscountStr));
            } else {
                coupon.setMaxDiscountAmount(null);
            }

            coupon.setMinOrderValue(minOrderStr.isEmpty() ? 0.0 : Double.parseDouble(minOrderStr));
            coupon.setUsageLimit(usageLimitStr.isEmpty() ? 0 : Integer.parseInt(usageLimitStr));

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số liệu nhập vào không hợp lệ", Toast.LENGTH_SHORT).show();
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
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "";
                        if (errorBody.contains("tồn tại") || errorBody.contains("exists")) {
                            Toast.makeText(AdminCouponFormActivity.this,
                                    "Mã giảm giá này đã tồn tại trong hệ thống. Vui lòng nhập mã khác.",
                                    Toast.LENGTH_LONG)
                                    .show();
                        } else {
                            Toast.makeText(AdminCouponFormActivity.this, "Lỗi tạo: " + response.code(),
                                    Toast.LENGTH_SHORT)
                                    .show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(AdminCouponFormActivity.this, "Lỗi tạo: " + response.code(), Toast.LENGTH_SHORT)
                                .show();
                    }
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
