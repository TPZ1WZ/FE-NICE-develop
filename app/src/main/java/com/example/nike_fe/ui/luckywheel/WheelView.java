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

public class WheelView extends View {

    private Paint paint;
    private Paint textPaint;
    private Paint borderPaint;
    private Paint shadowPaint;
    private RectF rectF;
    private float currentRotation = 0f;
    private boolean isSpinning = false;

    // Wheel prizes - matching backend rewards (8 positions)
    private final String[] prizes = { "1000", "☹", "2000", "500", "100", "1500", "50", "10K" };

    // Premium Colors (8 colors for 8 positions)
    private final int[] colors = {
            Color.parseColor("#6200EA"), // Purple - 1000
            Color.parseColor("#FFD700"), // Gold - NOTHING
            Color.parseColor("#1A237E"), // Navy - 2000
            Color.parseColor("#FFD700"), // Gold - 500
            Color.parseColor("#6200EA"), // Purple - 100
            Color.parseColor("#1A237E"), // Navy - 1500
            Color.parseColor("#FFD700"), // Gold - 50
            Color.parseColor("#6200EA") // Purple - 10000
    };

    private Runnable onSpinComplete;

    public WheelView(Context context) {
        super(context);
        init();
    }

    public WheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WheelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(50f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);
        // Add shadow to text for better visibility
        textPaint.setShadowLayer(3f, 1f, 1f, Color.BLACK);

        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(12f);
        borderPaint.setColor(Color.WHITE);
        // Glow effect for border - requires software layer type for setShadowLayer to
        // work fully on some devices,
        // but for simple stroke it might be okay. For consistency let's just use white
        // border.

        shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setColor(Color.BLACK);
        shadowPaint.setAlpha(50);

        rectF = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int size = Math.min(width, height);
        int centerX = width / 2;
        int centerY = height / 2;
        int radius = size / 2 - 30; // Increased padding for border/shadow

        rectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);

        // Draw shadow/glow behind the wheel
        canvas.drawCircle(centerX, centerY, radius + 10, shadowPaint);

        canvas.save();
        canvas.rotate(currentRotation, centerX, centerY);

        // Draw wheel segments
        float sweepAngle = 360f / prizes.length;
        for (int i = 0; i < prizes.length; i++) {
            paint.setColor(colors[i]);
            canvas.drawArc(rectF, i * sweepAngle, sweepAngle, true, paint);

            // Determine text color based on background
            // Gold background -> Black text, Dark background -> Gold/White text
            if (colors[i] == Color.parseColor("#FFD700")) {
                textPaint.setColor(Color.BLACK);
                textPaint.setShadowLayer(0f, 0f, 0f, 0); // Remove shadow for black text
            } else {
                textPaint.setColor(Color.WHITE);
                textPaint.setShadowLayer(4f, 2f, 2f, Color.parseColor("#80000000"));
            }

            // Draw text
            float angle = (float) Math.toRadians(i * sweepAngle + sweepAngle / 2);
            float textRadius = radius * 0.7f; // Push text slightly further out
            float textX = centerX + textRadius * (float) Math.cos(angle);
            float textY = centerY + textRadius * (float) Math.sin(angle);

            // Adjust textY for vertical centering (approximate)
            float textHeight = textPaint.descent() - textPaint.ascent();
            float textOffset = (textHeight / 2) - textPaint.descent();
            textY += textOffset;

            canvas.save();
            // Radial rotation: Rotate so text points outwards/inwards.
            // i * sweepAngle + sweepAngle / 2 is the angle of the slice center.
            // Adding 0 makes it radial outwards (text bottom to center).
            canvas.rotate(i * sweepAngle + sweepAngle / 2, textX, textY);
            canvas.drawText(prizes[i], textX, textY, textPaint);
            canvas.restore();
        }

        // Draw dividers
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(4f);
        for (int i = 0; i < prizes.length; i++) {
            float angle = (float) Math.toRadians(i * sweepAngle);
            float startX = centerX;
            float startY = centerY;
            float endX = centerX + radius * (float) Math.cos(angle);
            float endY = centerY + radius * (float) Math.sin(angle);
            canvas.drawLine(startX, startY, endX, endY, paint);
        }

        canvas.restore();

        // Draw outer border
        canvas.drawCircle(centerX, centerY, radius, borderPaint);

        // Draw center circle (The "Hub")
        // Outer ring of hub
        paint.setColor(Color.WHITE);
        canvas.drawCircle(centerX, centerY, radius * 0.18f, paint);

        // Inner hub
        paint.setColor(Color.parseColor("#1A103C")); // Dark center
        canvas.drawCircle(centerX, centerY, radius * 0.15f, paint);

        // Gold center point
        paint.setColor(Color.parseColor("#FFD700"));
        canvas.drawCircle(centerX, centerY, radius * 0.05f, paint);
    }

    public void spinTo(int prizeIndex, Runnable callback) {
        if (isSpinning) {
            return;
        }

        isSpinning = true;
        onSpinComplete = callback;

        // Calculate target rotation
        float sweepAngle = 360f / prizes.length; // 45 degrees per segment

        // Calculation:
        // Position 0 is at [0, 45] degrees (East-SouthEast). Center at 22.5 deg.
        // We want the selected prize center to land at 270 degrees (Top/North) where
        // the pointer is.
        // Rotation needed = Target(270) - Current(PrizeCenter)

        float prizeCenterAngle = (prizeIndex * sweepAngle) + (sweepAngle / 2);
        float targetAngle = 270f - prizeCenterAngle;

        // Ensure positive rotation (clockwise)
        float totalRotation = 360 * 5 + targetAngle;

        ValueAnimator animator = ValueAnimator.ofFloat(currentRotation, currentRotation + totalRotation);
        animator.setDuration(4000); // Slower, more suspenseful spin
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            currentRotation = (float) animation.getAnimatedValue() % 360;
            invalidate();
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isSpinning = false;
                if (onSpinComplete != null) {
                    postDelayed(onSpinComplete, 500);
                }
            }
        });
        animator.start();
    }

    public boolean isSpinning() {
        return isSpinning;
    }
}
