package com.example.nike_fe.ui.luckywheel;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom View cho vòng quay may mắn với 6 phần thưởng
 */
public class LuckyWheelView extends View {

    private Paint paint;
    private Paint textPaint;
    private RectF rectF;
    private List<WheelItem> wheelItems;
    private float currentRotation = 0f;
    private int[] colors = {
            Color.parseColor("#FF6B6B"),
            Color.parseColor("#4ECDC4"),
            Color.parseColor("#FFE66D"),
            Color.parseColor("#95E1D3"),
            Color.parseColor("#F38181"),
            Color.parseColor("#AA96DA")
    };

    public LuckyWheelView(Context context) {
        super(context);
        init();
    }

    public LuckyWheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(40f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);
        textPaint.setShadowLayer(4f, 2f, 2f, Color.BLACK);

        rectF = new RectF();
        wheelItems = new ArrayList<>();

        // Thêm 6 phần thưởng mặc định
        wheelItems.add(new WheelItem("Giảm 10%", colors[0]));
        wheelItems.add(new WheelItem("Giảm 20%", colors[1]));
        wheelItems.add(new WheelItem("Freeship", colors[2]));
        wheelItems.add(new WheelItem("Điểm thưởng", colors[3]));
        wheelItems.add(new WheelItem("Quà tặng", colors[4]));
        wheelItems.add(new WheelItem("Chúc may mắn", colors[5]));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int radius = Math.min(width, height) / 2 - 20;
        int centerX = width / 2;
        int centerY = height / 2;

        rectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);

        if (wheelItems == null || wheelItems.isEmpty()) {
            return;
        }

        float sweepAngle = 360f / wheelItems.size();

        // Vẽ từng phần của vòng quay
        for (int i = 0; i < wheelItems.size(); i++) {
            float startAngle = sweepAngle * i + currentRotation;

            paint.setColor(wheelItems.get(i).color);
            canvas.drawArc(rectF, startAngle, sweepAngle, true, paint);

            // Vẽ viền trắng
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3);
            canvas.drawArc(rectF, startAngle, sweepAngle, true, paint);
            paint.setStyle(Paint.Style.FILL);

            // Vẽ text
            float angle = startAngle + sweepAngle / 2;
            float textRadius = radius * 0.7f;

            canvas.save();
            canvas.rotate(angle + 90, centerX, centerY);

            String text = wheelItems.get(i).text;
            // Chia text thành nhiều dòng nếu quá dài
            String[] lines = text.split(" ");
            if (lines.length > 2) {
                canvas.drawText(lines[0] + " " + lines[1], centerX, centerY - textRadius + 20, textPaint);
                if (lines.length > 2) {
                    canvas.drawText(lines[2], centerX, centerY - textRadius + 50, textPaint);
                }
            } else if (lines.length == 2) {
                canvas.drawText(lines[0], centerX, centerY - textRadius + 20, textPaint);
                canvas.drawText(lines[1], centerX, centerY - textRadius + 50, textPaint);
            } else {
                canvas.drawText(text, centerX, centerY - textRadius + 35, textPaint);
            }

            canvas.restore();
        }

        // Vẽ vòng tròn giữa
        paint.setColor(Color.parseColor("#1E88E5"));
        canvas.drawCircle(centerX, centerY, radius * 0.15f, paint);
    }

    public void setWheelItems(List<WheelItem> items) {
        this.wheelItems = items;
        invalidate();
    }

    public void spinToPosition(int position, SpinListener listener) {
        if (wheelItems == null || wheelItems.isEmpty()) {
            if (listener != null)
                listener.onSpinComplete();
            return;
        }

        float sweepAngle = 360f / wheelItems.size();
        // Tính góc cần quay để dừng ở vị trí mong muốn
        float targetAngle = 360f - (sweepAngle * position + sweepAngle / 2);
        // Thêm nhiều vòng quay để tạo hiệu ứng
        float totalRotation = targetAngle + 360f * 5;

        ValueAnimator animator = ValueAnimator.ofFloat(currentRotation, currentRotation + totalRotation);
        animator.setDuration(4000);
        animator.setInterpolator(new DecelerateInterpolator());

        animator.addUpdateListener(animation -> {
            currentRotation = (float) animation.getAnimatedValue() % 360;
            invalidate();
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (listener != null) {
                    listener.onSpinComplete();
                }
            }
        });

        animator.start();
    }

    public static class WheelItem {
        String text;
        int color;

        public WheelItem(String text, int color) {
            this.text = text;
            this.color = color;
        }
    }

    public interface SpinListener {
        void onSpinComplete();
    }
}
