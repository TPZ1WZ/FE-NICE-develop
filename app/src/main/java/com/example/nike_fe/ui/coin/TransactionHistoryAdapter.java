package com.example.nike_fe.ui.coin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nike_fe.R;
import com.example.nike_fe.data.model.TransactionHistoryItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionHistoryAdapter extends RecyclerView.Adapter<TransactionHistoryAdapter.ViewHolder> {

    private List<TransactionHistoryItem> transactions = new ArrayList<>();

    public void setTransactions(List<TransactionHistoryItem> transactions) {
        this.transactions = transactions != null ? transactions : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TransactionHistoryItem item = transactions.get(position);
        
        // Set title based on source
        String title = getTransactionTitle(item.getSource(), item.getDescription());
        holder.tvTitle.setText(title);
        
        // Format date
        String formattedDate = formatDate(item.getCreatedAt());
        holder.tvDate.setText(formattedDate);
        
        // Format amount with color
        Integer amount = item.getAmount();
        if (amount != null) {
            String amountText = String.format(Locale.getDefault(), "%+,d", amount);
            holder.tvAmount.setText(amountText);
            
            // Color: green for positive, red for negative
            int color = amount > 0 ? 0xFF4CAF50 : 0xFFF44336;
            holder.tvAmount.setTextColor(color);
        }
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    private String getTransactionTitle(String source, String description) {
        if (description != null && !description.isEmpty()) {
            // Chuyển "Daily checkin Day 2 reward" -> "Điểm danh ngày 2"
            if (description.contains("Daily checkin Day")) {
                String dayNumber = description.replaceAll("[^0-9]", "");
                return "Điểm danh ngày " + dayNumber;
            }
            
            // Chuyển các description khác sang tiếng Việt
            if (description.contains("order") || description.contains("Order")) {
                return "Sử dụng Nike Coin cho đơn hàng";
            }
            
            return description;
        }
        
        switch (source) {
            case "DAILY_CHECKIN":
                return "Điểm danh hàng ngày";
            case "ORDER":
                return "Sử dụng Nike Coin";
            case "REVIEW":
                return "Đánh giá sản phẩm";
            case "ADMIN":
                return "Quản trị viên cộng";
            default:
                return source;
        }
    }

    private String formatDate(String isoDate) {
        try {
            // Parse ISO date: 2026-01-05T10:30:00
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(isoDate);
            return date != null ? outputFormat.format(date) : isoDate;
        } catch (Exception e) {
            return isoDate;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvDate;
        TextView tvAmount;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTransactionTitle);
            tvDate = itemView.findViewById(R.id.tvTransactionDate);
            tvAmount = itemView.findViewById(R.id.tvTransactionAmount);
        }
    }
}
