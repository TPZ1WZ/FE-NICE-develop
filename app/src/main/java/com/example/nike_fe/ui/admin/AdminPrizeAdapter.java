package com.example.nike_fe.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nike_fe.R;
import com.example.nike_fe.data.model.Prize;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class AdminPrizeAdapter extends RecyclerView.Adapter<AdminPrizeAdapter.PrizeViewHolder> {

    private List<Prize> prizes;
    private final Consumer<Prize> onEditClick;
    private final Consumer<Prize> onDeleteClick;

    public AdminPrizeAdapter(List<Prize> prizes, Consumer<Prize> onEditClick, Consumer<Prize> onDeleteClick) {
        this.prizes = prizes != null ? prizes : new ArrayList<>();
        this.onEditClick = onEditClick;
        this.onDeleteClick = onDeleteClick;
    }

    public void setPrizes(List<Prize> prizes) {
        this.prizes = prizes != null ? prizes : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PrizeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_prize, parent, false);
        return new PrizeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PrizeViewHolder holder, int position) {
        Prize prize = prizes.get(position);
        holder.bind(prize);
    }

    @Override
    public int getItemCount() {
        return prizes.size();
    }

    class PrizeViewHolder extends RecyclerView.ViewHolder {

        ImageView ivIcon, btnEdit, btnDelete;
        TextView tvName, tvType, tvProbability, tvQuantity;

        public PrizeViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivPrizeIcon);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            tvName = itemView.findViewById(R.id.tvPrizeName);
            tvType = itemView.findViewById(R.id.tvPrizeType);
            tvProbability = itemView.findViewById(R.id.tvProbability);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
        }

        public void bind(Prize prize) {
            tvName.setText(prize.getName());

            String typeText = prize.getType();
            if ("VOUCHER".equals(typeText) && prize.getDiscountValue() != null) {
                typeText += " - " + String.format("%.0f", prize.getDiscountValue());
            }
            tvType.setText(typeText);

            // Format probability as percentage
            if (prize.getProbability() != null) {
                String prob = String.format("%.1f", prize.getProbability() * 100) + "%";
                tvProbability.setText(prob);
            } else {
                tvProbability.setText("0%");
            }

            // Quantity
            int remaining = prize.getRemainingQuantity() != null ? prize.getRemainingQuantity() : 0;
            int total = prize.getQuantity() != null ? prize.getQuantity() : 0;
            tvQuantity.setText(remaining + "/" + total);

            // Icon
            if (prize.getIconUrl() != null && !prize.getIconUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(prize.getIconUrl())
                        .placeholder(R.drawable.ic_voucher)
                        .error(R.drawable.ic_voucher)
                        .into(ivIcon);
            } else {
                ivIcon.setImageResource(R.drawable.ic_voucher);
            }

            btnEdit.setOnClickListener(v -> onEditClick.accept(prize));
            btnDelete.setOnClickListener(v -> onDeleteClick.accept(prize));
        }
    }
}
