package com.example.nike_fe.service;

import android.content.Context;
import android.util.Log;

import com.example.nike_fe.data.model.ChatMessage;
import com.example.nike_fe.data.model.User;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton WebSocket Manager
 * - Maintains single WebSocket connection across app
 * - Tracks unread messages when chat popup is closed
 * - Notifies listeners of new messages
 * - Stores all messages for the session
 * - Tracks unread count per user for admin
 */
public class WebSocketChatManager {
    private static final String TAG = "WebSocketChatManager";
    private static WebSocketChatManager instance;
    
    private ChatWebSocketService webSocketService;
    private User currentUser;
    private int unreadCount = 0;
    private List<UnreadMessageListener> listeners = new ArrayList<>();
    private boolean isChatWindowOpen = false;
    
    // Store all messages for the session
    private static List<ChatMessage> allMessages = new ArrayList<>();
    
    // Track unread messages per user (for admin)
    private Map<Long, Integer> unreadPerUser = new HashMap<>();
    
    public interface UnreadMessageListener {
        void onUnreadCountChanged(int count);
        void onNewMessage(ChatMessage message);
        void onUserUnreadChanged(Long userId, int count); // New callback for per-user unread
    }
    
    private WebSocketChatManager() {}
    
    public static synchronized WebSocketChatManager getInstance() {
        if (instance == null) {
            instance = new WebSocketChatManager();
        }
        return instance;
    }
    
    public void initialize(Context context, User user) {
        if (currentUser != null && currentUser.getId().equals(user.getId())) {
            // Already initialized for this user
            return;
        }
        
        this.currentUser = user;
        Log.d(TAG, "Initializing WebSocket for user: " + user.getId());
        
        // Connect WebSocket
        if (webSocketService == null) {
            webSocketService = new ChatWebSocketService();
        }
        
        webSocketService.connect(user.getId(), user.getFullName(), new ChatWebSocketService.ChatListener() {
            @Override
            public void onMessageReceived(ChatMessage message) {
                Log.d(TAG, "📩 New message received: " + message.getContent());
                
                // Always add message to allMessages list
                allMessages.add(message);
                
                // If message is from others (not current user)
                if (!message.getSenderId().equals(currentUser.getId())) {
                    Long senderId = message.getSenderId();
                    
                    // Track unread per user for admin
                    int currentUnread = unreadPerUser.getOrDefault(senderId, 0);
                    unreadPerUser.put(senderId, currentUnread + 1);
                    notifyUserUnreadChanged(senderId, currentUnread + 1);
                    
                    // If chat window is closed, increment total unread
                    if (!isChatWindowOpen) {
                        unreadCount++;
                        notifyUnreadCountChanged();
                    }
                }
                
                // Notify all listeners
                notifyNewMessage(message);
            }
            
            @Override
            public void onConnectionStatus(boolean isConnected) {
                Log.d(TAG, "WebSocket connection status: " + isConnected);
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "WebSocket error: " + error);
            }
        });
    }
    
    public void setChatWindowOpen(boolean open) {
        this.isChatWindowOpen = open;
        if (open) {
            // Reset unread when chat window opens
            unreadCount = 0;
            notifyUnreadCountChanged();
        }
    }
    
    public int getUnreadCount() {
        return unreadCount;
    }
    
    public int getUnreadCountForUser(Long userId) {
        return unreadPerUser.getOrDefault(userId, 0);
    }
    
    public Map<Long, Integer> getAllUserUnreadCounts() {
        return new HashMap<>(unreadPerUser);
    }
    
    public void clearUnreadForUser(Long userId) {
        unreadPerUser.put(userId, 0);
        notifyUserUnreadChanged(userId, 0);
    }
    
    public List<ChatMessage> getAllMessages() {
        return new ArrayList<>(allMessages);
    }
    
    public void addMessageToHistory(ChatMessage message) {
        if (!allMessages.contains(message)) {
            allMessages.add(message);
        }
    }
    
    public void addListener(UnreadMessageListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    public void removeListener(UnreadMessageListener listener) {
        listeners.remove(listener);
    }
    
    private void notifyUnreadCountChanged() {
        for (UnreadMessageListener listener : listeners) {
            listener.onUnreadCountChanged(unreadCount);
        }
    }
    
    private void notifyUserUnreadChanged(Long userId, int count) {
        for (UnreadMessageListener listener : listeners) {
            listener.onUserUnreadChanged(userId, count);
        }
    }
    
    private void notifyNewMessage(ChatMessage message) {
        for (UnreadMessageListener listener : listeners) {
            listener.onNewMessage(message);
        }
    }
    
    public ChatWebSocketService getWebSocketService() {
        return webSocketService;
    }
    
    public void disconnect() {
        if (webSocketService != null) {
            webSocketService.disconnect();
            webSocketService = null;
        }
        currentUser = null;
        unreadCount = 0;
        unreadPerUser.clear();
        listeners.clear();
    }
    
    /**
     * Clear all chat data when user logs out
     */
    public void clearChatData() {
        allMessages.clear();
        unreadCount = 0;
        unreadPerUser.clear();
        notifyUnreadCountChanged();
        Log.d(TAG, "Chat data cleared");
    }
}
