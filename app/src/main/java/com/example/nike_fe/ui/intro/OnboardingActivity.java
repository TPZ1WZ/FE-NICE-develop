package com.example.nike_fe.ui.intro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.nike_fe.R;
import com.example.nike_fe.ui.auth.LoginActivity;

public class OnboardingActivity extends AppCompatActivity implements OnboardingAdapter.OnButtonClickListener {

    private ViewPager2 viewPager;
    private LinearLayout dotsLayout;
    private OnboardingAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPager);
        dotsLayout = findViewById(R.id.dotsLayout);

        adapter = new OnboardingAdapter(this, this);
        viewPager.setAdapter(adapter);
        
        // Set offscreen page limit to keep all pages in memory
        viewPager.setOffscreenPageLimit(2);

        setupDots(0);
        
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                setupDots(position);
            }
        });
    }

    @Override
    public void onNextClick(int position) {
        Log.d("OnboardingActivity", "onNextClick called with position: " + position);
        if (position < 2) {
            Log.d("OnboardingActivity", "Moving to page: " + (position + 1));
            viewPager.setCurrentItem(position + 1, true);
        }
    }

    @Override
    public void onGetStartedClick() {
        finishOnboarding();
    }

    private void setupDots(int currentPage) {
        dotsLayout.removeAllViews();
        
        for (int i = 0; i < 3; i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                i == currentPage ? 40 : 12,
                12
            );
            params.setMargins(8, 0, 8, 0);
            dot.setLayoutParams(params);
            
            if (i == currentPage) {
                dot.setBackgroundResource(R.drawable.dot_active);
            } else {
                dot.setBackgroundResource(R.drawable.dot_inactive);
            }
            
            dotsLayout.addView(dot);
        }
    }

    private void finishOnboarding() {
        // Không lưu trạng thái, luôn hiện intro mỗi lần mở app
        Intent intent = new Intent(OnboardingActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
