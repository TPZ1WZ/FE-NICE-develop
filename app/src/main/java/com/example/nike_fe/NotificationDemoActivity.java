package com.example.nike_fe;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity demo sử dụng NotificationBadgeView
 */
public class NotificationDemoActivity extends AppCompatActivity {

    private NotificationBadgeView notificationBadge;
    private Button btnIncrease;
    private Button btnDecrease;
    private Button btnReset;
    private Button btnSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_demo);

        // Khởi tạo views
        initViews();

        // Setup listeners
        setupListeners();
    }

    private void initViews() {
        notificationBadge = findViewById(R.id.notification_badge_view);
        btnIncrease = findViewById(R.id.btn_increase);
        btnDecrease = findViewById(R.id.btn_decrease);
        btnReset = findViewById(R.id.btn_reset);
        btnSet = findViewById(R.id.btn_set);
    }

    private void setupListeners() {
        // Click vào chuông
        notificationBadge.setBellClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(NotificationDemoActivity.this,
                        "Có " + notificationBadge.getNotifyCount() + " thông báo",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Tăng số thông báo
        btnIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notificationBadge.incrementCount();
            }
        });

        // Giảm số thông báo
        btnDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notificationBadge.decrementCount();
            }
        });

        // Reset về 0
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notificationBadge.resetCount();
            }
        });

        // Set số cụ thể
        btnSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set số random từ 0-150 để test
                int randomCount = (int) (Math.random() * 150);
                notificationBadge.setNotifyCount(randomCount);
                Toast.makeText(NotificationDemoActivity.this,
                        "Đã set " + randomCount + " thông báo",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Ví dụ: Gọi API và cập nhật số thông báo
     */
    private void fetchUnreadNotifications() {
        // TODO: Gọi API lấy số thông báo chưa đọc
        // Ví dụ:
        // apiService.getUnreadCount().enqueue(new Callback<UnreadResponse>() {
        //     @Override
        //     public void onResponse(Call<UnreadResponse> call, Response<UnreadResponse> response) {
        //         if (response.isSuccessful() && response.body() != null) {
        //             int unreadCount = response.body().getCount();
        //             notificationBadge.setNotifyCount(unreadCount);
        //         }
        //     }
        //     
        //     @Override
        //     public void onFailure(Call<UnreadResponse> call, Throwable t) {
        //         // Handle error
        //     }
        // });
    }
}
