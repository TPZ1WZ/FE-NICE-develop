package com.example.nike_fe.ui.admin;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nike_fe.R;
import com.example.nike_fe.data.api.LuckyWheelApi;
import com.example.nike_fe.data.model.BatchProbabilityRequest;
import com.example.nike_fe.data.model.Prize;
import com.example.nike_fe.data.model.PrizeProbabilityUpdate;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminProbabilityActivity extends AppCompatActivity {

    private RecyclerView rvProbabilities;
    private TextView tvTotalProbability;
    private Button btnSave;
    private ProbabilityAdapter adapter;

    private LuckyWheelApi api;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_probability);

        com.example.nike_fe.data.api.RetrofitClient retrofitClient = 
            com.example.nike_fe.data.api.RetrofitClient.getInstance(this);
        api = retrofitClient.getLuckyWheelApi();
        String savedToken = retrofitClient.getToken();
        if (savedToken != null) {
            token = "Bearer " + savedToken;
        }

        initViews();
        loadPrizes();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        rvProbabilities = findViewById(R.id.rvProbabilities);
        tvTotalProbability = findViewById(R.id.tvTotalProbability);
        btnSave = findViewById(R.id.btnSave);

        rvProbabilities.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProbabilityAdapter(new ArrayList<>(), this::updateTotal);
        rvProbabilities.setAdapter(adapter);

        btnSave.setOnClickListener(v -> saveProbabilities());
    }

    private void loadPrizes() {
        if (token == null)
            return;

        api.getAllPrizes(token).enqueue(new Callback<List<Prize>>() {
            @Override
            public void onResponse(Call<List<Prize>> call, Response<List<Prize>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setPrizes(response.body());
                    updateTotal();
                } else {
                    Toast.makeText(AdminProbabilityActivity.this, "Lỗi tải danh sách", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Prize>> call, Throwable t) {
                Toast.makeText(AdminProbabilityActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTotal() {
        double total = 0;
        for (Prize p : adapter.getPrizes()) {
            if (p.getProbability() != null) {
                total += p.getProbability();
            }
        }

        int totalPercent = (int) Math.round(total * 100);
        tvTotalProbability.setText(totalPercent + "%");

        if (totalPercent == 100) {
            tvTotalProbability.setTextColor(Color.parseColor("#10B981")); // Green
        } else {
            tvTotalProbability.setTextColor(Color.parseColor("#EF4444")); // Red
        }
    }

    private void saveProbabilities() {
        if (token == null)
            return;

        List<PrizeProbabilityUpdate> updates = new ArrayList<>();
        for (Prize p : adapter.getPrizes()) {
            updates.add(new PrizeProbabilityUpdate(p.getId(), p.getProbability()));
        }

        BatchProbabilityRequest request = new BatchProbabilityRequest(updates);

        api.batchUpdateProbabilities(token, request).enqueue(new Callback<List<Prize>>() {
            @Override
            public void onResponse(Call<List<Prize>> call, Response<List<Prize>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminProbabilityActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AdminProbabilityActivity.this, "Lỗi cập nhật", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Prize>> call, Throwable t) {
                Toast.makeText(AdminProbabilityActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
