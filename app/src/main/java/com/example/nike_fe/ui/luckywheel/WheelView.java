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

    // Wheel prizes - will be loaded from API (default 8 empty slots)
    private String[] prizes = { "", "", "", "", "", "", "", "" };

    // Premium Colors (8 colors for 8 positions) - Vibrant & Diverse Palette
    private final int[] colors = {
            Color.parseColor("#FF6B35"), // Vibrant Orange - 1000
            Color.parseColor("#F7B801"), // Golden Yellow - NOTHING
            Color.parseColor("#6A0572"), // Deep Purple - 2000
            Color.parseColor("#00D9FF"), // Bright Cyan - 500
            Color.parseColor("#FF1744"), // Red - 100
            Color.parseColor("#00E676"), // Green - 1500
            Color.parseColor("#3D5AFE"), // Blue - 50
            Color.parseColor("#FFD600") // Yellow - 10000
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

            // Determine text color based on background brightness
            int color = colors[i];
            int brightness = (Color.red(color) + Color.green(color) + Color.blue(color)) / 3;
            
            if (brightness > 180) { // Bright background
                textPaint.setColor(Color.BLACK);
                textPaint.setShadowLayer(2f, 1f, 1f, Color.parseColor("#40FFFFFF")); // Light shadow
            } else { // Dark background
                textPaint.setColor(Color.WHITE);
                textPaint.setShadowLayer(4f, 2f, 2f, Color.parseColor("#80000000")); // Dark shadow
            }

            // Auto-adjust font size based on text length
            String text = prizes[i];
            float baseFontSize = 50f;
            float fontSize = baseFontSize;
            
            // Reduce font size for longer text
            if (text.length() > 15) {
                fontSize = baseFontSize * 0.5f; // Very long text
            } else if (text.length() > 10) {
                fontSize = baseFontSize * 0.65f; // Long text
            } else if (text.length() > 6) {
                fontSize = baseFontSize * 0.8f; // Medium text
            }
            
            textPaint.setTextSize(fontSize);

            // Draw text
            float angle = (float) Math.toRadians(i * sweepAngle + sweepAngle / 2);
            float textRadius = radius * 0.7f;
            float textX = centerX + textRadius * (float) Math.cos(angle);
            float textY = centerY + textRadius * (float) Math.sin(angle);

            // Adjust textY for vertical centering
            float textHeight = textPaint.descent() - textPaint.ascent();
            float textOffset = (textHeight / 2) - textPaint.descent();
            textY += textOffset;

            canvas.save();
            canvas.rotate(i * sweepAngle + sweepAngle / 2, textX, textY);
            
            // For very long text, break into multiple lines
            if (text.length() > 15) {
                String[] words = text.split(" ");
                if (words.length > 2) {
                    // Draw first part above center
                    String line1 = words[0] + " " + words[1];
                    canvas.drawText(line1, textX, textY - fontSize * 0.5f, textPaint);
                    // Draw second part below center
                    String line2 = "";
                    for (int j = 2; j < words.length; j++) {
                        line2 += words[j] + (j < words.length - 1 ? " " : "");
                    }
                    canvas.drawText(line2, textX, textY + fontSize * 0.5f, textPaint);
                } else {
                    canvas.drawText(text, textX, textY, textPaint);
                }
            } else {
                canvas.drawText(text, textX, textY, textPaint);
            }
            
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

        // Draw outer border with gold color
        borderPaint.setColor(Color.parseColor("#FFD700")); // Gold border
        borderPaint.setStrokeWidth(8f);
        canvas.drawCircle(centerX, centerY, radius, borderPaint);

        // Draw center circle (The "Hub") - Multi-layer design
        // Outer ring of hub - Gold
        paint.setColor(Color.parseColor("#FFD700"));
        canvas.drawCircle(centerX, centerY, radius * 0.20f, paint);
        
        // Middle ring - White
        paint.setColor(Color.WHITE);
        canvas.drawCircle(centerX, centerY, radius * 0.17f, paint);

        // Inner hub - Dark purple gradient effect
        paint.setColor(Color.parseColor("#6A0572"));
        canvas.drawCircle(centerX, centerY, radius * 0.14f, paint);

        // Gold center point with glow
        paint.setColor(Color.parseColor("#FFD600"));
        canvas.drawCircle(centerX, centerY, radius * 0.06f, paint);
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

    public void setPrizes(String[] newPrizes) {
        if (newPrizes != null && newPrizes.length == 8) {
            this.prizes = newPrizes;
            invalidate(); // Redraw with new prizes
        }
    }

    public boolean isSpinning() {
        return isSpinning;
    }
}
