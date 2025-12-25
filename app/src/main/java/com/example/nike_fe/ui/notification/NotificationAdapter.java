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
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(NotificationItem notification);
    }

    public NotificationAdapter(Context context, List<NotificationItem> notifications,
            OnNotificationClickListener listener) {
        this.context = context;
        this.notifications = notifications;
        this.listener = listener;
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

    class NotificationViewHolder extends RecyclerView.ViewHolder {
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

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && NotificationAdapter.this.listener != null) {
                    NotificationAdapter.this.listener
                            .onNotificationClick(NotificationAdapter.this.notifications.get(position));
                }
            });
        }

        public void bind(NotificationItem notification) {
            tvTitle.setText(notification.getTitle());
            tvMessage.setText(notification.getMessage());
            tvTime.setText(notification.getTime());

            // Set icon based on type
            String type = notification.getType();
            if (type.startsWith("order")) {
                ivIcon.setImageResource(R.drawable.ic_truck);
            } else if (type.equals("promotion")) {
                ivIcon.setImageResource(R.drawable.ic_ticket);
            } else if (type.equals("delivery")) {
                ivIcon.setImageResource(R.drawable.ic_shopping_bag);
            } else {
                ivIcon.setImageResource(R.drawable.ic_notification);
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
