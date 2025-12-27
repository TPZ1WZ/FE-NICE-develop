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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
            // Hiển thị code (mã coupon) thay vì name để tránh trùng lặp với description
            tvCouponCode.setText(coupon.getCode());
            
            // Hiển thị mô tả
            String descText = coupon.getDescription();
            if (descText == null || descText.isEmpty()) {
                descText = coupon.getName() != null ? coupon.getName() : "Không có mô tả";
            }
            tvCouponDescription.setText(descText);

            // Format Discount Value - sửa logic check discountType
            String discountType = coupon.getDiscountType();
            if (discountType != null && (discountType.equalsIgnoreCase("PERCENTAGE") || discountType.equalsIgnoreCase("PERCENT"))) {
                tvDiscountValue.setText("Giảm: " + String.format("%.0f", coupon.getDiscountValue()) + "%");
            } else {
                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
                tvDiscountValue.setText("Giảm: " + currencyFormat.format(coupon.getDiscountValue()));
            }

            // Expiry Date
            String expiryText = "Hết hạn: " + (coupon.getEndDate() != null ? coupon.getEndDate() : "Vô thời hạn");
            tvExpiryDate.setText(expiryText);

            // Status - kiểm tra cả isActive và ngày hết hạn
            Boolean activeStatus = coupon.getIsActive();
            boolean isActive = activeStatus != null && activeStatus;
            
            // Kiểm tra ngày hết hạn
            boolean isExpired = isExpiredCoupon(coupon.getEndDate());
            
            // Nếu hết hạn thì hiển thị "Hết hạn", ngược lại hiển thị theo trạng thái isActive
            if (isExpired) {
                chipStatus.setText("Hết hạn");
                chipStatus.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#FEE2E2")));
                chipStatus.setTextColor(Color.parseColor("#991B1B"));
            } else if (isActive) {
                chipStatus.setText("Đang hoạt động");
                chipStatus.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#DCFCE7")));
                chipStatus.setTextColor(Color.parseColor("#166534"));
            } else {
                chipStatus.setText("Không hoạt động");
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
        
        /**
         * Kiểm tra xem coupon có hết hạn không
         * @param endDateStr Ngày hết hạn dạng String (format: yyyy-MM-dd hoặc dd/MM/yyyy)
         * @return true nếu đã hết hạn, false nếu còn hạn hoặc không có ngày hết hạn
         */
        private boolean isExpiredCoupon(String endDateStr) {
            if (endDateStr == null || endDateStr.isEmpty()) {
                return false; // Không có ngày hết hạn = vô thời hạn
            }
            
            try {
                // Thử parse với nhiều format khác nhau
                SimpleDateFormat[] formats = {
                    new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
                    new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                };
                
                Date endDate = null;
                for (SimpleDateFormat format : formats) {
                    try {
                        endDate = format.parse(endDateStr);
                        break;
                    } catch (ParseException e) {
                        continue;
                    }
                }
                
                if (endDate == null) {
                    return false;
                }
                
                // So sánh với ngày hiện tại (bỏ qua giờ phút giây)
                Date currentDate = new Date();
                SimpleDateFormat dateOnly = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                
                Date endDateOnly = dateOnly.parse(dateOnly.format(endDate));
                Date currentDateOnly = dateOnly.parse(dateOnly.format(currentDate));
                
                // Hết hạn nếu endDate <= currentDate (ngày hết hạn đã qua hoặc là hôm nay)
                return !endDateOnly.after(currentDateOnly);
                
            } catch (Exception e) {
                android.util.Log.e("AdminCouponAdapter", "Error parsing date: " + endDateStr, e);
                return false;
            }
        }
    }
}
