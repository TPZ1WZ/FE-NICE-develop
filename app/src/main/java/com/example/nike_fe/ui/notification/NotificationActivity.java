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
    private com.example.nike_fe.data.api.OrderApi orderApi;
    private com.example.nike_fe.data.api.UserCouponApi userCouponApi;
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
        orderApi = retrofitClient.getOrderApi();
        userCouponApi = retrofitClient.getUserCouponApi();
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

        orderApi.getUserOrders("Bearer " + token)
                .enqueue(new retrofit2.Callback<List<com.example.nike_fe.data.model.Order>>() {
                    @Override
                    public void onResponse(retrofit2.Call<List<com.example.nike_fe.data.model.Order>> call,
                            retrofit2.Response<List<com.example.nike_fe.data.model.Order>> response) {
                        List<NotificationItem> notifications = new ArrayList<>();

                        if (response.isSuccessful() && response.body() != null) {
                            for (com.example.nike_fe.data.model.Order order : response.body()) {
                                String title = "Đơn hàng #" + order.getId() + " - " + getStatusText(order.getStatus());
                                String message = "Đơn hàng của bạn đang ở trạng thái: "
                                        + getStatusText(order.getStatus());

                                notifications.add(new NotificationItem(
                                        title,
                                        message,
                                        formatDate(order.getCreatedAt()), // Use helper if available or simple text
                                        "order:" + order.getId(),
                                        false));
                            }
                        }
                        // Continue to fetch coupons
                        fetchCoupons(notifications);
                    }

                    @Override
                    public void onFailure(retrofit2.Call<List<com.example.nike_fe.data.model.Order>> call,
                            Throwable t) {
                        // Even if orders fail, try coupons
                        fetchCoupons(new ArrayList<>());
                    }
                });
    }

    private void fetchCoupons(List<NotificationItem> currentNotifications) {
        userCouponApi.getValidCoupons("Bearer " + token)
                .enqueue(new retrofit2.Callback<List<com.example.nike_fe.data.model.Coupon>>() {
                    @Override
                    public void onResponse(retrofit2.Call<List<com.example.nike_fe.data.model.Coupon>> call,
                            retrofit2.Response<List<com.example.nike_fe.data.model.Coupon>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            for (com.example.nike_fe.data.model.Coupon coupon : response.body()) {
                                String title = "Mã giảm giá mới: " + coupon.getCode();
                                String message = coupon.getDescription() + ". HSD: " + coupon.getEndDate();

                                currentNotifications.add(new NotificationItem(
                                        title,
                                        message,
                                        "Mới",
                                        "promotion:" + coupon.getCode(), // Hack for click
                                        false));
                            }
                        }
                        finalizeNotifications(currentNotifications);
                    }

                    @Override
                    public void onFailure(retrofit2.Call<List<com.example.nike_fe.data.model.Coupon>> call,
                            Throwable t) {
                        finalizeNotifications(currentNotifications);
                    }
                });
    }

    private void finalizeNotifications(List<NotificationItem> notifications) {
        if (notifications.isEmpty()) {
            loadDummyNotifications();
        } else {
            displayNotifications(notifications);
        }
    }

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

    private void displayNotifications(List<NotificationItem> items) {
        if (items.isEmpty()) {
            if (tvEmpty != null)
                tvEmpty.setVisibility(View.VISIBLE);
            if (rvNotifications != null)
                rvNotifications.setVisibility(View.GONE);
        } else {
            if (tvEmpty != null)
                tvEmpty.setVisibility(View.GONE);
            if (rvNotifications != null)
                rvNotifications.setVisibility(View.VISIBLE);
            adapter.setNotifications(items);
        }
    }

    private void loadDummyNotifications() {
        List<NotificationItem> notifications = getDummyNotifications();
        displayNotifications(notifications);
    }

    // Helper to translate status
    private String getStatusText(String status) {
        if (status == null)
            return "";
        switch (status.toUpperCase()) {
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

        notifications.add(new NotificationItem(
                "Đơn hàng đã được xác nhận",
                "Đơn hàng #12345 của bạn đã được xác nhận và đang được chuẩn bị.",
                "2 giờ trước",
                "order",
                false));

        notifications.add(new NotificationItem(
                "Sản phẩm yêu thích đang giảm giá",
                "Nike Air Max 90 bạn đã yêu thích đang giảm giá 20%!",
                "1 ngày trước",
                "promotion",
                false));

        notifications.add(new NotificationItem(
                "Đơn hàng đã giao thành công",
                "Đơn hàng #12340 đã được giao thành công.",
                "3 ngày trước",
                "delivery",
                true));

        return notifications;
    }

    @Override
    public void onNotificationClick(NotificationItem notification) {
        // Mark as read (update UI)
        notification.setRead(true);
        adapter.notifyDataSetChanged();

        // Navigate based on type
        String type = notification.getType();
        Intent intent = null;

        if (type.startsWith("order") || type.startsWith("delivery")) {
            long orderId = 1L; // Default dummy
            if (type.contains(":")) {
                try {
                    orderId = Long.parseLong(type.split(":")[1]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            intent = new Intent(this, OrderDetailActivity.class);
            intent.putExtra("order_id", orderId);
            startActivity(intent);
        } else if (type.startsWith("promotion")) {
            // It's a coupon, extract code and copy to clipboard
            String code = "";
            if (type.contains(":")) {
                code = type.split(":")[1];
            }

            if (!code.isEmpty()) {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(
                        android.content.Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("Coupon Code", code);
                clipboard.setPrimaryClip(clip);
                android.widget.Toast.makeText(this, "Đã sao chép mã: " + code, android.widget.Toast.LENGTH_SHORT)
                        .show();
            }

            // Optionally navigate to cart or stay
            // intent = new Intent(this, com.example.nike_fe.ui.cart.CartActivity.class);
            // startActivity(intent);

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
}
