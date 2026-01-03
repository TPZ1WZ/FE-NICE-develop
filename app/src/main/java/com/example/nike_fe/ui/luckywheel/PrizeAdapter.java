package com.example.nike_fe.ui.luckywheel;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nike_fe.R;
import com.example.nike_fe.data.model.Prize;

import java.util.List;

public class PrizeAdapter extends RecyclerView.Adapter<PrizeAdapter.PrizeViewHolder> {

    private Context context;
    private List<Prize> prizeList;

    public PrizeAdapter(Context context, List<Prize> prizeList) {
        this.context = context;
        this.prizeList = prizeList;
    }

    @NonNull
    @Override
    public PrizeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_prize, parent, false);
        return new PrizeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PrizeViewHolder holder, int position) {
        Prize prize = prizeList.get(position);
        
        holder.tvPrizeName.setText(prize.getName());
        holder.tvPrizeDescription.setText(prize.getDescription() != null ? 
            prize.getDescription() : "");

        // Set icon dựa vào type
        int iconRes = R.drawable.ic_voucher;
        switch (prize.getType()) {
            case "VOUCHER":
                iconRes = R.drawable.ic_voucher;
                break;
            case "FREESHIP":
                iconRes = R.drawable.ic_truck;
                break;
            case "POINTS":
                iconRes = R.drawable.ic_star_filled;
                break;
            case "NOTHING":
                iconRes = R.drawable.ic_heart;
                break;
        }
        holder.ivPrizeIcon.setImageResource(iconRes);
    }

    @Override
    public int getItemCount() {
        return prizeList.size();
    }

    static class PrizeViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPrizeIcon;
        TextView tvPrizeName;
        TextView tvPrizeDescription;

        public PrizeViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPrizeIcon = itemView.findViewById(R.id.ivPrizeIcon);
            tvPrizeName = itemView.findViewById(R.id.tvPrizeName);
            tvPrizeDescription = itemView.findViewById(R.id.tvPrizeDescription);
        }
    }
}
