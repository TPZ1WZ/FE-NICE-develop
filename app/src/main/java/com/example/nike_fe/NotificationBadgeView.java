package com.example.nike_fe;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Custom View để hiển thị icon chuông thông báo với badge số
 */
public class NotificationBadgeView extends FrameLayout {

    private ImageView bellIcon;
    private TextView badgeText;
    private int notificationCount = 0;

    public NotificationBadgeView(Context context) {
        super(context);
        init(context);
    }

    public NotificationBadgeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public NotificationBadgeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /**
     * Khởi tạo view
     */
    private void init(Context context) {
        // Inflate layout
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.notification_badge_layout, this, true);

        // Lấy references đến các view
        bellIcon = findViewById(R.id.notification_bell);
        badgeText = findViewById(R.id.notification_badge);

        // Mặc định ẩn badge
        badgeText.setVisibility(View.GONE);
    }

    /**
     * Cập nhật số lượng thông báo chưa đọc
     * @param count Số lượng thông báo
     */
    public void setNotifyCount(int count) {
        this.notificationCount = count;

        if (count > 0) {
            // Hiển thị badge
            badgeText.setVisibility(View.VISIBLE);
            
            // Hiển thị số, nếu > 99 thì hiển thị "99+"
            if (count > 99) {
                badgeText.setText("99+");
            } else {
                badgeText.setText(String.valueOf(count));
            }
        } else {
            // Ẩn badge khi không có thông báo
            badgeText.setVisibility(View.GONE);
        }
    }

    /**
     * Lấy số lượng thông báo hiện tại
     * @return Số lượng thông báo
     */
    public int getNotifyCount() {
        return notificationCount;
    }

    /**
     * Tăng số lượng thông báo thêm 1
     */
    public void incrementCount() {
        setNotifyCount(notificationCount + 1);
    }

    /**
     * Giảm số lượng thông báo đi 1
     */
    public void decrementCount() {
        if (notificationCount > 0) {
            setNotifyCount(notificationCount - 1);
        }
    }

    /**
     * Reset số lượng thông báo về 0
     */
    public void resetCount() {
        setNotifyCount(0);
    }

    /**
     * Set listener khi click vào icon chuông
     * @param listener OnClickListener
     */
    public void setBellClickListener(OnClickListener listener) {
        bellIcon.setOnClickListener(listener);
        this.setOnClickListener(listener);
    }
}
