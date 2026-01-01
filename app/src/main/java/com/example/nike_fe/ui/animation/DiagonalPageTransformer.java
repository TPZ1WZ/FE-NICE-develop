package com.example.nike_fe.ui.animation;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

public class DiagonalPageTransformer implements ViewPager2.PageTransformer {
    @Override
    public void transformPage(@NonNull View page, float position) {
        // Position range: -1 (Leaving) ... 0 (Current) ... 1 (Entering)
        int width = page.getWidth();
        int height = page.getHeight();

        // 1. STACK PAGES: Counteract default horizontal scroll
        // This makes all pages sit on top of each other at the center (before we apply
        // custom motion)
        float stackTranslationX = -position * width;

        // 2. DIAGONAL MOTION (Bottom-Left <-> Top-Right)
        // We want the LEAVING page (0 -> -1) to go to TOP-RIGHT.
        // We want the ENTERING page (1 -> 0) to come from BOTTOM-LEFT.

        // At position -1 (Left/Exiting), we want visual offset: X=+Width, Y=-Height
        // (Top-Right)
        // At position 1 (Right/Entering), we want visual offset: X=-Width, Y=+Height
        // (Bottom-Left)

        // Formula that satisfies this:
        // MotionX = -position * width
        // MotionY = position * height

        // Combined TranslationX = Stack + MotionX
        // = (-position * width) + (-position * width) = -2 * position * width

        page.setTranslationX(stackTranslationX + (-position * width));
        page.setTranslationY(position * height);

        // 3. EFFECT REFINEMENTS
        // Z-Index: Ensure the page closer to center (0) is on top
        page.setTranslationZ(-Math.abs(position));

        // Soft Wipe: Fade out edges slightly
        // 1.0 -> 0.5 alpha fade
        float alpha = 0.5f + (0.5f * (1 - Math.abs(position)));
        page.setAlpha(alpha);
    }
}
