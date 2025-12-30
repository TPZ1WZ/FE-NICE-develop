package com.example.nike_fe.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nike_fe.R;
import com.example.nike_fe.data.api.AdminApi;
import com.example.nike_fe.data.api.RetrofitClient;
import com.example.nike_fe.data.model.Order;
import com.example.nike_fe.ui.admin.adapter.AdminOrderAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminOrdersActivity extends AppCompatActivity {
    
    private RecyclerView recyclerView;
    private AdminOrderAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty, tvTotalOrders;
    private Spinner spinnerStatus;
    private ImageView ivBack;
    
    private AdminApi adminApi;
    private String token;
    private String currentStatus = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_admin_orders);
            
            // Check for filter from intent (from dashboard)
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra("filter")) {
                String filter = intent.getStringExtra("filter");
                if ("pending".equals(filter)) {
                    currentStatus = "pending";
                }
            }
            
            initViews();
            setupSpinner();
            setupRecyclerView();
            loadOrders(currentStatus);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khởi tạo: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }
    
    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewOrders);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvTotalOrders = findViewById(R.id.tvTotalOrders);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        ivBack = findViewById(R.id.ivBack);
        
        RetrofitClient retrofitClient = RetrofitClient.getInstance(this);
        adminApi = retrofitClient.getAdminApi();
        token = retrofitClient.getToken();
        
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        ivBack.setOnClickListener(v -> finish());
    }
    
    private void setupSpinner() {
        String[] statuses = {
            "Tất cả đơn hàng",
            "Chờ xác nhận",
            "Đã xác nhận",
            "Đang giao",
            "Hoàn tất",
            "Đã hủy"
        };
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this, 
            android.R.layout.simple_spinner_item, 
            statuses
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapter);
        
        // Set initial position based on currentStatus from intent
        if (currentStatus != null && currentStatus.equals("pending")) {
            spinnerStatus.setSelection(1, false); // "Chờ xác nhận" is at position 1
        }
        
        spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: currentStatus = null; break;
                    case 1: currentStatus = "pending"; break;
                    case 2: currentStatus = "confirmed"; break;
                    case 3: currentStatus = "shipping"; break;
                    case 4: currentStatus = "completed"; break;
                    case 5: currentStatus = "canceled"; break;
                }
                loadOrders(currentStatus);
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
    
    private void setupRecyclerView() {
        adapter = new AdminOrderAdapter(new ArrayList<>(), order -> {
            Intent intent = new Intent(AdminOrdersActivity.this, AdminOrderDetailActivity.class);
            intent.putExtra("order_id", order.getId());
            startActivity(intent);
        });
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
    
    private void loadOrders(String status) {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);
        
        adminApi.getAllOrders("Bearer " + token, status).enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    List<Order> orders = response.body();
                    
                    // Debug logging
                    if (!orders.isEmpty()) {
                        Order firstOrder = orders.get(0);
                        android.util.Log.d("AdminOrders", "First order: ID=" + firstOrder.getId() 
                            + ", userName=" + firstOrder.getUserName()
                            + ", finalAmount=" + firstOrder.getFinalAmount()
                            + ", totalAmount=" + firstOrder.getTotalAmount()
                            + ", paymentMethod=" + firstOrder.getPaymentMethod());
                    }
                    
                    tvTotalOrders.setText("Tổng: " + orders.size() + " đơn");
                    
                    if (orders.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        adapter.updateData(orders);
                    }
                } else {
                    Toast.makeText(AdminOrdersActivity.this, 
                            "Lỗi tải đơn hàng: " + response.code(), Toast.LENGTH_SHORT).show();
                    tvEmpty.setVisibility(View.VISIBLE);
                }
            }
            
            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
                Toast.makeText(AdminOrdersActivity.this, 
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadOrders(currentStatus);
    }
}
