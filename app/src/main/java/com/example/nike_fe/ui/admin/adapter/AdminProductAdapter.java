package com.example.nike_fe.ui.admin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nike_fe.R;
import com.example.nike_fe.data.model.AdminProduct;
import com.google.android.material.chip.Chip;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.ProductViewHolder> {
    
    private List<AdminProduct> products = new ArrayList<>();
    private OnProductClickListener listener;
    
    public interface OnProductClickListener {
        void onEditClick(AdminProduct product);
        void onDeleteClick(AdminProduct product);
    }
    
    public void setOnProductClickListener(OnProductClickListener listener) {
        this.listener = listener;
    }
    
    public void setProducts(List<AdminProduct> products) {
        this.products = products;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_product, parent, false);
        return new ProductViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        AdminProduct product = products.get(position);
        holder.bind(product, listener);
    }
    
    @Override
    public int getItemCount() {
        return products.size();
    }
    
    static class ProductViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProductImage;
        private TextView tvProductName, tvProductSku, tvProductCategory;
        private TextView tvProductPrice, tvProductSalePrice, tvProductStock;
        private Chip chipStatus;
        private ImageButton btnEdit, btnDelete;
        
        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductSku = itemView.findViewById(R.id.tvProductSku);
            tvProductCategory = itemView.findViewById(R.id.tvProductCategory);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvProductSalePrice = itemView.findViewById(R.id.tvProductSalePrice);
            tvProductStock = itemView.findViewById(R.id.tvProductStock);
            chipStatus = itemView.findViewById(R.id.chipStatus);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
        
        public void bind(AdminProduct product, OnProductClickListener listener) {
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            
            // Load image
            Glide.with(itemView.getContext())
                    .load(product.getImage())
                    .placeholder(R.drawable.placeholder)
                    .into(ivProductImage);
            
            // Product info
            tvProductName.setText(product.getName());
            tvProductSku.setText("SKU: " + product.getSku());
            tvProductCategory.setText(product.getCategory());
            
            // Price
            if (product.getSalePrice() != null && product.getSalePrice() > 0) {
                tvProductPrice.setText(currencyFormat.format(product.getSalePrice()));
                tvProductSalePrice.setVisibility(View.VISIBLE);
                tvProductSalePrice.setText(currencyFormat.format(product.getPrice()));
            } else {
                tvProductPrice.setText(currencyFormat.format(product.getPrice()));
                tvProductSalePrice.setVisibility(View.GONE);
            }
            
            // Stock
            tvProductStock.setText("Kho: " + product.getStock());
            if (product.isOutOfStock()) {
                tvProductStock.setTextColor(itemView.getContext().getColor(R.color.red_600));
            } else if (product.isLowStock()) {
                tvProductStock.setTextColor(itemView.getContext().getColor(R.color.orange_600));
            } else {
                tvProductStock.setTextColor(itemView.getContext().getColor(R.color.black));
            }
            
            // Status
            if (product.isActive()) {
                chipStatus.setText("Hiển thị");
                chipStatus.setChipBackgroundColorResource(R.color.green_100);
                chipStatus.setTextColor(itemView.getContext().getColor(R.color.green_800));
            } else {
                chipStatus.setText("Đã ẩn");
                chipStatus.setChipBackgroundColorResource(R.color.gray_200);
                chipStatus.setTextColor(itemView.getContext().getColor(R.color.gray_800));
            }
            
            // Click listeners
            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(product);
                }
            });
            
            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(product);
                }
            });
        }
    }
}
