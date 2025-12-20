package com.example.nike_fe.ui.notification;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nike_fe.R;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private ImageView ivBack;
    private RecyclerView rvNotifications;
    private TextView tvEmpty;
    private NotificationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        initViews();
        setupRecyclerView();
        loadNotifications();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        rvNotifications = findViewById(R.id.rvNotifications);
        tvEmpty = findViewById(R.id.tvEmpty);

        if (ivBack != null) {
            ivBack.setOnClickListener(v -> finish());
        }
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(this, new ArrayList<>());
        if (rvNotifications != null) {
            rvNotifications.setLayoutManager(new LinearLayoutManager(this));
            rvNotifications.setAdapter(adapter);
        }
    }

    private void loadNotifications() {
        // TODO: Load từ API khi backend có endpoint
        List<NotificationItem> notifications = getDummyNotifications();
        
        if (notifications.isEmpty()) {
            if (tvEmpty != null) tvEmpty.setVisibility(View.VISIBLE);
            if (rvNotifications != null) rvNotifications.setVisibility(View.GONE);
        } else {
            if (tvEmpty != null) tvEmpty.setVisibility(View.GONE);
            if (rvNotifications != null) rvNotifications.setVisibility(View.VISIBLE);
            adapter.setNotifications(notifications);
        }
    }

    private List<NotificationItem> getDummyNotifications() {
        List<NotificationItem> notifications = new ArrayList<>();
        
        notifications.add(new NotificationItem(
                "Đơn hàng đã được xác nhận",
                "Đơn hàng #12345 của bạn đã được xác nhận và đang được chuẩn bị.",
                "2 giờ trước",
                "order",
                false
        ));
        
        notifications.add(new NotificationItem(
                "Sản phẩm yêu thích đang giảm giá",
                "Nike Air Max 90 bạn đã yêu thích đang giảm giá 20%!",
                "1 ngày trước",
                "promotion",
                false
        ));
        
        notifications.add(new NotificationItem(
                "Đơn hàng đã giao thành công",
                "Đơn hàng #12340 đã được giao thành công.",
                "3 ngày trước",
                "delivery",
                true
        ));
        
        return notifications;
    }
}
