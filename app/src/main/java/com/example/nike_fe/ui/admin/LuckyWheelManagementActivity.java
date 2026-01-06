package com.example.nike_fe.ui.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nike_fe.R;
import com.example.nike_fe.data.api.LuckyWheelAdminApi;
import com.example.nike_fe.data.api.RetrofitClient;
import com.example.nike_fe.data.model.LuckyWheelReward;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.button.MaterialButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LuckyWheelManagementActivity extends AppCompatActivity {

    private SwitchCompat switchWheelEnabled;
    private RecyclerView recyclerView;
    private ImageView ivBack;
    private FrameLayout layoutLoading;
    
    private LuckyWheelAdminApi adminApi;
    private RewardAdapter adapter;
    private boolean isWheelEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lucky_wheel_management);

        // Initialize views
        ivBack = findViewById(R.id.ivBack);
        switchWheelEnabled = findViewById(R.id.switchWheelEnabled);
        recyclerView = findViewById(R.id.recyclerView);
        layoutLoading = findViewById(R.id.layoutLoading);

        // Initialize API
        adminApi = RetrofitClient.getInstance(this).getLuckyWheelAdminApi();

        // Setup RecyclerView
        adapter = new RewardAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Setup listeners
        ivBack.setOnClickListener(v -> onBackPressed());
        
        switchWheelEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                toggleWheel(isChecked);
            }
        });

        // Load data
        loadStatus();
        loadRewards();
    }

    private void loadStatus() {
        String token = RetrofitClient.getInstance(this).getToken();
        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        layoutLoading.setVisibility(View.VISIBLE);
        adminApi.getStatus("Bearer " + token).enqueue(new Callback<LuckyWheelAdminApi.StatusResponse>() {
            @Override
            public void onResponse(Call<LuckyWheelAdminApi.StatusResponse> call, 
                                 Response<LuckyWheelAdminApi.StatusResponse> response) {
                layoutLoading.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    LuckyWheelAdminApi.StatusResponse status = response.body();
                    isWheelEnabled = status.isEnabled();
                    switchWheelEnabled.setChecked(status.isEnabled());
                } else {
                    Toast.makeText(LuckyWheelManagementActivity.this, 
                        "Lỗi: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LuckyWheelAdminApi.StatusResponse> call, Throwable t) {
                layoutLoading.setVisibility(View.GONE);
                Toast.makeText(LuckyWheelManagementActivity.this, 
                    "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleWheel(boolean enabled) {
        String token = RetrofitClient.getInstance(this).getToken();
        layoutLoading.setVisibility(View.VISIBLE);
        
        adminApi.toggleWheel("Bearer " + token, enabled)
            .enqueue(new Callback<LuckyWheelAdminApi.ToggleResponse>() {
                @Override
                public void onResponse(Call<LuckyWheelAdminApi.ToggleResponse> call, 
                                     Response<LuckyWheelAdminApi.ToggleResponse> response) {
                    layoutLoading.setVisibility(View.GONE);
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(LuckyWheelManagementActivity.this, 
                            response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        isWheelEnabled = response.body().isEnabled();
                    } else {
                        switchWheelEnabled.setChecked(!enabled);
                        Toast.makeText(LuckyWheelManagementActivity.this, 
                            "Lỗi: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<LuckyWheelAdminApi.ToggleResponse> call, Throwable t) {
                    layoutLoading.setVisibility(View.GONE);
                    switchWheelEnabled.setChecked(!enabled);
                    Toast.makeText(LuckyWheelManagementActivity.this, 
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void loadRewards() {
        String token = RetrofitClient.getInstance(this).getToken();
        layoutLoading.setVisibility(View.VISIBLE);
        
        adminApi.getRewards("Bearer " + token).enqueue(new Callback<List<LuckyWheelReward>>() {
            @Override
            public void onResponse(Call<List<LuckyWheelReward>> call, 
                                 Response<List<LuckyWheelReward>> response) {
                layoutLoading.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<LuckyWheelReward> rewards = response.body();
                    adapter.setRewards(rewards);
                } else {
                    Toast.makeText(LuckyWheelManagementActivity.this, 
                        "Lỗi: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<LuckyWheelReward>> call, Throwable t) {
                layoutLoading.setVisibility(View.GONE);
                Toast.makeText(LuckyWheelManagementActivity.this, 
                    "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditRewardDialog(LuckyWheelReward reward) {
        View dialogView = LayoutInflater.from(this)
            .inflate(R.layout.dialog_edit_reward, null);
        
        EditText etLabel = dialogView.findViewById(R.id.etLabel);
        EditText etCoinAmount = dialogView.findViewById(R.id.etCoinAmount);
        EditText etWeight = dialogView.findViewById(R.id.etWeight);
        
        etLabel.setText(reward.getLabel());
        etCoinAmount.setText(String.valueOf(reward.getCoinAmount()));
        etWeight.setText(String.valueOf(reward.getWeight()));
        
        new AlertDialog.Builder(this)
            .setTitle("Sửa phần thưởng")
            .setView(dialogView)
            .setPositiveButton("Lưu", (dialog, which) -> {
                try {
                    int coinAmount = Integer.parseInt(etCoinAmount.getText().toString());
                    
                    reward.setRewardType(coinAmount > 0 ? "COINS" : "NOTHING"); // Auto detect
                    reward.setCoinAmount(coinAmount);
                    reward.setWeight(Integer.parseInt(etWeight.getText().toString()));
                    reward.setLabel(etLabel.getText().toString());
                    reward.setIsActive(true); // Always active
                    
                    updateReward(reward);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Vui lòng nhập đúng định dạng số", 
                        Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void updateReward(LuckyWheelReward reward) {
        String token = RetrofitClient.getInstance(this).getToken();
        layoutLoading.setVisibility(View.VISIBLE);
        
        adminApi.updateReward("Bearer " + token, reward.getId(), reward)
            .enqueue(new Callback<LuckyWheelReward>() {
                @Override
                public void onResponse(Call<LuckyWheelReward> call, 
                                     Response<LuckyWheelReward> response) {
                    layoutLoading.setVisibility(View.GONE);
                    if (response.isSuccessful()) {
                        Toast.makeText(LuckyWheelManagementActivity.this, 
                            "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                        loadRewards();
                    } else {
                        Toast.makeText(LuckyWheelManagementActivity.this, 
                            "Lỗi: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<LuckyWheelReward> call, Throwable t) {
                    layoutLoading.setVisibility(View.GONE);
                    Toast.makeText(LuckyWheelManagementActivity.this, 
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }

    // Adapter
    private class RewardAdapter extends RecyclerView.Adapter<RewardAdapter.ViewHolder> {
        private List<LuckyWheelReward> rewards = new ArrayList<>();

        void setRewards(List<LuckyWheelReward> rewards) {
            this.rewards = rewards;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reward_manage, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            LuckyWheelReward reward = rewards.get(position);
            holder.bind(reward);
        }

        @Override
        public int getItemCount() {
            return rewards.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvLabel, tvCoinAmount, tvWeight, tvProbability;
            MaterialButton btnEdit;

            ViewHolder(View itemView) {
                super(itemView);
                tvLabel = itemView.findViewById(R.id.tvLabel);
                tvCoinAmount = itemView.findViewById(R.id.tvCoinAmount);
                tvWeight = itemView.findViewById(R.id.tvWeight);
                tvProbability = itemView.findViewById(R.id.tvProbability);
                btnEdit = itemView.findViewById(R.id.btnEdit);
            }

            void bind(LuckyWheelReward reward) {
                tvLabel.setText(reward.getLabel());
                tvCoinAmount.setText(reward.getCoinAmount() + " coins");
                tvWeight.setText("Trọng số: " + reward.getWeight());
                tvProbability.setText("Tỉ lệ: " + String.format("%.1f", reward.getProbability()) + "%");

                btnEdit.setOnClickListener(v -> showEditRewardDialog(reward));
            }
        }
    }
}