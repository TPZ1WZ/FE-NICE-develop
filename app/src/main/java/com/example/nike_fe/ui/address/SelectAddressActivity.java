package com.example.nike_fe.ui.address;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nike_fe.R;
import com.example.nike_fe.data.api.AddressApi;
import com.example.nike_fe.data.api.RetrofitClient;
import com.example.nike_fe.data.model.Address;
import com.example.nike_fe.ui.address.adapter.AddressAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelectAddressActivity extends AppCompatActivity implements AddressAdapter.OnAddressClickListener {

    private ImageView ivBack;
    private RecyclerView rvAddresses;
    private LinearLayout layoutEmptyState;
    private LinearLayout btnAddNewAddress;
    private View layoutLoading;

    private AddressApi addressApi;
    private String token;
    private AddressAdapter adapter;
    private List<Address> addressList = new ArrayList<>();
    private Long selectedAddressId;

    private static final int REQUEST_ADD_ADDRESS = 1001;
    private static final int REQUEST_EDIT_ADDRESS = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_address);

        initViews();
        setupApi();
        setupRecyclerView();
        loadAddresses();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        rvAddresses = findViewById(R.id.rvAddresses);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        btnAddNewAddress = findViewById(R.id.btnAddNewAddress);
        layoutLoading = findViewById(R.id.layoutLoading);

        ivBack.setOnClickListener(v -> onBackPressed());
        btnAddNewAddress.setOnClickListener(v -> openAddAddressScreen());

        // Button in empty state
        Button btnAddFirstAddress = findViewById(R.id.btnAddFirstAddress);
        if (btnAddFirstAddress != null) {
            btnAddFirstAddress.setOnClickListener(v -> openAddAddressScreen());
        }
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

    private void setupRecyclerView() {
        adapter = new AddressAdapter(this, addressList, this);
        rvAddresses.setLayoutManager(new LinearLayoutManager(this));
        rvAddresses.setAdapter(adapter);
    }

    private void loadAddresses() {
        showLoading(true);

        addressApi.getUserAddresses("Bearer " + token).enqueue(new Callback<List<Address>>() {
            @Override
            public void onResponse(Call<List<Address>> call, Response<List<Address>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    addressList.clear();
                    addressList.addAll(response.body());
                    adapter.notifyDataSetChanged();

                    updateEmptyState();
                } else {
                    Toast.makeText(SelectAddressActivity.this,
                            "Không thể tải danh sách địa chỉ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Address>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(SelectAddressActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateEmptyState() {
        if (addressList.isEmpty()) {
            rvAddresses.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
            btnAddNewAddress.setVisibility(View.GONE);
        } else {
            rvAddresses.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
            btnAddNewAddress.setVisibility(View.VISIBLE);
        }
    }

    private void openAddAddressScreen() {
        Intent intent = new Intent(this, AddEditAddressActivity.class);
        startActivityForResult(intent, REQUEST_ADD_ADDRESS);
    }

    @Override
    public void onAddressSelected(Address address) {
        selectedAddressId = address.getId();

        // Return selected address to checkout
        Intent resultIntent = new Intent();
        resultIntent.putExtra("selected_address_id", address.getId());
        resultIntent.putExtra("recipient_name", address.getRecipientName());
        resultIntent.putExtra("phone_number", address.getPhoneNumber());
        resultIntent.putExtra("full_address", address.getFullAddress());
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void onEditAddress(Address address) {
        Intent intent = new Intent(this, AddEditAddressActivity.class);
        intent.putExtra("address_id", address.getId());
        intent.putExtra("recipient_name", address.getRecipientName());
        intent.putExtra("phone_number", address.getPhoneNumber());
        intent.putExtra("address_line", address.getAddressLine());
        intent.putExtra("ward", address.getWard());
        intent.putExtra("district", address.getDistrict());
        intent.putExtra("city", address.getCity());
        intent.putExtra("is_default", address.isDefault());
        startActivityForResult(intent, REQUEST_EDIT_ADDRESS);
    }

    @Override
    public void onDeleteAddress(Address address) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa địa chỉ")
                .setMessage("Bạn có chắc chắn muốn xóa địa chỉ này?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteAddress(address.getId()))
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onSetDefault(Address address) {
        showLoading(true);

        addressApi.setDefaultAddress("Bearer " + token, address.getId())
                .enqueue(new Callback<Address>() {
                    @Override
                    public void onResponse(Call<Address> call, Response<Address> response) {
                        showLoading(false);

                        if (response.isSuccessful()) {
                            Toast.makeText(SelectAddressActivity.this,
                                    "Đã đặt làm địa chỉ mặc định", Toast.LENGTH_SHORT).show();
                            loadAddresses();
                        } else {
                            Toast.makeText(SelectAddressActivity.this,
                                    "Không thể đặt làm mặc định", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Address> call, Throwable t) {
                        showLoading(false);
                        Toast.makeText(SelectAddressActivity.this,
                                "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteAddress(Long addressId) {
        showLoading(true);

        addressApi.deleteAddress("Bearer " + token, addressId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                showLoading(false);

                if (response.isSuccessful()) {
                    Toast.makeText(SelectAddressActivity.this,
                            "Đã xóa địa chỉ", Toast.LENGTH_SHORT).show();
                    loadAddresses();
                } else {
                    Toast.makeText(SelectAddressActivity.this,
                            "Không thể xóa địa chỉ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showLoading(false);
                Toast.makeText(SelectAddressActivity.this,
                        "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        if (layoutLoading != null) {
            layoutLoading.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && (requestCode == REQUEST_ADD_ADDRESS || requestCode == REQUEST_EDIT_ADDRESS)) {
            // Reload addresses after add/edit
            loadAddresses();
        }
    }
}
