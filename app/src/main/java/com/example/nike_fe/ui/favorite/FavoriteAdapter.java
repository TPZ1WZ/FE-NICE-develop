package com.example.nike_fe.ui.favorite;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nike_fe.R;
import com.example.nike_fe.data.model.FavoriteProduct;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder> {

    private List<FavoriteProduct> favoriteList;
    private OnFavoriteClickListener listener;

    public interface OnFavoriteClickListener {
        void onProductClick(FavoriteProduct product);

        void onRemoveClick(FavoriteProduct product);
    }

    public FavoriteAdapter(List<FavoriteProduct> favoriteList, OnFavoriteClickListener listener) {
        this.favoriteList = favoriteList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        FavoriteProduct product = favoriteList.get(position);

        holder.tvProductName.setText(product.getName());
        holder.tvSubTitle.setText(product.getSubTitle());

        // Format price
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.tvPrice.setText(formatter.format(product.getPrice()));

        // Category
        if (product.getCategoryName() != null) {
            holder.tvCategory.setText(product.getCategoryName());
        }

        // Load image
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            String imageUrl = product.getImages().get(0);

            // Xử lý base64 image data
            if (imageUrl.startsWith("data:image")) {
                // Base64 image - load trực tiếp
                Glide.with(holder.itemView.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.img_placeholder_shoe)
                        .error(R.drawable.img_placeholder_shoe)
                        .centerCrop()
                        .into(holder.ivProduct);
            }
            // URL đầy đủ
            else if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                Glide.with(holder.itemView.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.img_placeholder_shoe)
                        .error(R.drawable.img_placeholder_shoe)
                        .centerCrop()
                        .into(holder.ivProduct);
            }
            // Relative URL
            else if (imageUrl.startsWith("/")) {
                imageUrl = "http://10.0.2.2:8080" + imageUrl;
                Glide.with(holder.itemView.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.img_placeholder_shoe)
                        .error(R.drawable.img_placeholder_shoe)
                        .centerCrop()
                        .into(holder.ivProduct);
            } else {
                // Fallback
                Glide.with(holder.itemView.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.img_placeholder_shoe)
                        .error(R.drawable.img_placeholder_shoe)
                        .centerCrop()
                        .into(holder.ivProduct);
            }
        } else {
            holder.ivProduct.setImageResource(R.drawable.img_placeholder_shoe);
        }

        // Click listeners with separate debounce
        holder.itemView.setOnClickListener(v -> {
            if (holder.canClickProduct() && listener != null) {
                listener.onProductClick(product);
            }
        });

        holder.ivRemove.setOnClickListener(v -> {
            if (holder.canClickRemove() && listener != null) {
                listener.onRemoveClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return favoriteList.size();
    }

    static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        ImageView ivRemove;
        TextView tvProductName;
        TextView tvSubTitle;
        TextView tvPrice;
        TextView tvCategory;
        private long lastProductClickTime = 0;
        private long lastRemoveClickTime = 0;

        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.ivProduct);
            ivRemove = itemView.findViewById(R.id.ivRemove);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvSubTitle = itemView.findViewById(R.id.tvSubTitle);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvCategory = itemView.findViewById(R.id.tvCategory);
        }

        public boolean canClickProduct() {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastProductClickTime > 300) {
                lastProductClickTime = currentTime;
                return true;
            }
            return false;
        }

        public boolean canClickRemove() {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastRemoveClickTime > 1000) {
                lastRemoveClickTime = currentTime;
                return true;
            }
            return false;
        }
    }
}
