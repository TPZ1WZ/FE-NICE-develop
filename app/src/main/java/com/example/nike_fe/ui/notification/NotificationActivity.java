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

import android.content.Intent;
import com.example.nike_fe.ui.order.OrderDetailActivity;
import com.example.nike_fe.ui.product.ProductDetailActivity;

public class NotificationActivity extends AppCompatActivity implements NotificationAdapter.OnNotificationClickListener {

    private ImageView ivBack;
    private RecyclerView rvNotifications;
    private TextView tvEmpty;
    private NotificationAdapter adapter;
    private com.example.nike_fe.data.api.NotificationApi notificationApi;
    private String token;

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

        com.example.nike_fe.data.api.RetrofitClient retrofitClient = com.example.nike_fe.data.api.RetrofitClient
                .getInstance(this);
        notificationApi = retrofitClient.getNotificationApi();
        token = retrofitClient.getToken();

        if (ivBack != null) {
            ivBack.setOnClickListener(v -> finish());
        }
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(this, new ArrayList<>(), this);
        if (rvNotifications != null) {
            rvNotifications.setLayoutManager(new LinearLayoutManager(this));
            rvNotifications.setAdapter(adapter);
        }
    }

    private void loadNotifications() {
        if (token == null) {
            loadDummyNotifications();
            return;
        }

        notificationApi.getNotifications("Bearer " + token, 0, 50)
                .enqueue(new retrofit2.Callback<com.example.nike_fe.data.model.NotificationResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.example.nike_fe.data.model.NotificationResponse> call,
                            retrofit2.Response<com.example.nike_fe.data.model.NotificationResponse> response) {
                        List<NotificationItem> notifications = new ArrayList<>();

                        if (response.isSuccessful() && response.body() != null
                                && response.body().getContent() != null) {
                            for (com.example.nike_fe.data.model.Notification notif : response.body().getContent()) {
                                notifications.add(new NotificationItem(
                                        notif.getId(),
                                        notif.getTitle(),
                                        notif.getMessage(),
                                        formatDate(notif.getCreatedAt()),
                                        notif.getType() != null ? notif.getType().toLowerCase() : "system",
                                        notif.getIsRead() != null && notif.getIsRead(),
                                        notif.getData())); // Pass data map here
                            }
                        }

                        displayNotifications(notifications);
                    }

                    @Override
                    public void onFailure(retrofit2.Call<com.example.nike_fe.data.model.NotificationResponse> call,
                            Throwable t) {
                        // On failure, maybe show empty or cached?
                        // For now, just show empty
                        displayNotifications(new ArrayList<>());
                    }
                });
    }

    // Removed fetchCoupons and finalizeNotifications as they are no longer needed.

    private String formatDate(String dateStr) {
        // Simple helper or reuse existing util if accessible.
        // For notification list, we might want relative time "2 hours ago", but for now
        // just pass string or simplified.
        if (dateStr == null)
            return "Vừa xong";
        try {
            // Very basic validation/shortening
            if (dateStr.length() > 10)
                return dateStr.substring(0, 10);
            return dateStr;
        } catch (Exception e) {
            return "Vừa xong";
        }
    }

    private void displayNotifications(List<NotificationItem> notifications) {
        if (notifications == null || notifications.isEmpty()) {
            if (tvEmpty != null)
                tvEmpty.setVisibility(View.VISIBLE);
            if (rvNotifications != null)
                rvNotifications.setVisibility(View.GONE);
        } else {
            if (tvEmpty != null)
                tvEmpty.setVisibility(View.GONE);
            if (rvNotifications != null)
                rvNotifications.setVisibility(View.VISIBLE);

            adapter = new NotificationAdapter(this, notifications, this);
            if (rvNotifications != null)
                rvNotifications.setAdapter(adapter);
        }
    }

    private void loadDummyNotifications() {
        List<NotificationItem> notifications = getDummyNotifications();
        displayNotifications(notifications);
    }

    // Helper to translate status
    private String getStatusText(String status) {
        if (status == null)
            return "Không xác định";
        switch (status) {
            case "PENDING":
                return "Chờ xác nhận";
            case "CONFIRMED":
                return "Đã xác nhận";
            case "SHIPPING":
                return "Đang giao";
            case "COMPLETED":
                return "Hoàn thành";
            case "CANCELLED":
                return "Đã hủy";
            default:
                return status;
        }
    }

    private List<NotificationItem> getDummyNotifications() {
        List<NotificationItem> notifications = new ArrayList<>();
        // Dummy data preserved but not used when logged in
        return notifications;
    }

    @Override
    public void onNotificationClick(NotificationItem notification) {
        // Mark as read (update UI)
        if (!notification.isRead()) {
            notification.setRead(true);
            adapter.notifyDataSetChanged();

            // Mark as read (API)
            if (token != null && notification.getId() != null) {
                notificationApi.markAsRead("Bearer " + token, notification.getId())
                        .enqueue(new retrofit2.Callback<java.util.Map<String, String>>() {
                            @Override
                            public void onResponse(retrofit2.Call<java.util.Map<String, String>> call,
                                    retrofit2.Response<java.util.Map<String, String>> response) {
                                // Successfully marked as read
                            }

                            @Override
                            public void onFailure(retrofit2.Call<java.util.Map<String, String>> call, Throwable t) {
                                // Ignore error
                            }
                        });
            }
        }

        // Navigate based on type
        String type = notification.getType();
        Intent intent = null;
        java.util.Map<String, Object> data = notification.getData();
        android.util.Log.d("NotificationActivity",
                "Clicked notification: " + notification.getTitle() + ", Type: " + type);

        if (type.equalsIgnoreCase("order") || type.startsWith("order")) {
            long orderId = -1L;
            if (data != null) {
                if (data.containsKey("order_id")) {
                    Object obj = data.get("order_id");
                    if (obj instanceof Number) {
                        orderId = ((Number) obj).longValue();
                    } else if (obj instanceof String) {
                        try {
                            orderId = Long.parseLong((String) obj);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            // Fallback: Extract from Message or Title if data failed
            if (orderId == -1L) {
                orderId = extractIdFromText(notification.getMessage());
                if (orderId == -1L) {
                    orderId = extractIdFromText(notification.getTitle());
                }
            }

            if (orderId != -1L) {
                intent = new Intent(this, com.example.nike_fe.ui.order.OrderDetailActivity.class);
                intent.putExtra("order_id", orderId);
                startActivity(intent);
            } else {
                android.widget.Toast
                        .makeText(this, "Không tìm thấy thông tin đơn hàng", android.widget.Toast.LENGTH_SHORT).show();
            }

        } else if (type.equalsIgnoreCase("coupon") || type.startsWith("promotion")) {
            // It's a coupon, extract code and copy to clipboard
            String code = "";
            if (data != null) {
                if (data.containsKey("couponCode")) {
                    code = String.valueOf(data.get("couponCode"));
                } else if (data.containsKey("coupon_code")) {
                    code = String.valueOf(data.get("coupon_code"));
                }
            }

            // Fallback: extract from title/message
            if (code.isEmpty()) {
                // Try to extract after "Mã giảm giá mới: " or similar
                // Example format: "Mã giảm giá mới: tuankiet"
                String combinedText = (notification.getTitle() + " " + notification.getMessage()).toLowerCase();
                String prefix = "mã giảm giá mới: ";
                int index = combinedText.indexOf(prefix);
                if (index != -1) {
                    String sub = combinedText.substring(index + prefix.length()).trim();
                    // Take the first word
                    String[] parts = sub.split("\\s+");
                    if (parts.length > 0) {
                        code = parts[0];
                    }
                }

                // Fallback 2: look for just "Code: " or "Mã: "
                if (code.isEmpty()) {
                    java.util.regex.Pattern p = java.util.regex.Pattern.compile("(?i)(?:code|mã)[:\\s]+([a-zA-Z0-9]+)");
                    java.util.regex.Matcher m = p.matcher(notification.getTitle() + " " + notification.getMessage());
                    if (m.find()) {
                        code = m.group(1);
                    }
                }
            }

            if (!code.isEmpty()) {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(
                        android.content.Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("Coupon Code", code);
                clipboard.setPrimaryClip(clip);
                android.widget.Toast.makeText(this, "Đã sao chép mã: " + code, android.widget.Toast.LENGTH_SHORT)
                        .show();
            } else {
                android.widget.Toast.makeText(this, "Không tìm thấy mã giảm giá", android.widget.Toast.LENGTH_SHORT)
                        .show();
            }
        } else if (type.startsWith("product")) {
            long productId = 1L;
            if (type.contains(":")) {
                try {
                    productId = Long.parseLong(type.split(":")[1]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            intent = new Intent(this, ProductDetailActivity.class);
            intent.putExtra("product_id", productId);
            startActivity(intent);
        }
    }

    private long extractIdFromText(String text) {
        if (text == null || text.isEmpty())
            return -1L;
        // Regex to find # followed by digits
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("#(\\d+)");
        java.util.regex.Matcher m = p.matcher(text);
        if (m.find()) {
            try {
                return Long.parseLong(m.group(1));
            } catch (NumberFormatException e) {
                return -1L;
            }
        }
        return -1L;
    }
}
