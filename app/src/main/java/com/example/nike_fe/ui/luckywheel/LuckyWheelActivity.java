package com.example.nike_fe.ui.luckywheel;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.nike_fe.R;
import com.example.nike_fe.data.api.LuckyWheelApi;
import com.example.nike_fe.data.api.RetrofitClient;
import com.example.nike_fe.data.model.SpinRequest;
import com.example.nike_fe.data.model.SpinResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LuckyWheelActivity extends AppCompatActivity {

    private WheelView wheelView;
    private Button btnSpin;
    private TextView tvCoins;
    private TextView tvSpinCount;
    private android.widget.ProgressBar pbSpins;
    private LuckyWheelApi luckyWheelApi;
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
            getSupportActionBar().setTitle(""); // Title is now in the layout body
        }

        // Initialize views
        wheelView = findViewById(R.id.wheelView);
        btnSpin = findViewById(R.id.btnSpin);
        tvCoins = findViewById(R.id.tvCoins);
        tvSpinCount = findViewById(R.id.tvSpinCount);
        pbSpins = findViewById(R.id.pbSpins);

        // Initialize API
        luckyWheelApi = RetrofitClient.getInstance(this).getLuckyWheelApi();

        // Load user data
        loadUserData();

        // Setup spin button
        btnSpin.setOnClickListener(v -> spinWheel());
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
                    tvCoins.setText("Coins: " + status.getCurrentCoins());
                    tvSpinCount.setText(
                            "Lượt quay hôm nay: " + status.getTodaySpinCount() + "/" + status.getMaxFreeSpins());

                    if (pbSpins != null) {
                        pbSpins.setMax(status.getMaxFreeSpins());
                        pbSpins.setProgress(status.getTodaySpinCount());
                    }

                    // Update button text and track free spin status
                    hasFreeSpinAvailable = status.isHasFreeSpinToday();
                    if (status.isHasFreeSpinToday()) {
                        btnSpin.setText("QUAY MIỄN PHÍ");
                    } else {
                        btnSpin.setText("QUAY (" + status.getSpinCost() + " COINS)");
                    }
                }
            }

            @Override
            public void onFailure(Call<LuckyWheelApi.SpinStatusResponse> call, Throwable t) {
                // Show default values
                tvCoins.setText("Coins: 0");
                tvSpinCount.setText("Lượt quay hôm nay: 0/3");
                if (pbSpins != null) {
                    pbSpins.setMax(3);
                    pbSpins.setProgress(0);
                }
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
                            // DEBUG info included to help fix the prize array mismatch
                            String debugMsg = spinResponse.getMessage() +
                                    " (Pos: " + spinResponse.getRewardPosition() +
                                    ", Coins: " + spinResponse.getCoinAmount() + ")";

                            Toast.makeText(LuckyWheelActivity.this,
                                    debugMsg,
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
