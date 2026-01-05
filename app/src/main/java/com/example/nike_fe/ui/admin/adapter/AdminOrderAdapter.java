package com.example.nike_fe.ui.admin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nike_fe.R;
import com.example.nike_fe.data.model.Order;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.example.nike_fe.data.model.OrderItem;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.OrderViewHolder> {

    private List<Order> orders;
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public AdminOrderAdapter(List<Order> orders, OnOrderClickListener listener) {
        this.orders = orders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order, listener);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public void updateData(List<Order> newOrders) {
        this.orders = newOrders;
        notifyDataSetChanged();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView tvOrderId, tvCustomer, tvPhone, tvStatus, tvAmount;
        private TextView tvPaymentMethod, tvCreatedAt, tvQuantity;
        private TextView tvProductName;
        private ImageView ivProductImage;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            // In the new layout, the root is MaterialCardView which extends CardView
            if (itemView instanceof CardView) {
                cardView = (CardView) itemView;
            } else {
                // If specific ID is used for card click
                // cardView = itemView.findViewById(R.id.cardView);
            }

            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvCustomer = itemView.findViewById(R.id.tvCustomer);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvPaymentMethod = itemView.findViewById(R.id.tvPaymentMethod);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
        }

        public void bind(Order order, OnOrderClickListener listener) {
            try {
                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

                tvOrderId.setText("#" + (order.getId() != null ? order.getId() : "N/A"));
                tvCustomer.setText(order.getReceiverName() != null ? order.getReceiverName()
                        : (order.getUserName() != null ? order.getUserName() : "N/A"));

                String phone = order.getPhone() != null ? order.getPhone()
                        : (order.getUser() != null ? order.getUser().getPhone() : "N/A");
                tvPhone.setText(phone);

                String status = order.getStatus() != null ? order.getStatus() : "pending";
                tvStatus.setText(getStatusText(status));
                tvStatus.setBackgroundResource(getStatusBackground(status));

                // Tính finalAmount từ các thành phần để đảm bảo đúng
                Double totalAmount = order.getTotalAmount() != null ? order.getTotalAmount() : 0.0;
                Double totalDiscount = order.getTotalDiscount() != null ? order.getTotalDiscount() : 0.0;
                Integer nikeCoinUsed = order.getNikeCoinUsed() != null ? order.getNikeCoinUsed() : 0;
                Double shippingFee = order.getShippingFee() != null ? order.getShippingFee() : 0.0;
                Double finalAmount = totalAmount - totalDiscount - nikeCoinUsed + shippingFee;
                tvAmount.setText(currencyFormat.format(finalAmount));

                tvPaymentMethod.setText(order.getPaymentMethod() != null ? order.getPaymentMethod() : "COD");
                tvCreatedAt.setText(formatDate(order.getCreatedAt()));

                // Handle Product Info
                List<OrderItem> items = order.getItems();
                if (items != null && !items.isEmpty()) {
                    OrderItem first = items.get(0);
                    int totalItems = items.size();

                    if (totalItems > 1) {
                        tvProductName.setText(first.getProductName() + " (+" + (totalItems - 1) + " khác)");
                    } else {
                        tvProductName.setText(first.getProductName());
                    }

                    String imgUrl = first.getFirstImage();
                    if (imgUrl != null && !imgUrl.isEmpty()) {
                        Glide.with(itemView.getContext())
                                .load(imgUrl)
                                .placeholder(R.drawable.ic_image_placeholder)
                                .error(R.drawable.ic_image_placeholder)
                                .into(ivProductImage);
                    } else {
                        ivProductImage.setImageResource(R.drawable.ic_image_placeholder);
                    }

                    // Fallback for quantity if order.quantity is not set
                    int qty = order.getQuantity() != null ? order.getQuantity() : totalItems;
                    tvQuantity.setText(qty + " sản phẩm");
                } else {
                    tvProductName.setText("Không có sản phẩm");
                    ivProductImage.setImageResource(R.drawable.ic_image_placeholder);
                    tvQuantity.setText("0 sản phẩm");
                }

                itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onOrderClick(order);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                tvOrderId.setText("#Err");
            }
        }

        private String getStatusText(String status) {
            if (status == null)
                return "N/A";
            switch (status.toUpperCase()) {
                case "PENDING":
                    return "Chờ xác nhận";
                case "CONFIRMED":
                    return "Đã xác nhận";
                case "PROCESSING":
                    return "Đang xử lý";
                case "SHIPPING":
                    return "Đang giao";
                case "DELIVERED":
                    return "Đã giao hàng";
                case "COMPLETED":
                    return "Hoàn tất";
                case "CANCELED":
                case "CANCELLED":
                    return "Đã hủy";
                default:
                    return status;
            }
        }

        private int getStatusBackground(String status) {
            if (status == null)
                return R.drawable.status_pending;
            switch (status.toUpperCase()) {
                case "PENDING":
                    return R.drawable.status_pending;
                case "CONFIRMED":
                    return R.drawable.status_confirmed;
                case "PROCESSING":
                case "SHIPPING":
                    return R.drawable.status_shipping;
                case "DELIVERED":
                case "COMPLETED":
                    return R.drawable.status_completed;
                case "CANCELED":
                case "CANCELLED":
                    return R.drawable.status_canceled;
                default:
                    return R.drawable.status_pending;
            }
        }

        private String formatDate(String dateString) {
            if (dateString == null || dateString.isEmpty()) {
                return "";
            }
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date date = inputFormat.parse(dateString);
                return date != null ? outputFormat.format(date) : dateString;
            } catch (ParseException e) {
                return dateString;
            }
        }
    }
}
