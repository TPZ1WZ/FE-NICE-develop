package com.example.nike_fe.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nike_fe.R;
import com.example.nike_fe.data.api.AdminApi;
import com.example.nike_fe.data.api.RetrofitClient;
import com.example.nike_fe.data.model.User;
import com.example.nike_fe.data.model.UserListResponse;
import com.example.nike_fe.ui.admin.adapter.AdminUserAdapter;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminUsersActivity extends AppCompatActivity {

    private ImageView ivBack;
    private TextInputEditText etSearch;
    private RecyclerView rvUsers;
    private FrameLayout layoutLoading;
    private LinearLayout layoutEmpty;

    private AdminUserAdapter adapter;
    private AdminApi adminApi;
    private String token;
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_users);

        initViews();
        setupRecyclerView();
        setupClickListeners();
        loadUsers("");
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        etSearch = findViewById(R.id.etSearch);
        rvUsers = findViewById(R.id.rvUsers);
        layoutLoading = findViewById(R.id.layoutLoading);
        layoutEmpty = findViewById(R.id.layoutEmpty);

        RetrofitClient retrofitClient = RetrofitClient.getInstance(this);
        adminApi = retrofitClient.getAdminApi();
        token = retrofitClient.getToken();

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupRecyclerView() {
        adapter = new AdminUserAdapter();
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        rvUsers.setAdapter(adapter);

        adapter.setOnUserClickListener(new AdminUserAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(User user) {
                Intent intent = new Intent(AdminUsersActivity.this, AdminUserDetailActivity.class);
                intent.putExtra("user_id", user.getId());
                startActivity(intent);
            }

            @Override
            public void onMoreClick(User user, View view) {
                // Show popup menu or handle actions here if needed
                // For now, redirect to detail
                onUserClick(user);
            }
        });
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> finish());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                searchRunnable = () -> loadUsers(s.toString());
                searchHandler.postDelayed(searchRunnable, 500);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void loadUsers(String query) {
        layoutLoading.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
        rvUsers.setVisibility(View.GONE);

        Call<UserListResponse> call;
        // Use unified getUsers API for both load all and search
        call = adminApi.getUsers("Bearer " + token, query);
        // Note: if query is empty/null, backend should return all users

        call.enqueue(new Callback<UserListResponse>() {
            @Override
            public void onResponse(Call<UserListResponse> call, Response<UserListResponse> response) {
                layoutLoading.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<User> users = response.body().getContent();
                    if (users == null || users.isEmpty()) {
                        layoutEmpty.setVisibility(View.VISIBLE);
                    } else {
                        rvUsers.setVisibility(View.VISIBLE);
                        adapter.setUsers(users);
                    }
                } else {
                    layoutEmpty.setVisibility(View.VISIBLE);
                    Toast.makeText(AdminUsersActivity.this, "Lỗi tải dữ liệu: " + response.code(), Toast.LENGTH_SHORT)
                            .show();
                }
            }

            @Override
            public void onFailure(Call<UserListResponse> call, Throwable t) {
                layoutLoading.setVisibility(View.GONE);
                layoutEmpty.setVisibility(View.VISIBLE);
                Toast.makeText(AdminUsersActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUsers(etSearch.getText() != null ? etSearch.getText().toString() : "");
    }
}
