package com.example.nike_fe.ui.notification;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nike_fe.R;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private Context context;
    private List<NotificationItem> notifications;

    public NotificationAdapter(Context context, List<NotificationItem> notifications) {
        this.context = context;
        this.notifications = notifications;
    }

    public void setNotifications(List<NotificationItem> notifications) {
        this.notifications = notifications;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationItem notification = notifications.get(position);
        holder.bind(notification);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivIcon;
        private TextView tvTitle, tvMessage, tvTime;
        private View vUnreadIndicator;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivNotificationIcon);
            tvTitle = itemView.findViewById(R.id.tvNotificationTitle);
            tvMessage = itemView.findViewById(R.id.tvNotificationMessage);
            tvTime = itemView.findViewById(R.id.tvNotificationTime);
            vUnreadIndicator = itemView.findViewById(R.id.vUnreadIndicator);
        }

        public void bind(NotificationItem notification) {
            tvTitle.setText(notification.getTitle());
            tvMessage.setText(notification.getMessage());
            tvTime.setText(notification.getTime());

            // Set icon based on type
            switch (notification.getType()) {
                case "order":
                    ivIcon.setImageResource(R.drawable.ic_truck);
                    break;
                case "promotion":
                    ivIcon.setImageResource(R.drawable.ic_ticket);
                    break;
                case "delivery":
                    ivIcon.setImageResource(R.drawable.ic_shopping_bag);
                    break;
                default:
                    ivIcon.setImageResource(R.drawable.ic_notification);
                    break;
            }

            // Show/hide unread indicator
            vUnreadIndicator.setVisibility(notification.isRead() ? View.GONE : View.VISIBLE);

            // Set background for unread notifications
            if (!notification.isRead()) {
                itemView.setBackgroundColor(itemView.getContext().getColor(R.color.blue_50));
            } else {
                itemView.setBackgroundColor(itemView.getContext().getColor(R.color.white));
            }
        }
    }
}
