package com.example.nike_fe.ui.home;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nike_fe.R;

import java.util.List;

public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.FilterViewHolder> {

    private Context context;
    private List<String> filters;
    private int selectedPosition = 0;

    private OnFilterClickListener listener;

    public interface OnFilterClickListener {
        void onFilterClick(String filter);
    }

    public FilterAdapter(Context context, List<String> filters, OnFilterClickListener listener) {
        this.context = context;
        this.filters = filters;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FilterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_filter, parent, false);
        return new FilterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FilterViewHolder holder, int position) {
        String name = filters.get(position);
        holder.tvFilterName.setText(name);

        if (selectedPosition == position) {
            holder.tvFilterName.setBackgroundResource(R.drawable.bg_filter_selected);
            holder.tvFilterName.setTextColor(Color.WHITE);
        } else {
            holder.tvFilterName.setBackgroundResource(R.drawable.bg_filter_unselected);
            holder.tvFilterName.setTextColor(Color.BLACK);
        }

        holder.itemView.setOnClickListener(v -> {
            int previous = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previous);
            notifyItemChanged(selectedPosition);

            if (listener != null) {
                listener.onFilterClick(filters.get(selectedPosition));
            }
        });
    }

    @Override
    public int getItemCount() {
        return filters.size();
    }

    public static class FilterViewHolder extends RecyclerView.ViewHolder {
        TextView tvFilterName;

        public FilterViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFilterName = itemView.findViewById(R.id.tvFilterName);
        }
    }
}
