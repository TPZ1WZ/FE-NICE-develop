package com.example.nike_fe.ui.checkout.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nike_fe.R;
import com.example.nike_fe.data.model.Coupon;
import com.google.android.material.card.MaterialCardView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CouponSelectionAdapter extends RecyclerView.Adapter<CouponSelectionAdapter.CouponViewHolder> {

    private List<Coupon> coupons;
    private OnCouponSelectedListener listener;
    private double currentOrderTotal;
    private String selectedCouponCode = null;

    public interface OnCouponSelectedListener {
        void onCouponSelected(Coupon coupon);
    }

    public CouponSelectionAdapter(List<Coupon> coupons, double currentOrderTotal, OnCouponSelectedListener listener) {
        this.coupons = coupons;
        this.currentOrderTotal = currentOrderTotal;
        this.listener = listener;
    }

    public void setSelectedCouponCode(String code) {
        this.selectedCouponCode = code;
        notifyDataSetChanged();
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
        boolean isEligible = isCouponEligible(coupon);
        boolean isSelected = coupon.getCode().equals(selectedCouponCode);
        holder.bind(coupon, isEligible, isSelected, listener);
    }

    @Override
    public int getItemCount() {
        return coupons != null ? coupons.size() : 0;
    }

    private boolean isCouponEligible(Coupon coupon) {
        // Kiểm tra coupon có active không
        if (coupon.getIsActive() == null || !coupon.getIsActive()) {
            return false;
        }

        // Kiểm tra đơn tối thiểu
        if (coupon.getMinOrderValue() != null && currentOrderTotal < coupon.getMinOrderValue()) {
            return false;
        }

        return true;
    }

    static class CouponViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardCoupon;
        TextView tvCouponCode, tvDiscountValue, tvDescription, tvMinOrder, tvIneligibleNote;
        Button btnApply;
        ImageView ivSelected;

        public CouponViewHolder(@NonNull View itemView) {
            super(itemView);
            cardCoupon = itemView.findViewById(R.id.cardCoupon);
            tvCouponCode = itemView.findViewById(R.id.tvCouponCode);
            tvDiscountValue = itemView.findViewById(R.id.tvDiscountValue);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvMinOrder = itemView.findViewById(R.id.tvMinOrder);
            tvIneligibleNote = itemView.findViewById(R.id.tvIneligibleNote);
            btnApply = itemView.findViewById(R.id.btnApply);
            ivSelected = itemView.findViewById(R.id.ivSelected);
        }

        public void bind(Coupon coupon, boolean isEligible, boolean isSelected, OnCouponSelectedListener listener) {
            android.util.Log.d("CouponAdapter", "Binding coupon: " + coupon.getCode() + ", Eligible: " + isEligible);
            
            // Hiển thị mã voucher
            tvCouponCode.setText(coupon.getCode());

            // Hiển thị giá trị giảm
            NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
            String discountText = "";
            
            if ("PERCENTAGE".equals(coupon.getDiscountType()) || "PERCENT".equals(coupon.getDiscountType())) {
                if (coupon.getDiscountValue() != null) {
                    discountText = "Giảm " + coupon.getDiscountValue().intValue() + "%";
                    if (coupon.getMaxDiscountAmount() != null && coupon.getMaxDiscountAmount() > 0) {
                        discountText += " – tối đa " + formatter.format(coupon.getMaxDiscountAmount()) + "đ";
                    }
                }
            } else {
                if (coupon.getDiscountValue() != null) {
                    discountText = "Giảm " + formatter.format(coupon.getDiscountValue()) + "đ";
                }
            }
            tvDiscountValue.setText(discountText);

            // Hiển thị điều kiện đơn tối thiểu
            if (coupon.getMinOrderValue() != null && coupon.getMinOrderValue() > 0) {
                tvMinOrder.setText("Đơn tối thiểu " + formatter.format(coupon.getMinOrderValue()) + "đ");
            } else {
                tvMinOrder.setText("Không yêu cầu đơn tối thiểu");
            }

            // Hiển thị mô tả nếu có (tùy chọn)
            if (coupon.getDescription() != null && !coupon.getDescription().isEmpty()) {
                tvDescription.setVisibility(View.VISIBLE);
                tvDescription.setText(coupon.getDescription());
            } else {
                tvDescription.setVisibility(View.GONE);
            }

            // Xử lý trạng thái voucher
            if (!isEligible) {
                // Voucher không đủ điều kiện - hiển thị mờ
                cardCoupon.setAlpha(0.5f);
                btnApply.setEnabled(false);
                btnApply.setBackgroundColor(Color.parseColor("#CCCCCC"));
                btnApply.setText("Không đủ điều kiện");
                tvIneligibleNote.setVisibility(View.VISIBLE);
                ivSelected.setVisibility(View.GONE);
                
                // Không cho phép click
                btnApply.setOnClickListener(null);
            } else {
                // Voucher đủ điều kiện
                cardCoupon.setAlpha(1.0f);
                btnApply.setEnabled(true);
                btnApply.setBackgroundColor(Color.parseColor("#000000"));
                btnApply.setText("Dùng ngay");
                tvIneligibleNote.setVisibility(View.GONE);

                // Hiển thị trạng thái được chọn
                if (isSelected) {
                    cardCoupon.setStrokeWidth(3);
                    cardCoupon.setStrokeColor(Color.parseColor("#000000"));
                    ivSelected.setVisibility(View.VISIBLE);
                } else {
                    cardCoupon.setStrokeWidth(0);
                    ivSelected.setVisibility(View.GONE);
                }
                
                btnApply.setOnClickListener(v -> listener.onCouponSelected(coupon));
            }
        }
    }
}
