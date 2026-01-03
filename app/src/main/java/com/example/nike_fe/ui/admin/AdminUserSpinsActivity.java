package com.example.nike_fe.ui.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nike_fe.R;
import com.example.nike_fe.data.api.LuckyWheelApi;
import com.example.nike_fe.data.model.UserSpinManagement;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminUserSpinsActivity extends AppCompatActivity {

    private RecyclerView rvUserSpins;
    private TextInputEditText etSearchUser;
    private UserSpinAdapter adapter;

    private LuckyWheelApi api;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_spins);

        com.example.nike_fe.data.api.RetrofitClient retrofitClient = 
            com.example.nike_fe.data.api.RetrofitClient.getInstance(this);
        api = retrofitClient.getLuckyWheelApi();
        String savedToken = retrofitClient.getToken();
        if (savedToken != null) {
            token = "Bearer " + savedToken;
        }

        initViews();
        loadData();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        rvUserSpins = findViewById(R.id.rvUserSpins);
        etSearchUser = findViewById(R.id.etSearchUser);

        rvUserSpins.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserSpinAdapter(new ArrayList<>(), this::resetUserSpins, this::addBonusSpins);
        rvUserSpins.setAdapter(adapter);

        etSearchUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void loadData() {
        if (token == null)
            return;

        api.getUserSpinManagement(token).enqueue(new Callback<List<UserSpinManagement>>() {
            @Override
            public void onResponse(Call<List<UserSpinManagement>> call, Response<List<UserSpinManagement>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setData(response.body());
                } else {
                    Toast.makeText(AdminUserSpinsActivity.this, "Lỗi tải danh sách", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<UserSpinManagement>> call, Throwable t) {
                Toast.makeText(AdminUserSpinsActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetUserSpins(UserSpinManagement user) {
        new AlertDialog.Builder(this)
                .setTitle("Reset lượt quay")
                .setMessage("Bạn có chắc muốn reset lượt quay hôm nay của " + user.getUsername() + "?")
                .setPositiveButton("Reset", (dialog, which) -> {
                    api.resetUserSpins(token, user.getUserId()).enqueue(new Callback<Map<String, Object>>() {
                        @Override
                        public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(AdminUserSpinsActivity.this, "Đã reset thành công", Toast.LENGTH_SHORT)
                                        .show();
                                loadData(); // Reload to update UI
                            } else {
                                Toast.makeText(AdminUserSpinsActivity.this, "Lỗi reset", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                            Toast.makeText(AdminUserSpinsActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void addBonusSpins(UserSpinManagement user) {
        // Show dialog with input for number of spins
        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setHint("Nhập số lượt quay (VD: 5)");

        new AlertDialog.Builder(this)
                .setTitle("Tặng lượt quay cho " + user.getUsername())
                .setView(input)
                .setPositiveButton("Tặng", (dialog, which) -> {
                    String valueStr = input.getText().toString();
                    if (valueStr.isEmpty())
                        return;

                    int bonusSpins = Integer.parseInt(valueStr);

                    api.grantBonusSpins(token, user.getUserId(), bonusSpins)
                            .enqueue(new Callback<Map<String, Object>>() {
                                @Override
                                public void onResponse(Call<Map<String, Object>> call,
                                        Response<Map<String, Object>> response) {
                                    if (response.isSuccessful()) {
                                        Toast.makeText(AdminUserSpinsActivity.this,
                                                "Đã tặng " + bonusSpins + " lượt quay", Toast.LENGTH_SHORT).show();
                                        loadData();
                                    } else {
                                        Toast.makeText(AdminUserSpinsActivity.this, "Lỗi tặng lượt quay",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                                    Toast.makeText(AdminUserSpinsActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
