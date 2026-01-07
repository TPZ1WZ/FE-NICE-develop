package com.example.nike_fe.service;

import android.util.Log;

import com.example.nike_fe.data.model.ChatMessage;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.LifecycleEvent;

public class ChatWebSocketService {
    private static final String TAG = "ChatWebSocket";
    // Use ws://10.0.2.2:8080 for EMULATOR
    // Use ws://192.168.1.108:8080 for REAL DEVICE
    private static final String WS_URL = "ws://10.0.2.2:8080/ws-chat/websocket";

    private StompClient stompClient;
    private CompositeDisposable compositeDisposable;
    private Gson gson;
    private boolean isConnected = false;
    private Long currentUserId;
    private String currentUserName;

    public ChatWebSocketService() {
        this.gson = new Gson();
        this.compositeDisposable = new CompositeDisposable();
    }

    public interface ChatListener {
        void onMessageReceived(ChatMessage message);
        void onConnectionStatus(boolean isConnected);
        void onError(String error);
    }

    /**
     * Connect to WebSocket
     */
    public void connect(Long userId, String userName, ChatListener listener) {
        this.currentUserId = userId;
        this.currentUserName = userName;

        Log.d(TAG, "🔌 Connecting to WebSocket for user: " + userId + ", userName: " + userName);
        Log.d(TAG, "🔌 WebSocket URL: " + WS_URL);

        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, WS_URL);

        // Lifecycle monitoring
        Disposable lifecycleDisposable = stompClient.lifecycle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(lifecycleEvent -> {
                    switch (lifecycleEvent.getType()) {
                        case OPENED:
                            Log.d(TAG, "✅ WebSocket connection opened");
                            Log.d(TAG, "🔔 About to subscribe for userId: " + currentUserId);
                            isConnected = true;
                            listener.onConnectionStatus(true);
                            subscribeToMessages(listener);
                            sendConnectNotification();
                            break;

                        case CLOSED:
                            Log.d(TAG, "WebSocket connection closed");
                            isConnected = false;
                            listener.onConnectionStatus(false);
                            break;

                        case ERROR:
                            Log.e(TAG, "WebSocket connection error", lifecycleEvent.getException());
                            isConnected = false;
                            listener.onError("Connection error: " + lifecycleEvent.getException().getMessage());
                            break;
                    }
                });

        compositeDisposable.add(lifecycleDisposable);

        // Connect
        stompClient.connect();
    }

    /**
     * Subscribe to admin message topic (group chat for all users)
     */
    private void subscribeToMessages(ChatListener listener) {
        Log.d(TAG, "📡 subscribeToMessages() called");
        Log.d(TAG, "📡 currentUserId: " + currentUserId);
        
        // Subscribe to admin topic to receive ALL messages in the group chat
        String destination = "/topic/admin/messages";
        Log.d(TAG, "📡 Subscribing to: " + destination);
        
        Disposable messageDisposable = stompClient.topic(destination)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(topicMessage -> {
                    Log.d(TAG, "✅ Received message from WebSocket: " + topicMessage.getPayload());
                    try {
                        ChatMessage message = gson.fromJson(topicMessage.getPayload(), ChatMessage.class);
                        listener.onMessageReceived(message);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing message", e);
                        listener.onError("Error parsing message");
                    }
                }, throwable -> {
                    Log.e(TAG, "❌ Error subscribing to " + destination, throwable);
                    listener.onError("Subscription error: " + throwable.getMessage());
                });

        compositeDisposable.add(messageDisposable);
        Log.d(TAG, "Subscription added for " + destination);
    }

    /**
     * Send message to admin
     */
    public void sendMessageToAdmin(String content, ChatListener listener) {
        if (!isConnected) {
            Log.w(TAG, "Not connected to WebSocket");
            if (listener != null) {
                listener.onError("Not connected");
            }
            return;
        }

        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("senderId", currentUserId);
        messageMap.put("senderName", currentUserName);
        messageMap.put("receiverId", 0);
        messageMap.put("content", content);
        messageMap.put("type", "TEXT");

        String jsonMessage = gson.toJson(messageMap);

        Log.d(TAG, "Sending message to admin: " + jsonMessage);

        // Create local message for optimistic update
        ChatMessage localMessage = new ChatMessage();
        localMessage.setSenderId(currentUserId);
        localMessage.setSenderName(currentUserName);
        localMessage.setReceiverId(0L);
        localMessage.setContent(content);
        localMessage.setType("TEXT");
        localMessage.setSentAt(java.time.LocalDateTime.now().toString());

        Disposable sendDisposable = stompClient.send("/app/chat.sendToAdmin", jsonMessage)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            Log.d(TAG, "Message sent successfully");
                            // Optimistic update - show message immediately
                            if (listener != null) {
                                listener.onMessageReceived(localMessage);
                            }
                        },
                        throwable -> {
                            Log.e(TAG, "Error sending message", throwable);
                            if (listener != null) {
                                listener.onError("Failed to send message");
                            }
                        }
                );

        compositeDisposable.add(sendDisposable);
    }

    /**
     * Admin sends message to specific user
     */
    public void sendMessageToUser(Long targetUserId, String targetUserName, String content, ChatListener listener) {
        if (!isConnected) {
            Log.w(TAG, "Not connected to WebSocket");
            if (listener != null) {
                listener.onError("Not connected");
            }
            return;
        }

        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("senderId", currentUserId); // Admin ID
        messageMap.put("senderName", currentUserName); // Admin name
        messageMap.put("receiverId", targetUserId); // Target user ID
        messageMap.put("receiverName", targetUserName); // Target user name
        messageMap.put("content", content);
        messageMap.put("type", "TEXT");

        String jsonMessage = gson.toJson(messageMap);

        Log.d(TAG, "💬 Admin sending message to user " + targetUserId + ": " + jsonMessage);

        Disposable sendDisposable = stompClient.send("/app/chat.sendToUser", jsonMessage)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            Log.d(TAG, "✅ Message sent successfully to user " + targetUserId);
                        },
                        throwable -> {
                            Log.e(TAG, "❌ Error sending message to user", throwable);
                            if (listener != null) {
                                listener.onError("Failed to send message");
                            }
                        }
                );

        compositeDisposable.add(sendDisposable);
    }

    /**
     * Send connection notification
     */
    private void sendConnectNotification() {
        Map<String, Object> connectData = new HashMap<>();
        connectData.put("userId", currentUserId);
        connectData.put("userName", currentUserName);
        connectData.put("type", "CONNECT");

        Disposable connectDisposable = stompClient.send("/app/chat.connect", gson.toJson(connectData))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> Log.d(TAG, "Connect notification sent"),
                        throwable -> Log.e(TAG, "Error sending connect notification", throwable)
                );

        compositeDisposable.add(connectDisposable);
    }

    /**
     * Disconnect from WebSocket
     */
    public void disconnect() {
        Log.d(TAG, "Disconnecting from WebSocket");

        if (stompClient != null && isConnected) {
            // Send disconnect notification
            Map<String, Object> disconnectData = new HashMap<>();
            disconnectData.put("userId", currentUserId);
            disconnectData.put("type", "DISCONNECT");

            stompClient.send("/app/chat.disconnect", gson.toJson(disconnectData))
                    .subscribe();

            stompClient.disconnect();
        }

        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }

        isConnected = false;
    }

    public boolean isConnected() {
        return isConnected;
    }
}
