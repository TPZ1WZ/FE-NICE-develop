# 🔔 Hướng Dẫn Test Notification Badge

## ✅ Hệ thống đã có sẵn

Ứng dụng của bạn **ĐÃ CÓ** chức năng badge thông báo hoạt động trong `MainActivity`:

### Trong MainActivity.java:

```java
private void updateUnreadCount() {
    String token = RetrofitClient.getInstance(this).getToken();
    if (token == null) {
        tvNotificationBadge.setVisibility(View.GONE);
        return;
    }

    // Gọi API: GET /api/notifications/count-unread
    RetrofitClient.getInstance(this).getNotificationApi().getUnreadCount()
        .enqueue(new Callback<UnreadCountResponse>() {
            @Override
            public void onResponse(...) {
                long count = response.body().getCount();
                if (count > 0) {
                    tvNotificationBadge.setText(String.valueOf(count));
                    tvNotificationBadge.setVisibility(View.VISIBLE);
                } else {
                    tvNotificationBadge.setVisibility(View.GONE);
                }
            }
        });
}
```

**Hàm này được gọi tự động trong `onResume()`** - nghĩa là mỗi khi bạn vào MainActivity, badge sẽ tự động cập nhật!

---

## 🧪 Cách Test

### Option 1: Test trực tiếp trên MainActivity
1. Đăng nhập vào app
2. Vào màn hình chính (MainActivity)
3. Xem góc trên bên phải - icon chuông
4. Badge màu đỏ sẽ hiển thị số thông báo chưa đọc (nếu có)

### Option 2: Dùng Activity Test riêng
Tôi đã tạo `NotificationTestActivity` để test riêng:

```java
// Thêm vào AndroidManifest.xml (trong thẻ <application>):
<activity
    android:name=".NotificationTestActivity"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
```

Hoặc start từ MainActivity:
```java
startActivity(new Intent(this, NotificationTestActivity.class));
```

---

## 🔍 Kiểm tra Backend

Badge lấy dữ liệu từ API này:

```
GET /api/notifications/count-unread
Header: Authorization: Bearer <token>

Response:
{
    "count": 5
}
```

### Test bằng Postman/cURL:

```bash
curl -X GET http://localhost:8080/api/notifications/count-unread \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

---

## 🐛 Troubleshooting

### Badge không hiện?

1. **Kiểm tra đã đăng nhập chưa:**
   - Badge chỉ hiện khi có token
   - Kiểm tra: `RetrofitClient.getInstance(this).getToken()`

2. **Kiểm tra API có trả về đúng không:**
   - Xem Logcat để thấy response
   - Thêm log: `Log.d("Badge", "Count: " + count);`

3. **Kiểm tra trong code:**
   ```java
   @Override
   protected void onResume() {
       super.onResume();
       updateUnreadCount(); // ← Dòng này có được gọi không?
   }
   ```

4. **Kiểm tra layout:**
   - File: `activity_main.xml`
   - ID: `tvNotificationBadge`
   - Đảm bảo có `android:visibility="gone"` ban đầu

---

## 📱 Custom Badge View (Nâng cao)

Nếu muốn dùng custom view `NotificationBadgeView`:

### Trong layout:
```xml
<com.example.nike_fe.NotificationBadgeView
    android:id="@+id/custom_badge"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content" />
```

### Trong Java:
```java
NotificationBadgeView badge = findViewById(R.id.custom_badge);

// Set số thông báo
badge.setNotifyCount(5);

// Click listener
badge.setBellClickListener(v -> {
    Intent intent = new Intent(this, NotificationActivity.class);
    startActivity(intent);
});
```

---

## 🎯 Tóm tắt

✅ MainActivity **ĐÃ CÓ** badge hoạt động  
✅ Badge tự động update mỗi khi vào app  
✅ API endpoint: `/api/notifications/count-unread`  
✅ Badge tự động ẩn khi count = 0  
✅ Custom view `NotificationBadgeView` đã tạo sẵn nếu cần dùng chỗ khác  

**Chỉ cần chạy app và đăng nhập là badge sẽ hoạt động!** 🎉
