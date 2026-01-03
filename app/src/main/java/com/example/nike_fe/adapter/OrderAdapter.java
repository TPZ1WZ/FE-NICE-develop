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
import android.widget.Button;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.example.nike_fe.data.model.Order;
import com.example.nike_fe.data.model.OrderItem;

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

        void onBuyAgainClick(Order order);

        void onReviewClick(Order order);
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

        // Handle Product Info from first item
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            OrderItem firstItem = order.getItems().get(0);

            // Hiển thị tên sản phẩm đầu tiên và số sản phẩm còn lại
            int totalItems = order.getItems().size();
            if (totalItems > 1) {
                holder.tvProductName.setText(firstItem.getProductName() + " (+" + (totalItems - 1) + " sản phẩm)");
            } else {
                holder.tvProductName.setText(firstItem.getProductName());
            }

            // Hiển thị thông tin size và tổng số lượng của đơn hàng
            String size = firstItem.getSize() != null ? firstItem.getSize() : "--";
            int totalQty = order.getQuantity() != null ? order.getQuantity() : 0;
            holder.tvProductDetails.setText("Size = " + size + " | Số lương = " + totalQty);

            // Image
            String imageUrl = firstItem.getFirstImage();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_placeholder)
                        .into(holder.ivProductImage);
            } else {
                holder.ivProductImage.setImageResource(R.drawable.ic_image_placeholder);
            }
        } else {
            holder.tvProductName.setText("Đơn hàng #" + order.getId());
            holder.tvProductDetails.setText("Không có sản phẩm");
            holder.ivProductImage.setImageResource(R.drawable.ic_image_placeholder);
        }

        // Hiển thị tổng tiền cuối cùng (đã bao gồm giảm giá và phí ship)
        holder.tvOrderTotal.setText(formatPrice(order.getFinalAmount()));

        // Status
        String status = order.getStatus();
        holder.tvOrderStatus.setText(getStatusText(status));

        // Handle Action Buttons visibility
        boolean showButtons = isOrderCompleted(status);
        holder.layoutActionButtons.setVisibility(showButtons ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOrderClick(order);
            }
        });

        holder.btnBuyAgain.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBuyAgainClick(order);
            }
        });

        holder.btnLeaveReview.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReviewClick(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    private boolean isOrderCompleted(String status) {
        if (status == null)
            return false;
        String s = status.toUpperCase();
        // Chỉ hiển thị nút "Mua lại" và "Đánh giá" cho đơn hàng đã hoàn thành hoặc đã giao
        // KHÔNG hiển thị cho đơn đã hủy
        return s.equals("COMPLETED") || s.equals("DELIVERED");
    }

    private String formatPrice(Double price) {
        if (price == null)
            return "0 ₫";
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(price) + " ₫";
    }

    private String formatDate(String dateStr) {
        if (dateStr == null)
            return "";
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
        if (status == null)
            return "Không xác định";
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
                return "Hoàn tất";
            case "CANCELED":
            case "CANCELLED":
                return "Đã hủy";
            default:
                return status;
        }
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName, tvProductDetails, tvOrderStatus, tvOrderTotal;
        Button btnBuyAgain, btnLeaveReview;
        View layoutActionButtons;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductDetails = itemView.findViewById(R.id.tvProductDetails);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
            layoutActionButtons = itemView.findViewById(R.id.layoutActionButtons);
            btnBuyAgain = itemView.findViewById(R.id.btnBuyAgain);
            btnLeaveReview = itemView.findViewById(R.id.btnLeaveReview);
        }
    }
}
