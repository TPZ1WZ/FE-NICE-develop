package com.example.nike_fe.ui.admin.adapter;

import android.graphics.Color;
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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.ProductViewHolder> {

    private List<AdminProduct> products = new ArrayList<>();
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(AdminProduct product);
        
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
        private View cardProduct;
        private ImageView ivProductImage;
        private TextView tvProductName, tvProductSkuCategory;
        private TextView tvProductPrice, tvProductSalePrice, tvProductStock;
        private TextView tvStatus;
        private ImageView btnEdit, btnDelete;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            cardProduct = itemView.findViewById(R.id.cardProduct);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductSkuCategory = itemView.findViewById(R.id.tvProductSkuCategory);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvProductSalePrice = itemView.findViewById(R.id.tvProductSalePrice);
            tvProductStock = itemView.findViewById(R.id.tvProductStock);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(AdminProduct product, OnProductClickListener listener) {
            try {
                // Reset button visibility first (fix RecyclerView reuse issue)
                if (btnEdit != null)
                    btnEdit.setVisibility(View.VISIBLE);
                if (btnDelete != null)
                    btnDelete.setVisibility(View.VISIBLE);

                if (product == null) {
                    return;
                }

                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

                // Load image
                Glide.with(itemView.getContext())
                        .load(product.getImage())
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .into(ivProductImage);

                // Product info
                tvProductName.setText(product.getName());
                
                // SKU + Category
                String skuCategory = "SKU: " + product.getSku();
                if (product.getCategory() != null && !product.getCategory().isEmpty()) {
                    skuCategory += " • " + product.getCategory();
                }
                tvProductSkuCategory.setText(skuCategory);
                
                // Card click listener
                cardProduct.setOnClickListener(v -> {
                    if (listener != null && product != null) {
                        listener.onProductClick(product);
                    }
                });

                // Price
                if (product.getSalePrice() != null && product.getSalePrice() > 0) {
                    tvProductPrice.setText(currencyFormat.format(product.getSalePrice()));
                    if (tvProductSalePrice != null) {
                        tvProductSalePrice.setVisibility(View.VISIBLE);
                        tvProductSalePrice.setText(currencyFormat.format(product.getPrice()));
                        tvProductSalePrice.setPaintFlags(
                                tvProductSalePrice.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                    }
                } else {
                    tvProductPrice.setText(currencyFormat.format(product.getPrice()));
                    if (tvProductSalePrice != null) {
                        tvProductSalePrice.setVisibility(View.GONE);
                    }
                }

                // Stock Logic - Same background for all, only change text color
                tvProductStock.setText("Kho: " + product.getStock());
                tvProductStock.setBackgroundResource(R.drawable.bg_status_pill);
                if (product.isOutOfStock()) {
                    // Light Gray Background, Red Text #E53935
                    tvProductStock.setTextColor(Color.parseColor("#E53935"));
                } else if (product.isLowStock()) {
                    // Light Gray Background, Orange Text #F9A825
                    tvProductStock.setTextColor(Color.parseColor("#F9A825"));
                } else {
                    // Light Gray Background, Dark Gray Text #555555
                    tvProductStock.setTextColor(Color.parseColor("#555555"));
                }

                // Status Badge Logic - Same background, different text color
                tvStatus.setBackgroundResource(R.drawable.bg_status_pill);
                if (product.isActive()) {
                    tvStatus.setText("Đang hiển thị");
                    tvStatus.setTextColor(Color.parseColor("#555555"));
                } else {
                    tvStatus.setText("Đã ẩn");
                    tvStatus.setTextColor(Color.parseColor("#999999"));
                }

                // Click listeners
                btnEdit.setOnClickListener(v -> {
                    if (listener != null && product != null) {
                        listener.onEditClick(product);
                    }
                });

                btnDelete.setOnClickListener(v -> {
                    if (listener != null && product != null) {
                        listener.onDeleteClick(product);
                    }
                });
            } catch (Exception e) {
                android.util.Log.e("AdminProductAdapter", "Error binding product: " + e.getMessage(), e);
                // Set default values on error
                if (tvProductName != null)
                    tvProductName.setText("Lỗi hiển thị");
            }
        }
    }
}
