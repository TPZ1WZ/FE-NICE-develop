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
import com.example.nike_fe.data.model.OrderItem;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder> {
    
    private Context context;
    private List<OrderItem> orderItems;
    
    public OrderItemAdapter(Context context, List<OrderItem> orderItems) {
        this.context = context;
        this.orderItems = orderItems;
    }
    
    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_product, parent, false);
        return new OrderItemViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        OrderItem item = orderItems.get(position);
        
        holder.tvProductName.setText(item.getProductName());
        holder.tvProductPrice.setText(formatPrice(item.getProductPrice()));
        holder.tvQuantity.setText("x" + item.getQuantity());
        holder.tvTotalPrice.setText(formatPrice(item.getTotalPrice()));
        
        if (item.getSize() != null && !item.getSize().isEmpty()) {
            holder.tvSize.setText("Size: " + item.getSize());
            holder.tvSize.setVisibility(View.VISIBLE);
        } else {
            holder.tvSize.setVisibility(View.GONE);
        }
        
        // Load product image
        if (item.getFirstImage() != null && !item.getFirstImage().isEmpty()) {
            Glide.with(context)
                .load(item.getFirstImage())
                .placeholder(R.drawable.ic_heart)
                .error(R.drawable.ic_heart)
                .into(holder.ivProductImage);
        }
    }
    
    @Override
    public int getItemCount() {
        return orderItems.size();
    }
    
    private String formatPrice(Double price) {
        if (price == null) return "0 ₫";
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(price) + " ₫";
    }
    
    static class OrderItemViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName, tvProductPrice, tvQuantity, tvSize, tvTotalPrice;
        
        public OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvSize = itemView.findViewById(R.id.tvSize);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
        }
    }
}
