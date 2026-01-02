package com.example.nike_fe;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nike_fe.data.api.NotificationApi;
import com.example.nike_fe.data.api.RetrofitClient;
import com.example.nike_fe.data.model.UnreadCountResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity test để kiểm tra chức năng thông báo
 */
public class NotificationTestActivity extends AppCompatActivity {

    private NotificationBadgeView badgeView;
    private Button btnTestApi, btnSimulate;
    private NotificationApi notificationApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_test);

        badgeView = findViewById(R.id.notification_badge_test);
        btnTestApi = findViewById(R.id.btn_test_api);
        btnSimulate = findViewById(R.id.btn_simulate);

        notificationApi = RetrofitClient.getInstance(this).getNotificationApi();

        setupListeners();
    }

    private void setupListeners() {
        // Test gọi API thật
        btnTestApi.setOnClickListener(v -> testRealApi());

        // Simulate local data
        btnSimulate.setOnClickListener(v -> {
            int randomCount = (int) (Math.random() * 20);
            badgeView.setNotifyCount(randomCount);
            Toast.makeText(this, "Simulate: " + randomCount + " thông báo", Toast.LENGTH_SHORT).show();
        });

        // Click badge
        badgeView.setBellClickListener(v -> {
            Toast.makeText(this, 
                "Badge clicked! Count: " + badgeView.getNotifyCount(), 
                Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Test gọi API thật để lấy số thông báo chưa đọc
     */
    private void testRealApi() {
        String token = RetrofitClient.getInstance(this).getToken();
        
        if (token == null) {
            Toast.makeText(this, "Chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Đang gọi API...", Toast.LENGTH_SHORT).show();

        notificationApi.getUnreadCount("Bearer " + token)
            .enqueue(new Callback<UnreadCountResponse>() {
                @Override
                public void onResponse(Call<UnreadCountResponse> call, Response<UnreadCountResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        long count = response.body().getCount();
                        badgeView.setNotifyCount((int) count);
                        Toast.makeText(NotificationTestActivity.this, 
                            "✅ API Success: " + count + " thông báo chưa đọc", 
                            Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(NotificationTestActivity.this, 
                            "❌ API Error: " + response.code(), 
                            Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<UnreadCountResponse> call, Throwable t) {
                    Toast.makeText(NotificationTestActivity.this, 
                        "❌ Network Error: " + t.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                }
            });
    }
}
