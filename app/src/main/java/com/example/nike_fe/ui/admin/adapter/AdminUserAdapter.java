package com.example.nike_fe.ui.admin.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nike_fe.R;
import com.example.nike_fe.data.model.User;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.UserViewHolder> {

    private List<User> userList = new ArrayList<>();
    private OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClick(User user);

        void onMoreClick(User user, View view);
    }

    public void setOnUserClickListener(OnUserClickListener listener) {
        this.listener = listener;
    }

    public void setUsers(List<User> users) {
        this.userList = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.bind(user, listener);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivUserAvatar, btnMore;
        private TextView tvUserName, tvUserEmail, tvUserDate;
        private Chip chipRole, chipStatus;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvUserDate = itemView.findViewById(R.id.tvUserDate);
            chipRole = itemView.findViewById(R.id.chipRole);
            chipStatus = itemView.findViewById(R.id.chipStatus);
            btnMore = itemView.findViewById(R.id.btnMore);
        }

        public void bind(User user, OnUserClickListener listener) {
            if (user == null)
                return;

            Glide.with(itemView.getContext())
                    .load(user.getAvatarUrl())
                    .placeholder(R.drawable.ic_avatar_placeholder)
                    .error(R.drawable.ic_avatar_placeholder)
                    .into(ivUserAvatar);

            tvUserName.setText(user.getFullName() != null ? user.getFullName() : "Unknown User");
            tvUserEmail.setText(user.getEmail());
            tvUserDate.setText("Tham gia: " + (user.getCreatedAt() != null ? user.getCreatedAt() : "N/A"));

            // Role Badge
            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                chipRole.setText("ADMIN");
                chipRole.setChipBackgroundColor(
                        android.content.res.ColorStateList.valueOf(Color.parseColor("#DBEAFE"))); // Light Blue
                chipRole.setTextColor(Color.parseColor("#1D4ED8")); // Blue
            } else {
                chipRole.setText("MEMBER");
                chipRole.setChipBackgroundColorResource(android.R.color.transparent);
                chipRole.setTextColor(Color.parseColor("#374151"));
            }

            // Status Badge
            if (Boolean.TRUE.equals(user.getIsActive())) {
                chipStatus.setText("Active");
                chipStatus.setChipBackgroundColor(
                        android.content.res.ColorStateList.valueOf(Color.parseColor("#DCFCE7")));
                chipStatus.setTextColor(Color.parseColor("#166534"));
            } else {
                chipStatus.setText("Inactive");
                chipStatus.setChipBackgroundColor(
                        android.content.res.ColorStateList.valueOf(Color.parseColor("#FEE2E2")));
                chipStatus.setTextColor(Color.parseColor("#B91C1C"));
            }

            itemView.setOnClickListener(v -> {
                if (listener != null)
                    listener.onUserClick(user);
            });

            btnMore.setOnClickListener(v -> {
                if (listener != null)
                    listener.onMoreClick(user, btnMore);
            });
        }
    }
}
