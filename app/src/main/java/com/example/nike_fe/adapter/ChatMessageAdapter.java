package com.example.nike_fe.adapter;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nike_fe.R;
import com.example.nike_fe.data.model.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private List<ChatMessage> messages;
    private String currentUserRole;
    private Long currentUserId;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public ChatMessageAdapter(List<ChatMessage> messages, Long currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    public void setCurrentUser(Long currentUserId, String role) {
        this.currentUserId = currentUserId;
        this.currentUserRole = role;
        notifyDataSetChanged();
    }
    
    // Kept for backward compatibility if needed, though setCurrentUser(id, role) is preferred
    public void setCurrentUserId(Long currentUserId) {
        this.currentUserId = currentUserId;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messages.get(position);
        
        // DEBUG LOGGING
        Log.d("ChatAdapter", "===============================================");
        Log.d("ChatAdapter", "Message #" + position + ": " + message.getContent());
        Log.d("ChatAdapter", "  senderId: " + message.getSenderId());
        Log.d("ChatAdapter", "  currentUserId: " + currentUserId);
        
        if (message.getSenderId() != null && message.getSenderId().equals(currentUserId)) {
            Log.d("ChatAdapter", "  → VIEW_TYPE_SENT (right, green)");
            return VIEW_TYPE_SENT;      // item_message_sent.xml (right, green)
        } else {
            Log.d("ChatAdapter", "  → VIEW_TYPE_RECEIVED (left, white)");
            return VIEW_TYPE_RECEIVED;   // item_message_received.xml (left, white)
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).bind(message, currentUserRole);
        } else if (holder instanceof ReceivedMessageViewHolder) {
            ((ReceivedMessageViewHolder) holder).bind(message, currentUserRole);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    public void clearMessages() {
        messages.clear();
        notifyDataSetChanged();
    }

    // ViewHolder for sent messages (green, right)
    class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView tvMessageContent;
        private TextView tvMessageTime;
        private TextView tvSenderName;

        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageContent = itemView.findViewById(R.id.tvMessageContent);
            tvMessageTime = itemView.findViewById(R.id.tvMessageTime);
            tvSenderName = itemView.findViewById(R.id.tvSenderName);
        }

        public void bind(ChatMessage message, String role) {
            tvMessageContent.setText(message.getContent());
            
            // Logic name display for SENT message
            if ("ADMIN".equalsIgnoreCase(role)) {
                // Admin sending -> Shows "ADMIN"
                tvSenderName.setText("ADMIN");
            } else {
                // User sending -> Shows real name or "You"
                String name = message.getSenderName();
                if (name == null || name.isEmpty()) {
                    name = "You";
                }
                tvSenderName.setText(name);
            }
            
            // Format time
            String time = timeFormat.format(new Date());
            if (message.getSentAt() != null && message.getSentAt().length() >= 16) {
                try {
                     time = message.getSentAt().substring(11, 16); // Extract HH:mm from ISO format
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            tvMessageTime.setText(time);
        }
    }

    // ViewHolder for received messages (white, left)
    class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView tvMessageContent;
        private TextView tvMessageTime;
        private TextView tvSenderName;

        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageContent = itemView.findViewById(R.id.tvMessageContent);
            tvMessageTime = itemView.findViewById(R.id.tvMessageTime);
            tvSenderName = itemView.findViewById(R.id.tvSenderName);
        }

        public void bind(ChatMessage message, String role) {
            tvMessageContent.setText(message.getContent());

            // Logic name display for RECEIVED message
            if ("ADMIN".equalsIgnoreCase(role)) {
                // Admin receiving -> Shows sender's real name (User)
                String name = message.getSenderName();
                if (name == null || name.isEmpty()) {
                    name = "User";
                }
                tvSenderName.setText(name);
            } else {
                // User receiving (from Admin) -> Shows "ADMIN"
                tvSenderName.setText("ADMIN");
            }
            
            // Format time
            String time = timeFormat.format(new Date());
            if (message.getSentAt() != null && message.getSentAt().length() >= 16) {
                try {
                     time = message.getSentAt().substring(11, 16); // Extract HH:mm from ISO format
                } catch (Exception e) {
                     e.printStackTrace();
                }
            }
            tvMessageTime.setText(time);
        }
    }
}
