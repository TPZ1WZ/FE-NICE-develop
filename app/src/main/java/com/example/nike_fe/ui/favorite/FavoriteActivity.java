package com.example.nike_fe.ui.favorite;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nike_fe.R;
import com.example.nike_fe.data.api.FavoriteApi;
import com.example.nike_fe.data.api.RetrofitClient;
import com.example.nike_fe.data.model.FavoriteProduct;
import com.example.nike_fe.data.model.FavoriteResponse;
import com.example.nike_fe.ui.product.ProductDetailActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoriteActivity extends AppCompatActivity implements FavoriteAdapter.OnFavoriteClickListener {

    private static final String TAG = "FavoriteActivity";

    private RecyclerView recyclerView;
    private FavoriteAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private ImageView ivBack;

    private List<FavoriteProduct> favoriteList = new ArrayList<>();
    private FavoriteApi favoriteApi;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        initViews();
        initApi();
        // loadFavorites(); -> Moved to onResume
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavorites();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewFavorites);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        ivBack = findViewById(R.id.ivBack);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FavoriteAdapter(favoriteList, this);
        recyclerView.setAdapter(adapter);

        ivBack.setOnClickListener(v -> finish());
    }

    private void initApi() {
        RetrofitClient client = RetrofitClient.getInstance(this);
        favoriteApi = client.getFavoriteApi();
        token = "Bearer " + client.getToken();
    }

    private void loadFavorites() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        favoriteApi.getUserFavorites(0, 100, token).enqueue(new Callback<FavoriteResponse>() {
            @Override
            public void onResponse(Call<FavoriteResponse> call, Response<FavoriteResponse> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    FavoriteResponse favoriteResponse = response.body();

                    if (favoriteResponse.isSuccess() && favoriteResponse.getData() != null) {
                        favoriteList.clear();
                        favoriteList.addAll(favoriteResponse.getData());
                        adapter.notifyDataSetChanged();

                        if (favoriteList.isEmpty()) {
                            tvEmpty.setVisibility(View.VISIBLE);
                            tvEmpty.setText("Chưa có sản phẩm yêu thích nào");
                        }
                    }
                } else {
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("Không thể tải danh sách yêu thích");
                    Toast.makeText(FavoriteActivity.this,
                            "Lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FavoriteResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText("Không thể kết nối đến server");

                Log.e(TAG, "Load favorites failed", t);
                Toast.makeText(FavoriteActivity.this,
                        "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onProductClick(FavoriteProduct product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, product.getProductId());
        startActivity(intent);
    }

    private boolean isRemoving = false;

    @Override
    public void onRemoveClick(FavoriteProduct product) {
        if (isRemoving) {
            return; // Prevent multiple simultaneous requests
        }

        isRemoving = true;
        progressBar.setVisibility(View.VISIBLE);

        favoriteApi.removeFromFavorites(product.getProductId(), token)
                .enqueue(new Callback<java.util.Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<java.util.Map<String, Object>> call,
                            Response<java.util.Map<String, Object>> response) {
                        progressBar.setVisibility(View.GONE);
                        isRemoving = false;

                        if (response.isSuccessful()) {
                            favoriteList.remove(product);
                            adapter.notifyDataSetChanged();

                            if (favoriteList.isEmpty()) {
                                tvEmpty.setVisibility(View.VISIBLE);
                                tvEmpty.setText("Chưa có sản phẩm yêu thích nào");
                            }

                            Toast.makeText(FavoriteActivity.this,
                                    "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(FavoriteActivity.this,
                                    "Không thể xóa", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<java.util.Map<String, Object>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        isRemoving = false;
                        Log.e(TAG, "Remove favorite failed", t);
                        Toast.makeText(FavoriteActivity.this,
                                "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
