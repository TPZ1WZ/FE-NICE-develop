package com.example.nike_fe.ui.address;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nike_fe.R;
import com.example.nike_fe.data.api.AddressApi;
import com.example.nike_fe.data.api.RetrofitClient;
import com.example.nike_fe.data.model.Address;
import com.example.nike_fe.data.model.AddressRequest;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddEditAddressActivity extends AppCompatActivity {

    private ImageView ivBack;
    private TextView tvTitle;
    private TextInputEditText etRecipientName, etPhoneNumber, etAddressLine, etWard, etDistrict, etCity;
    private CheckBox cbSetDefault;
    private Button btnSave;
    private View layoutLoading;

    private AddressApi addressApi;
    private String token;
    private Long addressId; // null for add, not null for edit
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_address);

        initViews();
        setupApi();
        loadDataFromIntent();
        setupButtons();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        tvTitle = findViewById(R.id.tvTitle);
        etRecipientName = findViewById(R.id.etRecipientName);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etAddressLine = findViewById(R.id.etAddressLine);
        etWard = findViewById(R.id.etWard);
        etDistrict = findViewById(R.id.etDistrict);
        etCity = findViewById(R.id.etCity);
        cbSetDefault = findViewById(R.id.cbSetDefault);
        btnSave = findViewById(R.id.btnSave);
        layoutLoading = findViewById(R.id.layoutLoading);
    }

    private void setupApi() {
        RetrofitClient retrofitClient = RetrofitClient.getInstance(this);
        addressApi = retrofitClient.getAddressApi();
        token = retrofitClient.getToken();

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadDataFromIntent() {
        addressId = getIntent().getLongExtra("address_id", -1);
        if (addressId == -1) {
            addressId = null;
        }

        isEditMode = addressId != null;
        tvTitle.setText(isEditMode ? "Sửa địa chỉ" : "Thêm địa chỉ mới");

        if (isEditMode) {
            // Load data from intent
            etRecipientName.setText(getIntent().getStringExtra("recipient_name"));
            etPhoneNumber.setText(getIntent().getStringExtra("phone_number"));
            etAddressLine.setText(getIntent().getStringExtra("address_line"));
            etWard.setText(getIntent().getStringExtra("ward"));
            etDistrict.setText(getIntent().getStringExtra("district"));
            etCity.setText(getIntent().getStringExtra("city"));
            cbSetDefault.setChecked(getIntent().getBooleanExtra("is_default", false));
        }
    }

    private void setupButtons() {
        ivBack.setOnClickListener(v -> onBackPressed());
        btnSave.setOnClickListener(v -> saveAddress());
    }

    private void saveAddress() {
        // Validate inputs
        String recipientName = etRecipientName.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String addressLine = etAddressLine.getText().toString().trim();
        String ward = etWard.getText().toString().trim();
        String district = etDistrict.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        boolean isDefault = cbSetDefault.isChecked();

        if (recipientName.isEmpty()) {
            etRecipientName.setError("Vui lòng nhập tên người nhận");
            etRecipientName.requestFocus();
            return;
        }

        if (phoneNumber.isEmpty()) {
            etPhoneNumber.setError("Vui lòng nhập số điện thoại");
            etPhoneNumber.requestFocus();
            return;
        }

        if (!phoneNumber.matches("^(0|\\+84)(\\s|\\.)?((3[2-9])|(5[689])|(7[06-9])|(8[1-689])|(9[0-46-9]))(\\d)(\\s|\\.)?(\\d{3})(\\s|\\.)?(\\d{3})$")) {
            etPhoneNumber.setError("Số điện thoại không hợp lệ");
            etPhoneNumber.requestFocus();
            return;
        }

        if (addressLine.isEmpty()) {
            etAddressLine.setError("Vui lòng nhập địa chỉ cụ thể");
            etAddressLine.requestFocus();
            return;
        }

        if (district.isEmpty()) {
            etDistrict.setError("Vui lòng nhập quận/huyện");
            etDistrict.requestFocus();
            return;
        }

        if (city.isEmpty()) {
            etCity.setError("Vui lòng nhập tỉnh/thành phố");
            etCity.requestFocus();
            return;
        }

        AddressRequest request = new AddressRequest(
            recipientName,
            phoneNumber,
            addressLine,
            ward,
            district,
            city,
            isDefault
        );

        if (isEditMode) {
            updateAddress(request);
        } else {
            createAddress(request);
        }
    }

    private void createAddress(AddressRequest request) {
        showLoading(true);
        
        addressApi.createAddress("Bearer " + token, request).enqueue(new Callback<Address>() {
            @Override
            public void onResponse(Call<Address> call, Response<Address> response) {
                showLoading(false);
                
                if (response.isSuccessful()) {
                    Toast.makeText(AddEditAddressActivity.this, 
                        "Đã thêm địa chỉ mới", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(AddEditAddressActivity.this, 
                        "Không thể thêm địa chỉ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Address> call, Throwable t) {
                showLoading(false);
                Toast.makeText(AddEditAddressActivity.this, 
                    "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateAddress(AddressRequest request) {
        showLoading(true);
        
        addressApi.updateAddress("Bearer " + token, addressId, request).enqueue(new Callback<Address>() {
            @Override
            public void onResponse(Call<Address> call, Response<Address> response) {
                showLoading(false);
                
                if (response.isSuccessful()) {
                    Toast.makeText(AddEditAddressActivity.this, 
                        "Đã cập nhật địa chỉ", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(AddEditAddressActivity.this, 
                        "Không thể cập nhật địa chỉ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Address> call, Throwable t) {
                showLoading(false);
                Toast.makeText(AddEditAddressActivity.this, 
                    "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        if (layoutLoading != null) {
            layoutLoading.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        btnSave.setEnabled(!show);
    }
}
