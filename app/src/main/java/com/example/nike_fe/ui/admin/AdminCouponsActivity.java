package com.example.nike_fe.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nike_fe.R;
import com.example.nike_fe.data.api.AdminCouponApi;
import com.example.nike_fe.data.api.RetrofitClient;
import com.example.nike_fe.data.model.Coupon;
import com.example.nike_fe.data.model.CouponListResponse;
import com.example.nike_fe.ui.admin.adapter.AdminCouponAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminCouponsActivity extends AppCompatActivity implements AdminCouponAdapter.OnCouponActionClickListener {

    private static final String TAG = "AdminCouponsActivity";

    private ImageView ivBack;
    private FloatingActionButton fabAddCoupon;
    private TextInputEditText etSearch;
    private RecyclerView rvCoupons;
    private FrameLayout layoutLoading;
    private LinearLayout layoutEmpty;

    private AdminCouponApi adminCouponApi;
    private AdminCouponAdapter adapter;
    private String token;
    private List<Coupon> allCoupons = new ArrayList<>(); // For local search if pagination not heavy

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_coupons);

        initViews();
        setupRecyclerView();
        loadCoupons();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCoupons(); // Reload when returning from Add/Edit
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        fabAddCoupon = findViewById(R.id.fabAddCoupon);
        etSearch = findViewById(R.id.etSearch);
        rvCoupons = findViewById(R.id.rvCoupons);
        layoutLoading = findViewById(R.id.layoutLoading);
        layoutEmpty = findViewById(R.id.layoutEmpty);

        RetrofitClient retrofitClient = RetrofitClient.getInstance(this);
        adminCouponApi = retrofitClient.getAdminCouponApi();
        token = retrofitClient.getToken();

        ivBack.setOnClickListener(v -> finish());

        fabAddCoupon.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminCouponFormActivity.class);
            startActivity(intent);
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCoupons(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new AdminCouponAdapter(this, this);
        rvCoupons.setLayoutManager(new LinearLayoutManager(this));
        rvCoupons.setAdapter(adapter);
    }

    private void loadCoupons() {
        layoutLoading.setVisibility(View.VISIBLE);
        // Page 0, Size 100 for simplicity initially
        adminCouponApi.getCoupons("Bearer " + token, 0, 100, "id,desc").enqueue(new Callback<CouponListResponse>() {
            @Override
            public void onResponse(Call<CouponListResponse> call, Response<CouponListResponse> response) {
                layoutLoading.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    allCoupons = response.body().getContent();
                    updateList(allCoupons);
                } else {
                    Log.e(TAG, "Failed to load coupons: " + response.code());
                    Toast.makeText(AdminCouponsActivity.this, "Lỗi tải danh sách: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CouponListResponse> call, Throwable t) {
                layoutLoading.setVisibility(View.GONE);
                Log.e(TAG, "Error loading coupons", t);
                Toast.makeText(AdminCouponsActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterCoupons(String query) {
        if (allCoupons == null)
            return;

        List<Coupon> filteredList = new ArrayList<>();
        for (Coupon coupon : allCoupons) {
            if (coupon.getCode().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(coupon);
            }
        }
        updateList(filteredList);
    }

    private void updateList(List<Coupon> list) {
        if (list == null || list.isEmpty()) {
            rvCoupons.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            rvCoupons.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
            adapter.setCoupons(list);
        }
    }

    @Override
    public void onEdit(Coupon coupon) {
        Intent intent = new Intent(this, AdminCouponFormActivity.class);
        intent.putExtra("coupon_id", coupon.getId());
        startActivity(intent);
    }

    @Override
    public void onDelete(Coupon coupon) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa Coupon")
                .setMessage("Bạn có chắc chắn muốn xóa coupon " + coupon.getCode() + "?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteCoupon(coupon.getId()))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteCoupon(Long id) {
        layoutLoading.setVisibility(View.VISIBLE);
        adminCouponApi.deleteCoupon("Bearer " + token, id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                layoutLoading.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(AdminCouponsActivity.this, "Xóa thành công", Toast.LENGTH_SHORT).show();
                    loadCoupons();
                } else {
                    Toast.makeText(AdminCouponsActivity.this, "Lỗi xóa: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                layoutLoading.setVisibility(View.GONE);
                Toast.makeText(AdminCouponsActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
