package com.example.nike_fe.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nike_fe.R;
import com.example.nike_fe.data.api.RetrofitClient;
import com.example.nike_fe.data.model.User;
import com.example.nike_fe.service.WebSocketChatManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatUserAdapter extends RecyclerView.Adapter<ChatUserAdapter.UserViewHolder> {

    private List<User> users;
    private List<User> usersFiltered;
    private Context context;
    private OnUserClickListener listener;
    private Map<Long, Integer> unreadCounts;

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    public ChatUserAdapter(Context context, OnUserClickListener listener) {
        this.context = context;
        this.users = new ArrayList<>();
        this.usersFiltered = new ArrayList<>();
        this.listener = listener;
        this.unreadCounts = WebSocketChatManager.getInstance().getAllUserUnreadCounts();
    }

    public void setUsers(List<User> users) {
        this.users = users;
        this.usersFiltered = new ArrayList<>(users);
        sortUsersByUnread();
        notifyDataSetChanged();
    }
    
    public void updateUnreadCounts() {
        this.unreadCounts = WebSocketChatManager.getInstance().getAllUserUnreadCounts();
        sortUsersByUnread();
        notifyDataSetChanged();
    }
    
    private void sortUsersByUnread() {
        // Sort: users with unread messages first, then alphabetically
        Collections.sort(usersFiltered, new Comparator<User>() {
            @Override
            public int compare(User u1, User u2) {
                int unread1 = unreadCounts.getOrDefault(u1.getId(), 0);
                int unread2 = unreadCounts.getOrDefault(u2.getId(), 0);
                
                if (unread1 > 0 && unread2 == 0) return -1;
                if (unread1 == 0 && unread2 > 0) return 1;
                if (unread1 > 0 && unread2 > 0) return Integer.compare(unread2, unread1); // More unread first
                
                return u1.getFullName().compareToIgnoreCase(u2.getFullName());
            }
        });
    }

    public void filter(String query) {
        usersFiltered.clear();
        if (query.isEmpty()) {
            usersFiltered.addAll(users);
        } else {
            String lowerQuery = query.toLowerCase();
            for (User user : users) {
                if (user.getFullName().toLowerCase().contains(lowerQuery) ||
                    user.getEmail().toLowerCase().contains(lowerQuery)) {
                    usersFiltered.add(user);
                }
            }
        }
        sortUsersByUnread();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = usersFiltered.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return usersFiltered.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivUserAvatar;
        TextView tvUserName;
        TextView tvUserEmail;
        View vOnlineIndicator;
        TextView tvUnreadBadge;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            vOnlineIndicator = itemView.findViewById(R.id.vOnlineIndicator);
            tvUnreadBadge = itemView.findViewById(R.id.tvUnreadBadge);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onUserClick(usersFiltered.get(position));
                }
            });
        }

        void bind(User user) {
            tvUserName.setText(user.getFullName());
            tvUserEmail.setText(user.getEmail());

            // Load avatar
            if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                String avatarUrl = user.getAvatar();
                if (!avatarUrl.startsWith("http")) {
                    if (avatarUrl.startsWith("/")) {
                        avatarUrl = avatarUrl.substring(1);
                    }
                    avatarUrl = RetrofitClient.getInstance(context).getBaseUrl() + avatarUrl;
                }
                
                // Debug log
                android.util.Log.d("ChatUserAdapter", "Loading avatar for " + user.getFullName() + ": " + avatarUrl);
                
                Glide.with(context)
                        .load(avatarUrl)
                        .placeholder(R.drawable.ic_user_placeholder)
                        .error(R.drawable.ic_user_placeholder)
                        .into(ivUserAvatar);
            } else {
                android.util.Log.d("ChatUserAdapter", "No avatar for " + user.getFullName());
                ivUserAvatar.setImageResource(R.drawable.ic_user_placeholder);
            }

            // Online indicator (hidden for now)
            vOnlineIndicator.setVisibility(View.GONE);
            
            // Unread badge
            int unreadCount = unreadCounts.getOrDefault(user.getId(), 0);
            if (unreadCount > 0) {
                tvUnreadBadge.setText(String.valueOf(unreadCount));
                tvUnreadBadge.setVisibility(View.VISIBLE);
            } else {
                tvUnreadBadge.setVisibility(View.GONE);
            }
        }
    }
}
