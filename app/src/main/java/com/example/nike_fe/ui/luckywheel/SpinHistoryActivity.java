package com.example.nike_fe.ui.luckywheel;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nike_fe.R;
import com.example.nike_fe.data.api.LuckyWheelApi;
import com.example.nike_fe.data.api.RetrofitClient;
import com.example.nike_fe.data.model.SpinHistoryItem;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SpinHistoryActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private RecyclerView rvHistory;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    
    private LuckyWheelApi luckyWheelApi;
    private RetrofitClient retrofitClient;
    private SpinHistoryAdapter historyAdapter;
    private List<SpinHistoryItem> historyList = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spin_history);
        
        initViews();
        setupToolbar();
        setupRecyclerView();
        loadHistory();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvHistory = findViewById(R.id.rvHistory);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        
        retrofitClient = RetrofitClient.getInstance(this);
        luckyWheelApi = retrofitClient.getLuckyWheelApi();
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }
    
    private void setupRecyclerView() {
        historyAdapter = new SpinHistoryAdapter(this, historyList);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setAdapter(historyAdapter);
    }
    
    private String getAuthToken() {
        String token = retrofitClient.getToken();
        if (token != null && !token.isEmpty()) {
            return "Bearer " + token;
        }
        return null;
    }
    
    private void loadHistory() {
        String token = getAuthToken();
        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        progressBar.setVisibility(View.VISIBLE);
        rvHistory.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);
        
        luckyWheelApi.getHistory(token, 0, 50).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        Map<String, Object> responseBody = response.body();
                        List<Map<String, Object>> content = (List<Map<String, Object>>) responseBody.get("content");
                        
                        if (content != null && !content.isEmpty()) {
                            historyList.clear();
                            Gson gson = new Gson();
                            
                            for (Map<String, Object> item : content) {
                                String json = gson.toJson(item);
                                SpinHistoryItem historyItem = gson.fromJson(json, SpinHistoryItem.class);
                                historyList.add(historyItem);
                            }
                            
                            historyAdapter.notifyDataSetChanged();
                            rvHistory.setVisibility(View.VISIBLE);
                        } else {
                            tvEmpty.setVisibility(View.VISIBLE);
                        }
                    } catch (Exception e) {
                        android.util.Log.e("SpinHistory", "Error parsing response", e);
                        Toast.makeText(SpinHistoryActivity.this, 
                            "Lỗi xử lý dữ liệu", Toast.LENGTH_SHORT).show();
                        tvEmpty.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(SpinHistoryActivity.this, 
                        "Không thể tải lịch sử", Toast.LENGTH_SHORT).show();
                    tvEmpty.setVisibility(View.VISIBLE);
                }
            }
            
            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
                Toast.makeText(SpinHistoryActivity.this, 
                    "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
