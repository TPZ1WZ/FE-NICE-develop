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
        
        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvCustomer = itemView.findViewById(R.id.tvCustomer);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvPaymentMethod = itemView.findViewById(R.id.tvPaymentMethod);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
        }
        
        public void bind(Order order, OnOrderClickListener listener) {
            try {
                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
                
                tvOrderId.setText("#" + (order.getId() != null ? order.getId() : "N/A"));
                tvCustomer.setText(order.getUserName() != null && !order.getUserName().isEmpty() 
                    ? order.getUserName() : (order.getShippingAddress() != null ? order.getShippingAddress() : "N/A"));
                tvPhone.setText(order.getPhone() != null && !order.getPhone().isEmpty() 
                    ? order.getPhone() : "N/A");
                
                String status = order.getStatus() != null ? order.getStatus() : "pending";
                tvStatus.setText(getStatusText(status));
                tvStatus.setBackgroundResource(getStatusBackground(status));
                
                Double finalAmount = order.getFinalAmount() != null ? order.getFinalAmount() : 0.0;
                tvAmount.setText(currencyFormat.format(finalAmount));
                
                tvPaymentMethod.setText(order.getPaymentMethod() != null ? order.getPaymentMethod() : "N/A");
                tvCreatedAt.setText(formatDate(order.getCreatedAt()));
                
                Integer quantity = order.getQuantity() != null ? order.getQuantity() : 0;
                tvQuantity.setText(quantity + " sản phẩm");
            
                cardView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onOrderClick(order);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                tvOrderId.setText("#ERROR");
                tvCustomer.setText("Lỗi hiển thị");
            }
        }
        
        private String getStatusText(String status) {
            if (status == null) return "N/A";
            switch (status.toLowerCase()) {
                case "pending": return "Chờ xác nhận";
                case "confirmed": return "Đã xác nhận";
                case "shipping": return "Đang giao";
                case "completed": return "Hoàn tất";
                case "canceled": return "Đã hủy";
                default: return status;
            }
        }
        
        private int getStatusBackground(String status) {
            if (status == null) return R.drawable.status_pending;
            switch (status.toLowerCase()) {
                case "pending": return R.drawable.status_pending;
                case "confirmed": return R.drawable.status_confirmed;
                case "shipping": return R.drawable.status_shipping;
                case "completed": return R.drawable.status_completed;
                case "canceled": return R.drawable.status_canceled;
                default: return R.drawable.status_pending;
            }
        }
        
        private String formatDate(String dateString) {
            if (dateString == null || dateString.isEmpty()) {
                return "N/A";
            }
            
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                Date date = inputFormat.parse(dateString);
                return date != null ? outputFormat.format(date) : dateString;
            } catch (ParseException e) {
                return dateString;
            }
        }
    }
}
