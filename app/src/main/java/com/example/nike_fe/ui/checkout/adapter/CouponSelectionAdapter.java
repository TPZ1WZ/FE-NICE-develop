package com.example.nike_fe.ui.checkout.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nike_fe.R;
import com.example.nike_fe.data.model.Coupon;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CouponSelectionAdapter extends RecyclerView.Adapter<CouponSelectionAdapter.CouponViewHolder> {

    private List<Coupon> coupons;
    private OnCouponSelectedListener listener;

    public interface OnCouponSelectedListener {
        void onCouponSelected(Coupon coupon);
    }

    public CouponSelectionAdapter(List<Coupon> coupons, OnCouponSelectedListener listener) {
        this.coupons = coupons;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CouponViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_coupon_selection, parent, false);
        return new CouponViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CouponViewHolder holder, int position) {
        Coupon coupon = coupons.get(position);
        holder.bind(coupon, listener);
    }

    @Override
    public int getItemCount() {
        return coupons != null ? coupons.size() : 0;
    }

    static class CouponViewHolder extends RecyclerView.ViewHolder {
        TextView tvCouponCode, tvDiscountValue, tvDescription, tvMinOrder;
        Button btnApply;

        public CouponViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCouponCode = itemView.findViewById(R.id.tvCouponCode);
            tvDiscountValue = itemView.findViewById(R.id.tvDiscountValue);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvMinOrder = itemView.findViewById(R.id.tvMinOrder);
            btnApply = itemView.findViewById(R.id.btnApply);
        }

        public void bind(Coupon coupon, OnCouponSelectedListener listener) {
            android.util.Log.d("CouponAdapter", "Binding coupon: " + coupon.getCode());
            tvCouponCode.setText(coupon.getCode());

            if ("PERCENTAGE".equals(coupon.getDiscountType()) || "PERCENT".equals(coupon.getDiscountType())) {
                if (coupon.getDiscountValue() != null) {
                    tvDiscountValue.setText("Giảm " + coupon.getDiscountValue().intValue() + "%");
                }
            } else {
                NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
                if (coupon.getDiscountValue() != null) {
                    tvDiscountValue.setText("Giảm " + formatter.format(coupon.getDiscountValue()) + "đ");
                }
            }

            tvDescription.setText(coupon.getDescription());

            NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
            if (coupon.getMinOrderValue() != null) {
                tvMinOrder.setText("Đơn tối thiểu: " + formatter.format(coupon.getMinOrderValue()));
            } else {
                tvMinOrder.setText("Không có đơn tối thiểu");
            }

            btnApply.setOnClickListener(v -> listener.onCouponSelected(coupon));
        }
    }
}
