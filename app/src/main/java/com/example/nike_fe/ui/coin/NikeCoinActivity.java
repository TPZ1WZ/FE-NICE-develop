package com.example.nike_fe.ui.coin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nike_fe.R;
import com.example.nike_fe.data.api.RetrofitClient;
import com.example.nike_fe.data.model.CheckinResponse;
import com.example.nike_fe.data.model.CheckinStreakResponse;
import com.example.nike_fe.data.model.LoyaltyPointsResponse;
import com.example.nike_fe.data.model.TransactionHistoryItem;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NikeCoinActivity extends AppCompatActivity {

    private LinearLayout layoutStreakDays;
    private MaterialButton btnClaim;
    private TextView tvCoinBalance;
    private TextView tvCoinExpiry;
    private TextView tvEmptyState;
    private ProgressBar progressBar;
    private RecyclerView rvTransactionHistory;
    private TransactionHistoryAdapter transactionAdapter;
    private RetrofitClient retrofitClient;

    // Data from API
    private CheckinStreakResponse streakData;
    private int currentPoints = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nike_coin);

        initViews();
        loadData();
        setupEvents();
    }

    private void initViews() {
        layoutStreakDays = findViewById(R.id.layoutStreakDays);
        btnClaim = findViewById(R.id.btnClaim);
        tvCoinBalance = findViewById(R.id.tvCoinBalance);
        tvCoinExpiry = findViewById(R.id.tvCoinExpiry);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        progressBar = findViewById(R.id.progressBar);
        rvTransactionHistory = findViewById(R.id.rvTransactionHistory);
        
        // Setup RecyclerView
        transactionAdapter = new TransactionHistoryAdapter();
        rvTransactionHistory.setLayoutManager(new LinearLayoutManager(this));
        rvTransactionHistory.setAdapter(transactionAdapter);
        
        retrofitClient = RetrofitClient.getInstance(this);

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());
    }

    private void loadData() {
        progressBar.setVisibility(View.VISIBLE);
        
        // Load loyalty points
        loadLoyaltyPoints();
        
        // Load checkin streak
        loadCheckinStreak();
        
        // Load transaction history
        loadTransactionHistory();
    }

    private void loadLoyaltyPoints() {
        String token = getAuthToken();
        if (token == null) {
            tvCoinBalance.setText("0");
            tvCoinExpiry.setText("0 coin sẽ hết hạn vào --/--/----");
            return;
        }

        retrofitClient.getLoyaltyApi().getLoyaltyPoints(token)
                .enqueue(new Callback<LoyaltyPointsResponse>() {
                    @Override
                    public void onResponse(Call<LoyaltyPointsResponse> call,
                                           Response<LoyaltyPointsResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            LoyaltyPointsResponse data = response.body();
                            currentPoints = data.getCurrentPoints();
                            tvCoinBalance.setText(String.format("%,d", currentPoints));
                            
                            // Hiển thị thông tin coin sắp hết hạn
                            updateExpiryInfo(data);
                        } else {
                            tvCoinBalance.setText("0");
                            tvCoinExpiry.setText("0 coin sẽ hết hạn vào --/--/----");
                        }
                    }

                    @Override
                    public void onFailure(Call<LoyaltyPointsResponse> call, Throwable t) {
                        tvCoinBalance.setText("0");
                        tvCoinExpiry.setText("0 coin sẽ hết hạn vào --/--/----");
                        android.util.Log.e("NikeCoin", "Failed to load points", t);
                    }
                });
    }

    private void loadCheckinStreak() {
        String token = getAuthToken();
        if (token == null) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Vui lòng đăng nhập để sử dụng tính năng này", 
                    Toast.LENGTH_SHORT).show();
            return;
        }

        retrofitClient.getLoyaltyApi().getCheckinStreak(token)
                .enqueue(new Callback<CheckinStreakResponse>() {
                    @Override
                    public void onResponse(Call<CheckinStreakResponse> call,
                                           Response<CheckinStreakResponse> response) {
                        progressBar.setVisibility(View.GONE);
                        
                        if (response.isSuccessful() && response.body() != null) {
                            streakData = response.body();
                            setupStreak();
                            updateClaimButton();
                        } else {
                            Toast.makeText(NikeCoinActivity.this,
                                    "Không thể tải dữ liệu streak", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<CheckinStreakResponse> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(NikeCoinActivity.this,
                                "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        android.util.Log.e("NikeCoin", "Failed to load streak", t);
                    }
                });
    }

    private void setupStreak() {
        if (streakData == null || streakData.getWeeklyRewards() == null) {
            return;
        }

        layoutStreakDays.removeAllViews();

        for (CheckinStreakResponse.DayRewardInfo dayInfo : streakData.getWeeklyRewards()) {
            View itemView = LayoutInflater.from(this)
                    .inflate(R.layout.item_streak_day, layoutStreakDays, false);

            // Layout params for weight
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) itemView.getLayoutParams();
            params.weight = 1;
            itemView.setLayoutParams(params);

            TextView tvDay = itemView.findViewById(R.id.tvDay);
            TextView tvAmount = itemView.findViewById(R.id.tvAmount);
            View viewBackground = itemView.findViewById(R.id.viewBackground);
            ImageView ivIcon = itemView.findViewById(R.id.ivIcon);

            tvDay.setText("Day " + dayInfo.getDayNumber());

            int amount = dayInfo.getRewardAmount();
            tvAmount.setText(amount >= 1000 ? (amount / 1000 + "k") : String.valueOf(amount));

            // Icons for bonus day
            if (dayInfo.getIsBonus() != null && dayInfo.getIsBonus()) {
                ivIcon.setImageResource(R.drawable.ic_coin_stack);
                ivIcon.setImageTintList(null); // Show original colors
            }

            // State styling based on API data
            String status = dayInfo.getStatus();
            
            if ("PAST".equals(status)) {
                // Past days - claimed or missed
                viewBackground.setBackgroundResource(R.drawable.bg_circle_gray);
                viewBackground.setAlpha(0.5f);
                tvDay.setAlpha(0.5f);
                tvAmount.setAlpha(0.5f);
            } else if ("TODAY".equals(status)) {
                // Today (Active)
                viewBackground.setBackgroundResource(R.drawable.bg_circle_streak_active);
                tvDay.setTextColor(getResources().getColor(android.R.color.black));
                tvAmount.setTextColor(getResources().getColor(android.R.color.black));
            } else {
                // Future days
                viewBackground.setBackgroundResource(R.drawable.bg_circle_gray);
            }

            layoutStreakDays.addView(itemView);
        }
    }

    private void updateClaimButton() {
        if (streakData == null) {
            return;
        }

        if (streakData.getHasCheckedInToday()) {
            btnClaim.setText("Đã nhận hôm nay");
            btnClaim.setEnabled(false);
            btnClaim.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(0xFFBDBDBD));
        } else if (streakData.getTodayReward() != null) {
            int amount = streakData.getTodayReward();
            btnClaim.setText("Nhận ngay +" + amount + " coin");
            btnClaim.setEnabled(true);
            btnClaim.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(0xFF000000));
        } else {
            btnClaim.setText("Không có lượt nhận");
            btnClaim.setEnabled(false);
        }
    }

    private void setupEvents() {
        btnClaim.setOnClickListener(v -> performCheckin());
    }

    private void performCheckin() {
        if (streakData == null || streakData.getHasCheckedInToday()) {
            return;
        }

        String token = getAuthToken();
        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        btnClaim.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        retrofitClient.getLoyaltyApi().performCheckin(token)
                .enqueue(new Callback<CheckinResponse>() {
                    @Override
                    public void onResponse(Call<CheckinResponse> call,
                                           Response<CheckinResponse> response) {
                        progressBar.setVisibility(View.GONE);
                        
                        if (response.isSuccessful() && response.body() != null) {
                            CheckinResponse result = response.body();
                            
                            if (result.getSuccess()) {
                                // Update UI
                                currentPoints = result.getTotalPoints();
                                tvCoinBalance.setText(String.format("%,d", currentPoints));
                                
                                Toast.makeText(NikeCoinActivity.this,
                                        "✅ " + result.getMessage() + 
                                        " +"+result.getRewardAmount()+" coin",
                                        Toast.LENGTH_LONG).show();
                                
                                // Reload streak data, loyalty points và transaction history để cập nhật
                                loadCheckinStreak();
                                loadLoyaltyPoints();
                                loadTransactionHistory();
                            } else {
                                Toast.makeText(NikeCoinActivity.this,
                                        result.getMessage(), Toast.LENGTH_SHORT).show();
                                btnClaim.setEnabled(true);
                            }
                        } else {
                            Toast.makeText(NikeCoinActivity.this,
                                    "Checkin thất bại", Toast.LENGTH_SHORT).show();
                            btnClaim.setEnabled(true);
                        }
                    }

                    @Override
                    public void onFailure(Call<CheckinResponse> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(NikeCoinActivity.this,
                                "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        btnClaim.setEnabled(true);
                        android.util.Log.e("NikeCoin", "Checkin failed", t);
                    }
                });
    }

    /**
     * Cập nhật thông tin coin sắp hết hạn
     */
    private void updateExpiryInfo(LoyaltyPointsResponse data) {
        Integer expiringCoins = data.getExpiringCoins();
        String expiryDate = data.getExpiryDate();
        
        if (expiringCoins != null && expiringCoins > 0 && expiryDate != null) {
            // Parse ISO 8601 date format: 2026-02-04T10:30:00
            // Extract date part and format as dd-MM-yyyy
            String formattedDate = formatExpiryDate(expiryDate);
            tvCoinExpiry.setText(String.format("%,d coin sẽ hết hạn vào %s", expiringCoins, formattedDate));
            tvCoinExpiry.setVisibility(View.VISIBLE);
        } else {
            tvCoinExpiry.setText("Không có coin sắp hết hạn");
            tvCoinExpiry.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * Format expiry date từ ISO 8601 sang dd-MM-yyyy
     */
    private String formatExpiryDate(String isoDate) {
        try {
            // Parse ISO date: 2026-02-04T10:30:00
            String[] parts = isoDate.split("T")[0].split("-");
            if (parts.length == 3) {
                String year = parts[0];
                String month = parts[1];
                String day = parts[2];
                return day + "-" + month + "-" + year;
            }
        } catch (Exception e) {
            android.util.Log.e("NikeCoin", "Failed to parse date: " + isoDate, e);
        }
        return "--/--/----";
    }
    
    /**
     * Load transaction history
     */
    private void loadTransactionHistory() {
        String token = getAuthToken();
        if (token == null) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvTransactionHistory.setVisibility(View.GONE);
            return;
        }

        retrofitClient.getLoyaltyApi().getTransactionHistory(token)
                .enqueue(new Callback<List<TransactionHistoryItem>>() {
                    @Override
                    public void onResponse(Call<List<TransactionHistoryItem>> call,
                                           Response<List<TransactionHistoryItem>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<TransactionHistoryItem> transactions = response.body();
                            if (transactions.isEmpty()) {
                                tvEmptyState.setVisibility(View.VISIBLE);
                                rvTransactionHistory.setVisibility(View.GONE);
                            } else {
                                tvEmptyState.setVisibility(View.GONE);
                                rvTransactionHistory.setVisibility(View.VISIBLE);
                                transactionAdapter.setTransactions(transactions);
                            }
                        } else {
                            tvEmptyState.setVisibility(View.VISIBLE);
                            rvTransactionHistory.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<TransactionHistoryItem>> call, Throwable t) {
                        tvEmptyState.setVisibility(View.VISIBLE);
                        rvTransactionHistory.setVisibility(View.GONE);
                        android.util.Log.e("NikeCoin", "Failed to load transaction history", t);
                    }
                });
    }

    private String getAuthToken() {
        String token = retrofitClient.getToken();
        if (token != null && !token.isEmpty()) {
            return "Bearer " + token;
        }
        return null;
    }
}

