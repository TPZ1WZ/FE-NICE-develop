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
import com.example.nike_fe.data.model.TopProduct;

import java.util.List;

public class TopProductAdapter extends RecyclerView.Adapter<TopProductAdapter.TopProductViewHolder> {

    private Context context;
    private List<TopProduct> topProducts;

    public TopProductAdapter(Context context, List<TopProduct> topProducts) {
        this.context = context;
        this.topProducts = topProducts;
    }

    public void setData(List<TopProduct> list) {
        this.topProducts = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TopProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_top_product, parent, false);
        return new TopProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopProductViewHolder holder, int position) {
        TopProduct product = topProducts.get(position);
        if (product == null) {
            return;
        }

        holder.tvProductName.setText(product.getName());
        holder.tvProductSku.setText(product.getSku());
        holder.tvSoldQuantity.setText(String.valueOf(product.getSoldQuantity()));

        Glide.with(context)
                .load(product.getImageUrl())
                .placeholder(R.drawable.circle_gray_background)
                .error(R.drawable.circle_gray_background)
                .into(holder.imgProduct);
    }

    @Override
    public int getItemCount() {
        if (topProducts != null) {
            return topProducts.size();
        }
        return 0;
    }

    public class TopProductViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgProduct;
        private TextView tvProductName;
        private TextView tvProductSku;
        private TextView tvSoldQuantity;

        public TopProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductSku = itemView.findViewById(R.id.tvProductSku);
            tvSoldQuantity = itemView.findViewById(R.id.tvSoldQuantity);
        }
    }
}
