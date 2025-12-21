package com.example.nike_fe.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.Paint;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nike_fe.R;
import com.example.nike_fe.data.model.Product;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchProductAdapter extends RecyclerView.Adapter<SearchProductAdapter.SearchViewHolder> {

    private List<Product> products = new ArrayList<>();
    private Context context;
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public SearchProductAdapter(Context context, OnProductClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_product, parent, false);
        return new SearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        Product product = products.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return products != null ? products.size() : 0;
    }

    class SearchViewHolder extends RecyclerView.ViewHolder {
        ImageView ivSearchProductImage;
        TextView tvSearchProductName;
        TextView tvSearchProductPrice;
        TextView tvSearchProductOldPrice;
        TextView tvSearchDiscountBadge;
        TextView tvSearchStockStatus;

        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);
            ivSearchProductImage = itemView.findViewById(R.id.ivSearchProductImage);
            tvSearchProductName = itemView.findViewById(R.id.tvSearchProductName);
            tvSearchProductPrice = itemView.findViewById(R.id.tvSearchProductPrice);
            tvSearchProductOldPrice = itemView.findViewById(R.id.tvSearchProductOldPrice);
            tvSearchDiscountBadge = itemView.findViewById(R.id.tvSearchDiscountBadge);
            tvSearchStockStatus = itemView.findViewById(R.id.tvSearchStockStatus);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onProductClick(products.get(position));
                }
            });
        }

        public void bind(Product product) {
            tvSearchProductName.setText(product.getName());

            // Format price
            NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
            String formattedPrice = formatter.format(product.getPrice()) + " đ";
            tvSearchProductPrice.setText(formattedPrice);

            // Mock logic for Demo UI (Since backend doesn't have these fields yet)
            // If price > 2,000,000 show discount
            if (product.getPrice() > 2000000) {
                double original = product.getPrice() * 1.2; // Mock 20% markup
                String formattedOriginal = formatter.format(original) + " đ";
                tvSearchProductOldPrice.setText(formattedOriginal);
                tvSearchProductOldPrice.setVisibility(View.VISIBLE);
                tvSearchProductOldPrice
                        .setPaintFlags(tvSearchProductOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvSearchDiscountBadge.setVisibility(View.VISIBLE);
                tvSearchDiscountBadge.setText("-20%");
            } else {
                tvSearchProductOldPrice.setVisibility(View.GONE);
                tvSearchDiscountBadge.setVisibility(View.GONE);
            }

            // Always show "Còn hàng" for now
            tvSearchStockStatus.setVisibility(View.VISIBLE);

            // Load Image
            if (product.getThumbnail() != null && !product.getThumbnail().isEmpty()) {
                String imageUrl = product.getThumbnail();
                if (imageUrl.startsWith("/")) {
                    imageUrl = "http://10.0.2.2:8080" + imageUrl;
                }
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.img_placeholder_shoe)
                        .error(R.drawable.img_placeholder_shoe)
                        .centerCrop() // Use centerCrop for square image
                        .into(ivSearchProductImage);
            } else {
                ivSearchProductImage.setImageResource(R.drawable.img_placeholder_shoe);
            }
        }
    }
}
