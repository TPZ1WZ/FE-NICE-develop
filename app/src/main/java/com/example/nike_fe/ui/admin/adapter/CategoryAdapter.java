package com.example.nike_fe.ui.admin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nike_fe.R;
import com.example.nike_fe.data.model.Category;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    
    private List<Category> categories;
    private OnCategoryActionListener listener;
    
    public interface OnCategoryActionListener {
        void onEdit(Category category);
        void onDelete(Category category);
    }
    
    public CategoryAdapter(List<Category> categories, OnCategoryActionListener listener) {
        this.categories = categories;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.bind(category, listener);
    }
    
    @Override
    public int getItemCount() {
        return categories.size();
    }
    
    public void updateData(List<Category> newCategories) {
        this.categories = newCategories;
        notifyDataSetChanged();
    }
    
    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCategoryId, tvName, tvDescription, tvCreatedAt, tvUpdatedAt;
        private ImageView btnEdit, btnDelete;
        
        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryId = itemView.findViewById(R.id.tvCategoryId);
            tvName = itemView.findViewById(R.id.tvCategoryName);
            tvDescription = itemView.findViewById(R.id.tvCategoryDescription);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
            tvUpdatedAt = itemView.findViewById(R.id.tvUpdatedAt);
            btnEdit = itemView.findViewById(R.id.btnEditCategory);
            btnDelete = itemView.findViewById(R.id.btnDeleteCategory);
        }
        
        public void bind(Category category, OnCategoryActionListener listener) {
            // Set ID
            tvCategoryId.setText("Mã: " + category.getId());
            
            // Set Name
            tvName.setText(category.getName());
            
            // Set Description
            if (category.getDescription() != null && !category.getDescription().isEmpty()) {
                tvDescription.setText(category.getDescription());
                tvDescription.setVisibility(View.VISIBLE);
            } else {
                tvDescription.setText("Không có mô tả");
                tvDescription.setVisibility(View.VISIBLE);
            }
            
            // Set Created Date
            tvCreatedAt.setText(formatDate(category.getCreatedAt()));
            
            // Set Updated Date
            tvUpdatedAt.setText(formatDate(category.getUpdatedAt()));
            
            // Set click listeners
            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEdit(category);
                }
            });
            
            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDelete(category);
                }
            });
        }
        
        private String formatDate(String dateString) {
            if (dateString == null || dateString.isEmpty()) {
                return "N/A";
            }
            
            try {
                // Parse ISO 8601 format: 2025-12-10T15:30:00
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                Date date = inputFormat.parse(dateString);
                return date != null ? outputFormat.format(date) : dateString;
            } catch (ParseException e) {
                // If parsing fails, try without time
                try {
                    SimpleDateFormat inputFormat2 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    Date date = inputFormat2.parse(dateString);
                    return date != null ? outputFormat.format(date) : dateString;
                } catch (ParseException ex) {
                    return dateString;
                }
            }
        }
    }
}
