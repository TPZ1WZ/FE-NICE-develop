package com.example.nike_fe.ui.admin.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nike_fe.R;
import com.example.nike_fe.data.model.Coupon;
import com.google.android.material.chip.Chip;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminCouponAdapter extends RecyclerView.Adapter<AdminCouponAdapter.CouponViewHolder> {

    private Context context;
    private List<Coupon> coupons;
    private OnCouponActionClickListener listener;

    public interface OnCouponActionClickListener {
        void onEdit(Coupon coupon);

        void onDelete(Coupon coupon);
    }

    public AdminCouponAdapter(Context context, OnCouponActionClickListener listener) {
        this.context = context;
        this.coupons = new ArrayList<>();
        this.listener = listener;
    }

    public void setCoupons(List<Coupon> coupons) {
        this.coupons = coupons;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CouponViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_coupon, parent, false);
        return new CouponViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CouponViewHolder holder, int position) {
        Coupon coupon = coupons.get(position);
        holder.bind(coupon);
    }

    @Override
    public int getItemCount() {
        return coupons != null ? coupons.size() : 0;
    }

    class CouponViewHolder extends RecyclerView.ViewHolder {

        TextView tvCouponCode, tvCouponDescription, tvDiscountValue, tvExpiryDate;
        Chip chipStatus;
        ImageView btnEdit, btnDelete;

        public CouponViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCouponCode = itemView.findViewById(R.id.tvCouponCode);
            tvCouponDescription = itemView.findViewById(R.id.tvCouponDescription);
            tvDiscountValue = itemView.findViewById(R.id.tvDiscountValue);
            tvExpiryDate = itemView.findViewById(R.id.tvExpiryDate);
            chipStatus = itemView.findViewById(R.id.chipStatus);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(Coupon coupon) {
            tvCouponCode.setText(coupon.getCode());
            tvCouponDescription.setText(coupon.getDescription());

            // Format Discount Value
            if ("PERCENTAGE".equals(coupon.getDiscountType()) || "PERCENT".equals(coupon.getDiscountType())) {
                tvDiscountValue.setText("Giảm: " + String.format("%.0f", coupon.getDiscountValue()) + "%");
            } else {
                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
                tvDiscountValue.setText("Giảm: " + currencyFormat.format(coupon.getDiscountValue()));
            }

            // Expiry Date
            String expiryText = "Hết hạn: " + (coupon.getEndDate() != null ? coupon.getEndDate() : "Vô thời hạn");
            tvExpiryDate.setText(expiryText);

            // Status
            boolean isActive = Boolean.TRUE.equals(coupon.getIsActive());
            if (isActive) {
                chipStatus.setText("Active");
                chipStatus.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#DCFCE7")));
                chipStatus.setTextColor(Color.parseColor("#166534"));
            } else {
                chipStatus.setText("Inactive");
                chipStatus.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#F3F4F6")));
                chipStatus.setTextColor(Color.parseColor("#4B5563"));
            }

            btnEdit.setOnClickListener(v -> {
                if (listener != null)
                    listener.onEdit(coupon);
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null)
                    listener.onDelete(coupon);
            });
        }
    }
}
