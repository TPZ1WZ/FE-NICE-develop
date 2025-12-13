package com.example.nike_fe.ui.intro;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nike_fe.R;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {

    private Context context;
    private OnButtonClickListener listener;
    private int[] images = {R.drawable.spring_prev_ui_1, R.drawable.aire_jordan_nike, R.drawable.spring_prev_ui_1};
    private String[] titles = {
        "WELCOME TO\nNIKE",
        "Let's Start Journey\nWith Nike",
        "You Have The\nPower To"
    };
    private String[] descriptions = {
        "Just Do It",
        "Smart, Gorgeous & Fashionable\nCollection Explore Now",
        "There Are More Than 300+\nNew Shoes For You"
    };

    public interface OnButtonClickListener {
        void onNextClick(int position);
        void onGetStartedClick();
    }

    public OnboardingAdapter(Context context, OnButtonClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_onboarding, parent, false);
        // Force ViewPager2 to respect match_parent
        view.setLayoutParams(new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ));
        return new OnboardingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        holder.ivOnboarding.setImageResource(images[position]);
        holder.tvTitle.setText(titles[position]);
        holder.tvDescription.setText(descriptions[position]);
        
        if (descriptions[position].isEmpty()) {
            holder.tvDescription.setVisibility(View.GONE);
        } else {
            holder.tvDescription.setVisibility(View.VISIBLE);
        }
        
        // Set button text and click listener
        if (position == 2) {
            holder.btnAction.setText("Get Started");
            holder.btnAction.setOnClickListener(v -> {
                Log.d("OnboardingAdapter", "Get Started button clicked");
                if (listener != null) listener.onGetStartedClick();
            });
        } else {
            holder.btnAction.setText("Next");
            holder.btnAction.setOnClickListener(v -> {
                Log.d("OnboardingAdapter", "Next button clicked at position: " + position);
                if (listener != null) {
                    Log.d("OnboardingAdapter", "Listener is not null, calling onNextClick");
                    listener.onNextClick(position);
                } else {
                    Log.e("OnboardingAdapter", "Listener is null!");
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    static class OnboardingViewHolder extends RecyclerView.ViewHolder {
        ImageView ivOnboarding;
        TextView tvTitle, tvDescription;
        Button btnAction;

        public OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);
            ivOnboarding = itemView.findViewById(R.id.ivOnboarding);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            btnAction = itemView.findViewById(R.id.btnAction);
        }
    }
}
