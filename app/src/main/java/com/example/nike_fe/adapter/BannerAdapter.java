package com.example.nike_fe.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nike_fe.R;
import com.example.nike_fe.data.model.Banner;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

    private List<Banner> banners;

    public BannerAdapter(List<Banner> banners) {
        this.banners = banners;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        if (banners == null || banners.isEmpty())
            return;
        // Use modulo to cycle through items
        Banner banner = banners.get(position % banners.size());
        holder.bind(banner);
    }

    @Override
    public int getItemCount() {
        return banners == null || banners.isEmpty() ? 0 : Integer.MAX_VALUE;
    }

    static class BannerViewHolder extends RecyclerView.ViewHolder {
        FrameLayout bannerContainer;
        TextView tvDiscount;
        TextView tvTitle;
        TextView tvDescription;
        ImageView ivBannerImage;

        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            bannerContainer = itemView.findViewById(R.id.bannerContainer);
            tvDiscount = itemView.findViewById(R.id.tvDiscount);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            ivBannerImage = itemView.findViewById(R.id.ivBannerImage);
        }

        public void bind(Banner banner) {
            tvDiscount.setText(banner.getDiscount());
            tvTitle.setText(banner.getTitle());
            tvDescription.setText(banner.getDescription());
            ivBannerImage.setImageResource(banner.getImageResId());

            // Set dynamic background if needed, or keeping static from XML is fine.
            // If backgroundDrawable is meant to be a resource ID:
            if (banner.getBackgroundDrawable() != 0) {
                bannerContainer.setBackgroundResource(banner.getBackgroundDrawable());
            }
        }
    }
}
