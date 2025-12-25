package com.example.nike_fe.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nike_fe.R;
import com.example.nike_fe.data.model.CartItem;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItems = new ArrayList<>();
    private Context context;
    private OnCartItemListener listener;

    public interface OnCartItemListener {
        void onQuantityChanged(CartItem item, int newQuantity);

        void onItemRemoved(CartItem item);
    }

    public CartAdapter(Context context, OnCartItemListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage, ivDecrease, ivIncrease, ivDelete;
        TextView tvProductName, tvSize, tvPrice, tvQuantity;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvSize = itemView.findViewById(R.id.tvSize);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            ivDecrease = itemView.findViewById(R.id.ivDecrease);
            ivIncrease = itemView.findViewById(R.id.ivIncrease);
            ivDelete = itemView.findViewById(R.id.ivDelete);
        }

        public void bind(CartItem item) {
            tvProductName.setText(item.getProduct().getName());
            tvSize.setText("Size: " + item.getSize());
            tvQuantity.setText(String.valueOf(item.getQuantity()));

            // Format price
            NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
            String formattedPrice = formatter.format(item.getTotalPrice()) + "₫";
            tvPrice.setText(formattedPrice);

            // Load image
            if (item.getProduct().getImages() != null && !item.getProduct().getImages().isEmpty()) {
                String imageUrl = item.getProduct().getImages().get(0);
                if (!imageUrl.startsWith("http")) {
                    imageUrl = "http://10.0.2.2:8080" + imageUrl;
                }

                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_products)
                        .error(R.drawable.ic_products)
                        .centerCrop()
                        .into(ivProductImage);
            } else {
                ivProductImage.setImageResource(R.drawable.ic_products);
            }

            // Decrease quantity
            ivDecrease.setOnClickListener(v -> {
                int currentQty = item.getQuantity();
                if (currentQty > 1 && listener != null) {
                    listener.onQuantityChanged(item, currentQty - 1);
                }
            });

            // Increase quantity
            ivIncrease.setOnClickListener(v -> {
                int currentQty = item.getQuantity();

                // Check stock limit if available
                if (item.getProduct().getStock() != null) {
                    if (currentQty < item.getProduct().getStock()) {
                        if (listener != null) {
                            listener.onQuantityChanged(item, currentQty + 1);
                        }
                    } else {
                        android.widget.Toast.makeText(context,
                                "Đã đạt giới hạn tồn kho: " + item.getProduct().getStock(),
                                android.widget.Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Fallback if stock not populated (should allow to not block user, or block
                    // safer)
                    // Currently allowing increment if data missing to avoid blocking errors
                    if (listener != null) {
                        listener.onQuantityChanged(item, currentQty + 1);
                    }
                }
            });

            // Delete item
            ivDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemRemoved(item);
                }
            });
        }
    }

    public List<CartItem> getItems() {
        return cartItems;
    }
}
