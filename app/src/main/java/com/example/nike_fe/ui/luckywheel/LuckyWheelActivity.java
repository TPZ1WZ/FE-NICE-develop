package com.example.nike_fe.ui.luckywheel;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.nike_fe.R;
import com.example.nike_fe.data.api.LuckyWheelApi;
import com.example.nike_fe.data.api.LuckyWheelAdminApi;
import com.example.nike_fe.data.api.RetrofitClient;
import com.example.nike_fe.data.model.LuckyWheelReward;
import com.example.nike_fe.data.model.SpinRequest;
import com.example.nike_fe.data.model.SpinResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LuckyWheelActivity extends AppCompatActivity {

    private WheelView wheelView;
    private Button btnSpin;
    private TextView tvCoins;
    private TextView tvSpinCount;
    private TextView tvProductViews;
    private android.widget.ProgressBar pbSpins;
    private android.widget.ProgressBar pbProductViews;
    private LuckyWheelApi luckyWheelApi;
    private LuckyWheelAdminApi adminApi;
    private boolean isSpinning = false;
    private boolean hasFreeSpinAvailable = false; // Track if user has free spin

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lucky_wheel);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Vòng quay may mắn");
        }

        // Initialize views
        wheelView = findViewById(R.id.wheelView);
        btnSpin = findViewById(R.id.btnSpin);
        tvCoins = findViewById(R.id.tvCoins);
        tvSpinCount = findViewById(R.id.tvSpinCount);
        tvProductViews = findViewById(R.id.tvProductViews);
        pbSpins = findViewById(R.id.pbSpins);
        pbProductViews = findViewById(R.id.pbProductViews);

        // Initialize API
        luckyWheelApi = RetrofitClient.getInstance(this).getLuckyWheelApi();
        adminApi = RetrofitClient.getInstance(this).getLuckyWheelAdminApi();

        // Load wheel rewards from admin API
        loadWheelRewards();
        
        // Load user data
        loadUserData();

        // Setup spin button
        btnSpin.setOnClickListener(v -> spinWheel());
    }

    private void loadWheelRewards() {
        String token = RetrofitClient.getInstance(this).getToken();
        if (token == null) return;

        adminApi.getRewards("Bearer " + token).enqueue(new Callback<List<LuckyWheelReward>>() {
            @Override
            public void onResponse(Call<List<LuckyWheelReward>> call, Response<List<LuckyWheelReward>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<LuckyWheelReward> rewards = response.body();
                    
                    // Update wheel with full labels from admin API - MUST map by position
                    String[] labels = new String[8];
                    for (int i = 0; i < 8; i++) {
                        labels[i] = ""; // Default empty
                    }
                    
                    for (LuckyWheelReward reward : rewards) {
                        int pos = reward.getPosition();
                        if (pos >= 0 && pos < 8) {
                            labels[pos] = reward.getLabel() != null ? reward.getLabel() : "";
                        }
                    }
                    wheelView.setPrizes(labels);
                }
            }

            @Override
            public void onFailure(Call<List<LuckyWheelReward>> call, Throwable t) {
                // Keep default prizes if API fails
            }
        });
    }

    private void loadUserData() {
        String token = RetrofitClient.getInstance(this).getToken();
        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        luckyWheelApi.getInfo("Bearer " + token).enqueue(new Callback<LuckyWheelApi.SpinStatusResponse>() {
            @Override
            public void onResponse(Call<LuckyWheelApi.SpinStatusResponse> call,
                    Response<LuckyWheelApi.SpinStatusResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LuckyWheelApi.SpinStatusResponse status = response.body();
                    
                    // Update coins
                    tvCoins.setText("Nike Coins: " + status.getCurrentCoins());
                    
                    // Update product views
                    long viewed = status.getProductsViewedToday();
                    int required = status.getRequiredProductViews();
                    tvProductViews.setText("Đã xem: " + viewed + "/" + required + " sản phẩm");
                    if (pbProductViews != null) {
                        pbProductViews.setMax(required);
                        pbProductViews.setProgress((int) viewed);
                    }
                    
                    // Update spin count
                    tvSpinCount.setText(
                            "Lượt quay hôm nay: " + status.getTodaySpinCount() + "/" + status.getMaxFreeSpins());
                    if (pbSpins != null) {
                        pbSpins.setMax(status.getMaxFreeSpins());
                        pbSpins.setProgress(status.getTodaySpinCount());
                    }

                    // Update button state
                    hasFreeSpinAvailable = status.isHasFreeSpinToday();
                    boolean canSpin = status.isWheelEnabled() && hasFreeSpinAvailable && viewed >= required;
                    
                    btnSpin.setEnabled(canSpin);
                    
                    // Priority: Admin control > User conditions
                    if (!status.isWheelEnabled()) {
                        btnSpin.setText("VÒNG QUAY TẠM ĐÓNG");
                    } else if (viewed < required) {
                        btnSpin.setText("XEM THÊM " + (required - viewed) + " SẢN PHẨM");
                    } else if (hasFreeSpinAvailable) {
                        btnSpin.setText("QUAY MIỄN PHÍ");
                    } else {
                        btnSpin.setText("ĐÃ HẾT LƯỢT QUAY HÔM NAY");
                    }
                }
            }

            @Override
            public void onFailure(Call<LuckyWheelApi.SpinStatusResponse> call, Throwable t) {
                // Show default values
                tvCoins.setText("Nike Coins: 0");
                tvProductViews.setText("Đã xem: 0/3 sản phẩm");
                tvSpinCount.setText("Lượt quay hôm nay: 0/1");
                if (pbProductViews != null) {
                    pbProductViews.setMax(3);
                    pbProductViews.setProgress(0);
                }
                if (pbSpins != null) {
                    pbSpins.setMax(1);
                    pbSpins.setProgress(0);
                }
                btnSpin.setEnabled(false);
                btnSpin.setText("LỖI KẾT NỐI");
            }
        });
    }

    private void spinWheel() {
        if (isSpinning) {
            return;
        }

        String token = RetrofitClient.getInstance(this).getToken();
        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        isSpinning = true;
        btnSpin.setEnabled(false);

        // Use free spin if available, otherwise pay with coins
        SpinRequest request = new SpinRequest(hasFreeSpinAvailable);

        luckyWheelApi.spin("Bearer " + token, request).enqueue(new Callback<SpinResponse>() {
            @Override
            public void onResponse(Call<SpinResponse> call, Response<SpinResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SpinResponse spinResponse = response.body();

                    if (spinResponse.isSuccess()) {
                        // Animate wheel
                        wheelView.spinTo(spinResponse.getRewardPosition(), () -> {
                            // Show result message from server
                            Toast.makeText(LuckyWheelActivity.this,
                                    spinResponse.getMessage(),
                                    Toast.LENGTH_LONG).show();

                            // Update UI with new coin balance
                            if (spinResponse.getTotalPoints() != null) {
                                tvCoins.setText("Coins: " + spinResponse.getTotalPoints());
                            }
                            loadUserData();
                            isSpinning = false;
                            btnSpin.setEnabled(true);
                        });
                    } else {
                        // Show error message from server
                        Toast.makeText(LuckyWheelActivity.this,
                                spinResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        isSpinning = false;
                        btnSpin.setEnabled(true);
                    }
                } else {
                    Toast.makeText(LuckyWheelActivity.this,
                            "Lỗi: " + response.message(),
                            Toast.LENGTH_SHORT).show();
                    isSpinning = false;
                    btnSpin.setEnabled(true);
                }
            }

            @Override
            public void onFailure(Call<SpinResponse> call, Throwable t) {
                Toast.makeText(LuckyWheelActivity.this,
                        "Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                isSpinning = false;
                btnSpin.setEnabled(true);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
