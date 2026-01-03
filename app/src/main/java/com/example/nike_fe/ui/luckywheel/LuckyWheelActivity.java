package com.example.nike_fe.ui.luckywheel;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nike_fe.R;
import com.example.nike_fe.data.api.LuckyWheelApi;
import com.example.nike_fe.data.api.RetrofitClient;
import com.example.nike_fe.data.model.Prize;
import com.example.nike_fe.data.model.SpinResponse;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * LuckyWheelActivity - Màn hình vòng quay may mắn
 */
public class LuckyWheelActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView tvRemainingSpins;
    private TextView tvEventDescription;
    private androidx.cardview.widget.CardView cardEventDescription;
    private LuckyWheelView wheelView;
    private Button btnSpin;
    private Button btnViewHistory;
    private ProgressBar progressBar;
    private RecyclerView rvPrizes;

    private LuckyWheelApi luckyWheelApi;
    private RetrofitClient retrofitClient;
    private PrizeAdapter prizeAdapter;
    private List<Prize> prizeList = new ArrayList<>();
    private int remainingSpins = 0;
    private boolean isSpinning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lucky_wheel);

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadData();
        setupListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvRemainingSpins = findViewById(R.id.tvRemainingSpins);
        tvEventDescription = findViewById(R.id.tvEventDescription);
        cardEventDescription = findViewById(R.id.cardEventDescription);
        wheelView = findViewById(R.id.wheelView);
        btnSpin = findViewById(R.id.btnSpin);
        btnViewHistory = findViewById(R.id.btnViewHistory);
        progressBar = findViewById(R.id.progressBar);
        rvPrizes = findViewById(R.id.rvPrizes);

        retrofitClient = RetrofitClient.getInstance(this);
        luckyWheelApi = retrofitClient.getLuckyWheelApi();
    }

    /**
     * Lấy token với định dạng "Bearer {token}"
     */
    private String getAuthToken() {
        String token = retrofitClient.getToken();
        android.util.Log.d("LuckyWheel",
                "Raw token: " + (token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "null"));

        if (token != null && !token.isEmpty()) {
            String authHeader = "Bearer " + token;
            android.util.Log.d("LuckyWheel",
                    "Auth header: " + authHeader.substring(0, Math.min(30, authHeader.length())) + "...");
            return authHeader;
        }
        android.util.Log.e("LuckyWheel", "Token is null or empty!");
        return null;
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        prizeAdapter = new PrizeAdapter(this, prizeList);
        rvPrizes.setLayoutManager(new LinearLayoutManager(this));
        rvPrizes.setAdapter(prizeAdapter);
    }

    private void loadData() {
        loadPrizes();
        loadRemainingSpins();
        loadWheelConfig();

        // Debug: Kiểm tra token
        String token = getAuthToken();
        if (token == null) {
            Toast.makeText(this,
                    "⚠️ Chưa đăng nhập - Chế độ Demo được kích hoạt",
                    Toast.LENGTH_SHORT).show();
        } else {
            android.util.Log.d("LuckyWheel", "User is logged in, token found");
        }
    }

    private void loadPrizes() {
        luckyWheelApi.getPrizes().enqueue(new Callback<List<Prize>>() {
            @Override
            public void onResponse(Call<List<Prize>> call, Response<List<Prize>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    prizeList.clear();
                    prizeList.addAll(response.body());
                    prizeAdapter.notifyDataSetChanged();

                    // Update WheelView items
                    if (!prizeList.isEmpty()) {
                        List<LuckyWheelView.WheelItem> wheelItems = new ArrayList<>();
                        for (Prize prize : prizeList) {
                            int color;
                            try {
                                color = android.graphics.Color.parseColor(prize.getColor());
                            } catch (Exception e) {
                                color = android.graphics.Color.parseColor("#FF6B6B"); // Default color
                            }
                            wheelItems.add(new LuckyWheelView.WheelItem(prize.getName(), color));
                        }
                        wheelView.setWheelItems(wheelItems);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Prize>> call, Throwable t) {
                // Vẫn giữ phần thưởng mặc định nếu không tải được từ API
                Toast.makeText(LuckyWheelActivity.this,
                        "Không thể tải danh sách phần thưởng từ server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadRemainingSpins() {
        String token = getAuthToken();
        if (token == null) {
            // Nếu chưa đăng nhập, cho 1 lượt demo
            remainingSpins = 1;
            updateRemainingSpinsText();
            return;
        }

        luckyWheelApi.getRemainingSpins(token).enqueue(new Callback<Map<String, Integer>>() {
            @Override
            public void onResponse(Call<Map<String, Integer>> call,
                    Response<Map<String, Integer>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    remainingSpins = response.body().get("remainingSpins");
                    updateRemainingSpinsText();
                } else {
                    // Nếu API lỗi, mặc định cho 1 lượt để test
                    remainingSpins = 1;
                    updateRemainingSpinsText();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Integer>> call, Throwable t) {
                // Nếu không kết nối được API, mặc định cho 1 lượt để test
                remainingSpins = 1;
                updateRemainingSpinsText();
            }
        });
    }

    private void loadWheelConfig() {
        luckyWheelApi.getPublicConfig().enqueue(new Callback<com.example.nike_fe.data.model.WheelConfig>() {
            @Override
            public void onResponse(Call<com.example.nike_fe.data.model.WheelConfig> call,
                    Response<com.example.nike_fe.data.model.WheelConfig> response) {
                if (response.isSuccessful() && response.body() != null) {
                    com.example.nike_fe.data.model.WheelConfig config = response.body();
                    
                    // Hiển thị description/thông báo nếu có
                    if (config.getDescription() != null && !config.getDescription().trim().isEmpty()) {
                        tvEventDescription.setText(config.getDescription());
                        cardEventDescription.setVisibility(android.view.View.VISIBLE);
                    } else {
                        cardEventDescription.setVisibility(android.view.View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<com.example.nike_fe.data.model.WheelConfig> call, Throwable t) {
                // Ẩn card nếu không load được config
                cardEventDescription.setVisibility(android.view.View.GONE);
            }
        });
    }

    private void updateRemainingSpinsText() {
        if (remainingSpins > 0) {
            tvRemainingSpins.setText("Bạn còn " + remainingSpins + " lượt quay hôm nay");
            tvRemainingSpins.setTextColor(android.graphics.Color.parseColor("#6B7280"));
            btnSpin.setEnabled(true);
        } else {
            tvRemainingSpins.setText("⚠️ Bạn đã hết lượt quay hôm nay. Quay lại vào ngày mai!");
            tvRemainingSpins.setTextColor(android.graphics.Color.parseColor("#EF4444"));
            btnSpin.setEnabled(false);
        }
    }

    private void setupListeners() {
        btnSpin.setOnClickListener(v -> spin());
        btnViewHistory.setOnClickListener(v -> {
            String token = getAuthToken();
            if (token == null) {
                Toast.makeText(this,
                        "Vui lòng đăng nhập để xem lịch sử phần thưởng",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            // Mở màn hình lịch sử phần thưởng
            Intent intent = new Intent(this, SpinHistoryActivity.class);
            startActivity(intent);
        });
    }

    private void spin() {
        if (isSpinning || remainingSpins <= 0) {
            return;
        }

        isSpinning = true;
        btnSpin.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        // Kiểm tra token
        String token = getAuthToken();
        if (token == null) {
            // Không có token, chạy demo mode
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this,
                    "⚠️ Chạy Demo Mode (Vui lòng đăng nhập để quay thật)",
                    Toast.LENGTH_LONG).show();
            runDemoSpin();
            return;
        }

        // Gọi API với token
        luckyWheelApi.spin(token).enqueue(new Callback<SpinResponse>() {
            @Override
            public void onResponse(Call<SpinResponse> call, Response<SpinResponse> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    SpinResponse spinResponse = response.body();
                    Prize prize = spinResponse.getPrize();

                    // Tìm vị trí phần thưởng trong danh sách
                    int prizePosition = findPrizePosition(prize);
                    if (prizePosition == -1) {
                        // Fallback fallback if not found
                        prizePosition = 0;
                    }

                    // Animation quay với hiệu ứng chậm dần
                    wheelView.spinToPosition(prizePosition, () -> {
                        // Callback khi quay xong
                        isSpinning = false;
                        remainingSpins--;
                        if (remainingSpins < 0)
                            remainingSpins = 0;
                        updateRemainingSpinsText();
                        showResultDialog(spinResponse);
                    });
                } else {
                    isSpinning = false;
                    btnSpin.setEnabled(remainingSpins > 0);

                    // Parse error response để kiểm tra message
                    String errorMsg = "Có lỗi xảy ra";
                    boolean isAuthError = false;

                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            android.util.Log.e("LuckyWheel", "Error response: " + errorBody);

                            // Parse JSON để lấy message
                            if (errorBody.contains("\"message\"")) {
                                int start = errorBody.indexOf("\"message\":\"") + 11;
                                int end = errorBody.indexOf("\"", start);
                                if (start > 10 && end > start) {
                                    errorMsg = errorBody.substring(start, end);
                                }
                            }

                            // Chỉ chạy demo mode nếu là lỗi authentication
                            isAuthError = errorMsg.contains("not authenticated") ||
                                    errorMsg.contains("Unauthorized") ||
                                    response.code() == 401;
                        }
                    } catch (Exception e) {
                        android.util.Log.e("LuckyWheel", "Error parsing response", e);
                    }

                    if (isAuthError) {
                        // Lỗi authentication -> chạy demo mode
                        Toast.makeText(LuckyWheelActivity.this,
                                "⚠️ Chạy Demo Mode (Vui lòng đăng nhập để quay thật)",
                                Toast.LENGTH_LONG).show();
                        runDemoSpin();
                    } else {
                        // Lỗi khác (không có phần thưởng, hết lượt, etc.)
                        Toast.makeText(LuckyWheelActivity.this,
                                "❌ " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<SpinResponse> call, Throwable t) {
                // Nếu API lỗi, chạy demo mode
                progressBar.setVisibility(View.GONE);
                runDemoSpin();
            }
        });
    }

    /**
     * Chạy demo mode khi không kết nối được API
     */
    private void runDemoSpin() {
        // Random một phần thưởng
        int randomPosition = new Random().nextInt(6);

        wheelView.spinToPosition(randomPosition, () -> {
            isSpinning = false;
            remainingSpins--;
            if (remainingSpins < 0)
                remainingSpins = 0;
            updateRemainingSpinsText();

            // Tạo SpinResponse giả để hiển thị kết quả
            SpinResponse demoResponse = new SpinResponse();
            Prize demoPrize = new Prize();

            switch (randomPosition) {
                case 0:
                    demoPrize.setName("Giảm 10%");
                    demoPrize.setType("COUPON");
                    demoResponse.setPrizeCode("DEMO10OFF");
                    break;
                case 1:
                    demoPrize.setName("Giảm 20%");
                    demoPrize.setType("COUPON");
                    demoResponse.setPrizeCode("DEMO20OFF");
                    break;
                case 2:
                    demoPrize.setName("Freeship");
                    demoPrize.setType("COUPON");
                    demoResponse.setPrizeCode("DEMOFREESHIP");
                    break;
                case 3:
                    demoPrize.setName("50 Điểm thưởng");
                    demoPrize.setType("POINTS");
                    demoPrize.setPointsValue(50);
                    break;
                case 4:
                    demoPrize.setName("Quà tặng đặc biệt");
                    demoPrize.setType("GIFT");
                    demoResponse.setPrizeCode("DEMOGIFT");
                    break;
                default:
                    demoPrize.setName("Chúc bạn may mắn lần sau!");
                    demoPrize.setType("NOTHING");
                    break;
            }

            demoResponse.setPrize(demoPrize);
            demoResponse.setSuccess(true);
            demoResponse.setMessage("Demo Mode - Vui lòng đăng nhập để nhận thưởng thật");

            showResultDialog(demoResponse);
        });
    }

    private int findPrizePosition(Prize wonPrize) {
        if (prizeList == null || prizeList.isEmpty())
            return -1;
        for (int i = 0; i < prizeList.size(); i++) {
            // Compare by ID if available, otherwise name
            if (prizeList.get(i).getId() != null && wonPrize.getId() != null) {
                if (prizeList.get(i).getId().equals(wonPrize.getId())) {
                    return i;
                }
            } else if (prizeList.get(i).getName().equals(wonPrize.getName())) {
                return i;
            }
        }
        return -1;
    }

    private void showResultDialog(SpinResponse spinResponse) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_spin_result);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(false);

        TextView tvResultIcon = dialog.findViewById(R.id.tvResultIcon);
        TextView tvTitle = dialog.findViewById(R.id.tvResultTitle);
        TextView tvPrizeName = dialog.findViewById(R.id.tvPrizeName);
        TextView tvPrizeCode = dialog.findViewById(R.id.tvPrizeCode);
        LinearLayout layoutPrizeCode = dialog.findViewById(R.id.layoutPrizeCode);
        Button btnUseNow = dialog.findViewById(R.id.btnUseNow);
        Button btnSaveToWallet = dialog.findViewById(R.id.btnSaveToWallet);
        Button btnClose = dialog.findViewById(R.id.btnClose);

        Prize prize = spinResponse.getPrize();
        boolean isDemoMode = spinResponse.getMessage() != null &&
                spinResponse.getMessage().contains("Demo Mode");

        if ("NOTHING".equals(prize.getType())) {
            // Không trúng gì
            tvResultIcon.setText("😢");
            tvTitle.setText("Chúc bạn may mắn lần sau!");
            tvPrizeName.setText(prize.getName());
            layoutPrizeCode.setVisibility(View.GONE);
            btnUseNow.setVisibility(View.GONE);
            btnSaveToWallet.setVisibility(View.GONE);
            btnClose.setVisibility(View.VISIBLE);

            btnClose.setOnClickListener(v -> dialog.dismiss());
        } else {
            // Trúng thưởng
            tvResultIcon.setText("🎉");

            if (isDemoMode) {
                tvTitle.setText("🎮 Demo Mode");
                tvPrizeName.setText("Bạn đã quay được: " + prize.getName() +
                        "\n\n⚠️ Đây chỉ là demo. Vui lòng đăng nhập để nhận thưởng thật!");
            } else {
                tvTitle.setText("Chúc mừng bạn!");
                tvPrizeName.setText("Bạn đã trúng: " + prize.getName());
            }

            if (spinResponse.getPrizeCode() != null) {
                layoutPrizeCode.setVisibility(View.VISIBLE);
                tvPrizeCode.setText(spinResponse.getPrizeCode());

                if (isDemoMode) {
                    tvPrizeCode.setText(spinResponse.getPrizeCode() + " (DEMO)");
                }
            } else {
                layoutPrizeCode.setVisibility(View.GONE);
            }

            if (isDemoMode) {
                // Demo mode: chỉ hiển thị nút đóng
                btnUseNow.setVisibility(View.GONE);
                btnSaveToWallet.setVisibility(View.GONE);
                btnClose.setVisibility(View.VISIBLE);
                btnClose.setText("Đóng");
                btnClose.setOnClickListener(v -> {
                    dialog.dismiss();
                    Toast.makeText(this,
                            "💡 Tip: Đăng nhập để nhận thưởng thật từ vòng quay!",
                            Toast.LENGTH_LONG).show();
                });
            } else {
                // Real mode: hiển thị các nút hành động
                btnUseNow.setVisibility(View.VISIBLE);
                btnSaveToWallet.setVisibility(View.VISIBLE);
                btnClose.setVisibility(View.GONE);

                btnUseNow.setOnClickListener(v -> {
                    dialog.dismiss();
                    // TODO: Chuyển đến màn hình sử dụng voucher
                    Toast.makeText(this, "Đang áp dụng voucher...", Toast.LENGTH_SHORT).show();
                });

                btnSaveToWallet.setOnClickListener(v -> {
                    dialog.dismiss();
                    Toast.makeText(this, "Đã lưu vào ví voucher!", Toast.LENGTH_SHORT).show();
                });
            }
        }

        // Thêm hiệu ứng rung cho dialog
        dialog.getWindow().getDecorView().animate()
                .scaleX(0.9f).scaleY(0.9f).setDuration(0)
                .withEndAction(() -> {
                    dialog.getWindow().getDecorView().animate()
                            .scaleX(1f).scaleY(1f).setDuration(200).start();
                });

        dialog.show();
    }
}
