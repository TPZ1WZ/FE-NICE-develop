package com.example.nike_fe.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nike_fe.R;

import java.util.List;

public class BrandAdapter extends RecyclerView.Adapter<BrandAdapter.BrandViewHolder> {

    private Context context;
    private List<String> brandNames;

    private OnBrandClickListener listener;

    public interface OnBrandClickListener {
        void onBrandClick(String brandName);
    }

    public BrandAdapter(Context context, List<String> brandNames, OnBrandClickListener listener) {
        this.context = context;
        this.brandNames = brandNames;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BrandViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_brand, parent, false);
        return new BrandViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BrandViewHolder holder, int position) {
        String name = brandNames.get(position);
        holder.tvBrandName.setText(name);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBrandClick(name);
            }
        });
    }

    @Override
    public int getItemCount() {
        return brandNames.size();
    }

    public static class BrandViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBrandLogo;
        TextView tvBrandName;

        public BrandViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBrandLogo = itemView.findViewById(R.id.ivBrandLogo);
            tvBrandName = itemView.findViewById(R.id.tvBrandName);
        }
    }
}
