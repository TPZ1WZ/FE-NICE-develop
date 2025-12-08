package com.example.nike_fe.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nike_fe.R;
import com.example.nike_fe.data.model.Order;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    
    private Context context;
    private List<Order> orderList;
    private OnOrderClickListener listener;
    
    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }
    
    public OrderAdapter(Context context, List<Order> orderList, OnOrderClickListener listener) {
        this.context = context;
        this.orderList = orderList;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        
        holder.tvOrderId.setText("Đơn hàng #" + order.getId());
        holder.tvOrderDate.setText(formatDate(order.getCreatedAt()));
        holder.tvOrderTotal.setText(formatPrice(order.getFinalAmount()));
        holder.tvOrderQuantity.setText(order.getQuantity() + " sản phẩm");
        
        // Status
        String status = order.getStatus();
        holder.tvOrderStatus.setText(getStatusText(status));
        holder.tvOrderStatus.setTextColor(getStatusColor(status));
        
        // Payment method
        String paymentMethod = order.getPaymentMethod();
        if ("COD".equalsIgnoreCase(paymentMethod)) {
            holder.tvPaymentMethod.setText("COD");
        } else if ("VNPAY".equalsIgnoreCase(paymentMethod)) {
            holder.tvPaymentMethod.setText("VNPay");
        } else {
            holder.tvPaymentMethod.setText(paymentMethod);
        }
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOrderClick(order);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return orderList.size();
    }
    
    private String formatPrice(Double price) {
        if (price == null) return "0 ₫";
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(price) + " ₫";
    }
    
    private String formatDate(String dateStr) {
        if (dateStr == null) return "";
        
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateStr;
        }
    }
    
    private String getStatusText(String status) {
        if (status == null) return "Không xác định";
        
        switch (status.toUpperCase()) {
            case "PENDING":
                return "Chờ xác nhận";
            case "CONFIRMED":
                return "Đã xác nhận";
            case "PROCESSING":
                return "Đang xử lý";
            case "SHIPPING":
                return "Đang giao hàng";
            case "DELIVERED":
                return "Đã giao hàng";
            case "COMPLETED":
                return "Hoàn thành";
            case "CANCELLED":
                return "Đã hủy";
            default:
                return status;
        }
    }
    
    private int getStatusColor(String status) {
        if (status == null) return Color.parseColor("#6B7280");
        
        switch (status.toUpperCase()) {
            case "PENDING":
                return Color.parseColor("#F59E0B"); // Orange
            case "CONFIRMED":
            case "PROCESSING":
                return Color.parseColor("#3B82F6"); // Blue
            case "SHIPPING":
                return Color.parseColor("#8B5CF6"); // Purple
            case "DELIVERED":
            case "COMPLETED":
                return Color.parseColor("#10B981"); // Green
            case "CANCELLED":
                return Color.parseColor("#EF4444"); // Red
            default:
                return Color.parseColor("#6B7280"); // Gray
        }
    }
    
    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderDate, tvOrderStatus, tvOrderTotal, tvOrderQuantity, tvPaymentMethod;
        
        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
            tvOrderQuantity = itemView.findViewById(R.id.tvOrderQuantity);
            tvPaymentMethod = itemView.findViewById(R.id.tvPaymentMethod);
        }
    }
}
