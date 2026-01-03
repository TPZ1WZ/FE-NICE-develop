package com.example.nike_fe.ui.luckywheel;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nike_fe.R;
import com.example.nike_fe.data.model.SpinHistoryItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SpinHistoryAdapter extends RecyclerView.Adapter<SpinHistoryAdapter.ViewHolder> {

    private Context context;
    private List<SpinHistoryItem> historyList;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public SpinHistoryAdapter(Context context, List<SpinHistoryItem> historyList) {
        this.context = context;
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_spin_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SpinHistoryItem item = historyList.get(position);
        
        if (item.getPrize() != null) {
            holder.tvPrizeName.setText(item.getPrize().getName());
            
            // Set color badge
            try {
                int color = Color.parseColor(item.getPrize().getColor());
                holder.viewColorBadge.setBackgroundColor(color);
            } catch (Exception e) {
                holder.viewColorBadge.setBackgroundColor(Color.parseColor("#FF6B6B"));
            }
        }
        
        // Format time
        try {
            if (item.getSpinTime() != null) {
                // Parse ISO 8601 format
                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                Date date = isoFormat.parse(item.getSpinTime());
                holder.tvSpinTime.setText("🕐 " + dateFormat.format(date));
            }
        } catch (Exception e) {
            holder.tvSpinTime.setText("🕐 " + item.getSpinTime());
        }
        
        // Prize code
        if (item.getPrizeCode() != null && !item.getPrizeCode().isEmpty()) {
            holder.tvPrizeCode.setText("Mã: " + item.getPrizeCode());
            holder.tvPrizeCode.setVisibility(View.VISIBLE);
        } else {
            holder.tvPrizeCode.setVisibility(View.GONE);
        }
        
        // Claimed status
        if (item.getIsClaimed() != null && item.getIsClaimed()) {
            holder.tvStatus.setText("✅ Đã nhận");
            holder.tvStatus.setTextColor(Color.parseColor("#10B981"));
        } else {
            holder.tvStatus.setText("⏳ Chờ nhận");
            holder.tvStatus.setTextColor(Color.parseColor("#F59E0B"));
        }
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View viewColorBadge;
        TextView tvPrizeName;
        TextView tvSpinTime;
        TextView tvPrizeCode;
        TextView tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            viewColorBadge = itemView.findViewById(R.id.viewColorBadge);
            tvPrizeName = itemView.findViewById(R.id.tvPrizeName);
            tvSpinTime = itemView.findViewById(R.id.tvSpinTime);
            tvPrizeCode = itemView.findViewById(R.id.tvPrizeCode);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
