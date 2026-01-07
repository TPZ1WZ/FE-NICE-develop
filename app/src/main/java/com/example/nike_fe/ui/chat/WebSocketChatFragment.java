package com.example.nike_fe.ui.chat;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nike_fe.R;
import com.example.nike_fe.adapter.ChatMessageAdapter;
import com.example.nike_fe.data.api.ChatApi;
import com.example.nike_fe.data.api.RetrofitClient;
import com.example.nike_fe.data.model.ChatMessage;
import com.example.nike_fe.data.model.User;
import com.example.nike_fe.service.ChatWebSocketService;
import com.example.nike_fe.service.WebSocketChatManager;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class WebSocketChatFragment extends DialogFragment {

    private static final String ARG_TARGET_USER_ID = "target_user_id";
    private static final String ARG_TARGET_USER_NAME = "target_user_name";

    // History loaded flag
    private static boolean historyLoaded = false;

    private RecyclerView rvMessages;
    private EditText etMessage;
    private ImageButton btnSend;
    private ImageView ivClose;
    private TextView tvDragHandle;
    private TextView tvStatus;
    private TextView tvChatTitle;
    private View rootView;

    private ChatMessageAdapter adapter;
    private ChatWebSocketService chatWebSocketService;
    private User currentUser;
    
    // Target user for admin chat
    private Long targetUserId;
    private String targetUserName;

    // For dragging
    private float dX, dY;
    private boolean isDragging = false;
    
    public static WebSocketChatFragment newInstance(Long targetUserId, String targetUserName) {
        WebSocketChatFragment fragment = new WebSocketChatFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_TARGET_USER_ID, targetUserId);
        args.putString(ARG_TARGET_USER_NAME, targetUserName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.ChatDialogTheme);
        
        // Get target user from arguments
        if (getArguments() != null) {
            targetUserId = getArguments().getLong(ARG_TARGET_USER_ID, -1L);
            targetUserName = getArguments().getString(ARG_TARGET_USER_NAME);
            if (targetUserId == -1L) {
                targetUserId = null;
            }
        }
        
        // Get current user from SharedPreferences
        loadCurrentUser();
    }
    
    private void loadCurrentUser() {
        // ALWAYS load fresh user data from API to avoid stale cached data
        android.util.Log.d("WebSocketChat", "Loading current user from API...");
        loadUserFromApi();
    }
    
    private void loadUserFromApi() {
        String token = com.example.nike_fe.data.api.RetrofitClient.getInstance(requireContext()).getToken();
        if (token == null || token.isEmpty()) {
            android.util.Log.e("WebSocketChat", "No token found!");
            if (tvStatus != null) {
                tvStatus.setText("Chưa đăng nhập");
            }
            return;
        }
        
        com.example.nike_fe.data.api.RetrofitClient.getInstance(requireContext())
            .getUserApi()
            .getProfile("Bearer " + token)
            .enqueue(new retrofit2.Callback<com.example.nike_fe.data.model.User>() {
                @Override
                public void onResponse(retrofit2.Call<com.example.nike_fe.data.model.User> call, 
                                     retrofit2.Response<com.example.nike_fe.data.model.User> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        currentUser = response.body();
                        android.util.Log.d("WebSocketChat", "✅ Loaded user from API: ID=" + currentUser.getId() + ", Name=" + currentUser.getFullName());
                        
                        // IMPORTANT: Update adapter with the actual currentUserId AND Role
                        if (adapter != null) {
                            String role = currentUser.getRole();
                            if (role == null) role = "MEMBER"; // Default role
                            adapter.setCurrentUser(currentUser.getId(), role);
                        }
                        
                        // Connect WebSocket after user loaded
                        connectWebSocket();
                        
                        // Also load chat history if not already done (in case it was skipped in onViewCreated)
                        if (!historyLoaded) {
                            loadChatHistory();
                            historyLoaded = true;
                        }
                    } else {
                        android.util.Log.e("WebSocketChat", "Failed to load user: " + response.code());
                    }
                }
                
                @Override
                public void onFailure(retrofit2.Call<com.example.nike_fe.data.model.User> call, Throwable t) {
                    android.util.Log.e("WebSocketChat", "Error loading user from API", t);
                }
            });
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        
        // Make dialog fixed size
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setGravity(Gravity.CENTER);
            
            WindowManager.LayoutParams params = window.getAttributes();
            // Fixed 85% screen size, centered
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.85);
            params.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.85);
            window.setAttributes(params);
        }
        
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_websocket_chat, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        rootView = view;
        initViews(view);
        setupRecyclerView();
        setupListeners();
        setupDraggable(view);
        
        // Connect WebSocket only if user already loaded
        if (currentUser != null) {
            // Only load history once per app session
            if (!historyLoaded) {
                loadChatHistory();
                historyLoaded = true;
            } else {
                // Scroll to bottom if messages already exist
                if (adapter != null && adapter.getItemCount() > 0) {
                    rvMessages.scrollToPosition(adapter.getItemCount() - 1);
                }
            }
            connectWebSocket();
        }
    }

    private void initViews(View view) {
        rvMessages = view.findViewById(R.id.rvMessages);
        etMessage = view.findViewById(R.id.etMessage);
        btnSend = view.findViewById(R.id.btnSend);
        ivClose = view.findViewById(R.id.ivClose);
        tvDragHandle = view.findViewById(R.id.tvDragHandle);
        tvStatus = view.findViewById(R.id.tvStatus);
        tvChatTitle = view.findViewById(R.id.tvChatTitle);
        
        // Set title based on target user
        if (targetUserName != null) {
            tvChatTitle.setText("Chat với " + targetUserName);
        } else {
            tvChatTitle.setText("Hỗ trợ trực tuyến");
        }
    }

    private void setupRecyclerView() {
        // Get messages from WebSocketManager
        List<ChatMessage> allMessages = WebSocketChatManager.getInstance().getAllMessages();
        List<ChatMessage> displayMessages = new ArrayList<>();
        
        // Filter messages if we are chatting with a specific user (Admin mode)
        if (targetUserId != null) {
            android.util.Log.d("WebSocketChat", "🎯 ADMIN MODE: Filtering for targetUserId=" + targetUserId);
            
            for (ChatMessage msg : allMessages) {
                Long senderId = msg.getSenderId();
                Long receiverId = msg.getReceiverId();
                
                // ADMIN MODE: Show ALL messages between admin and target user
                // Show if message involves target user (sent by them OR sent to them)
                boolean isFromTargetUser = senderId != null && senderId.equals(targetUserId);
                boolean isToTargetUser = receiverId != null && receiverId.equals(targetUserId);
                
                if (isFromTargetUser || isToTargetUser) {
                    displayMessages.add(msg);
                    android.util.Log.d("WebSocketChat", "✅ Added msg: sender=" + senderId + ", receiver=" + receiverId);
                }
            }
            android.util.Log.d("WebSocketChat", "📊 Filtered " + displayMessages.size() + " messages for target user: " + targetUserId);
        } else {
            // User mode - show all messages (assuming user only receives their own messages from backend anyway)
            // Or if we want strict filtering for User too:
            if (currentUser != null) {
                // Normally user only gets their own messages, but filtering adds safety
                 displayMessages.addAll(allMessages);
            } else {
                 displayMessages.addAll(allMessages);
            }
        }
        
        Long userId = currentUser != null ? currentUser.getId() : null;
        adapter = new ChatMessageAdapter(displayMessages, userId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(adapter);
    }

    private void setupListeners() {
        btnSend.setOnClickListener(v -> sendMessage());
        ivClose.setOnClickListener(v -> dismiss());
        
        etMessage.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupDraggable(View view) {
        if (tvDragHandle != null && getDialog() != null && getDialog().getWindow() != null) {
            View dialogView = getDialog().getWindow().getDecorView();
            
            tvDragHandle.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = dialogView.getX() - event.getRawX();
                        dY = dialogView.getY() - event.getRawY();
                        isDragging = false;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float newX = event.getRawX() + dX;
                        float newY = event.getRawY() + dY;
                        
                        if (Math.abs(newX - dialogView.getX()) > 10 ||
                            Math.abs(newY - dialogView.getY()) > 10) {
                            isDragging = true;
                        }
                        
                        if (isDragging) {
                            dialogView.animate()
                                .x(newX)
                                .y(newY)
                                .setDuration(0)
                                .start();
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                        if (!isDragging) {
                            v.performClick();
                        }
                        return true;
                }
                return false;
            });
        }
    }

    private void connectWebSocket() {
        if (currentUser == null) {
            android.util.Log.e("WebSocketChat", "Current user is null! Cannot connect.");
            if (tvStatus != null) {
                tvStatus.setText("Chưa đăng nhập");
            }
            Toast.makeText(getContext(), "Vui lòng đăng nhập để chat", Toast.LENGTH_SHORT).show();
            return;
        }

        android.util.Log.d("WebSocketChat", "Using WebSocket from Manager for user: " + currentUser.getId());
        
        // Use WebSocket service from Manager (already connected in background)
        chatWebSocketService = WebSocketChatManager.getInstance().getWebSocketService();
        
        if (chatWebSocketService == null) {
            android.util.Log.e("WebSocketChat", "WebSocket service not initialized in Manager!");
            if (tvStatus != null) {
                tvStatus.setText("Lỗi kết nối");
            }
            return;
        }
        
        // Listen for new messages and update UI
        WebSocketChatManager.getInstance().addListener(new WebSocketChatManager.UnreadMessageListener() {
            @Override
            public void onUnreadCountChanged(int count) {
                // Badge already handled by MainActivity
            }
            
            @Override
            public void onNewMessage(ChatMessage message) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (adapter != null) {
                            android.util.Log.d("WebSocketChat", "📨 Received message: senderId=" + message.getSenderId() + 
                                ", receiverId=" + message.getReceiverId() + ", content=" + message.getContent());
                            
                            // Filter real-time messages
                            boolean shouldAdd = true;
                            if (targetUserId != null) {
                                Long senderId = message.getSenderId();
                                Long receiverId = message.getReceiverId();
                                
                                android.util.Log.d("WebSocketChat", "🔍 Filtering for targetUserId=" + targetUserId);
                                
                                // ADMIN MODE: Show ALL messages involving target user
                                // Show if: (senderId == targetUserId) OR (receiverId == targetUserId)
                                boolean isFromTargetUser = senderId != null && senderId.equals(targetUserId);
                                boolean isToTargetUser = receiverId != null && receiverId.equals(targetUserId);
                                
                                shouldAdd = isFromTargetUser || isToTargetUser;
                                
                                android.util.Log.d("WebSocketChat", "✅ Filter result: shouldAdd=" + shouldAdd + 
                                    " (isFromTarget=" + isFromTargetUser + ", isToTarget=" + isToTargetUser + ")");
                            }
                            
                            if (shouldAdd) {
                                android.util.Log.d("WebSocketChat", "➕ Adding message to adapter: " + message.getContent());
                                adapter.addMessage(message);
                                rvMessages.scrollToPosition(adapter.getItemCount() - 1);
                            } else {
                                android.util.Log.d("WebSocketChat", "🚫 Message filtered out: " + message.getContent());
                            }
                        }
                    });
                }
            }
            
            @Override
            public void onUserUnreadChanged(Long userId, int count) {
                // Not needed in chat fragment
            }
        });
        
        if (tvStatus != null) {
            tvStatus.setText("Đang hoạt động");
        }
    }

    private void loadChatHistory() {
        if (currentUser == null) {
            return;
        }

        android.util.Log.d("WebSocketChat", "Loading chat history for user: " + currentUser.getId());
        
        RetrofitClient retrofitClient = RetrofitClient.getInstance(requireContext());
        ChatApi chatApi = retrofitClient.getChatApi();
        String token = retrofitClient.getToken();

        chatApi.getChatHistory("Bearer " + token).enqueue(new retrofit2.Callback<ChatApi.ApiResponse<List<ChatMessage>>>() {
            @Override
            public void onResponse(retrofit2.Call<ChatApi.ApiResponse<List<ChatMessage>>> call, 
                                 retrofit2.Response<ChatApi.ApiResponse<List<ChatMessage>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ChatApi.ApiResponse<List<ChatMessage>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        List<ChatMessage> history = apiResponse.getData();
                        android.util.Log.d("WebSocketChat", "Loaded " + history.size() + " messages from history");
                        
                        requireActivity().runOnUiThread(() -> {
                            if (adapter != null) {
                                // Add to WebSocketManager so messages persist
                                for (ChatMessage message : history) {
                                    WebSocketChatManager.getInstance().addMessageToHistory(message);
                                    adapter.addMessage(message);
                                }
                                if (!history.isEmpty()) {
                                    rvMessages.scrollToPosition(adapter.getItemCount() - 1);
                                }
                            }
                        });
                    } else {
                        android.util.Log.e("WebSocketChat", "API returned error: " + 
                            (apiResponse != null ? apiResponse.getMessage() : "unknown"));
                    }
                } else {
                    android.util.Log.e("WebSocketChat", "Failed to load history: " + response.code());
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ChatApi.ApiResponse<List<ChatMessage>>> call, Throwable t) {
                android.util.Log.e("WebSocketChat", "Error loading history", t);
            }
        });
    }

    private void sendMessage() {
        String message = etMessage.getText().toString().trim();
        if (message.isEmpty() || chatWebSocketService == null) {
            return;
        }

        // Clear input immediately
        etMessage.setText("");
        
        android.util.Log.d("WebSocketChat", "📤 Sending message: targetUserId=" + targetUserId + ", message=" + message);
        
        // Check if this is admin mode (chatting with specific user) or user mode (chatting with admin)
        if (targetUserId != null && targetUserName != null) {
            // ADMIN MODE: Send to specific user
            android.util.Log.d("WebSocketChat", "👨‍💼 Admin sending to user " + targetUserId);
            chatWebSocketService.sendMessageToUser(targetUserId, targetUserName, message, new ChatWebSocketService.ChatListener() {
                @Override
                public void onMessageReceived(ChatMessage msg) {
                    // Message will be received via WebSocket subscription
                }

                @Override
                public void onConnectionStatus(boolean isConnected) {
                    // Not used
                }

                @Override
                public void onError(String error) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Gửi thất bại: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } else {
            // USER MODE: Send to admin
            android.util.Log.d("WebSocketChat", "👤 User sending to admin");
            chatWebSocketService.sendMessageToAdmin(message, new ChatWebSocketService.ChatListener() {
                @Override
                public void onMessageReceived(ChatMessage msg) {
                    // Message will be received via WebSocket subscription
                }

                @Override
                public void onConnectionStatus(boolean isConnected) {
                    // Not used
                }

                @Override
                public void onError(String error) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Gửi thất bại: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        
        // Notify manager that chat window is closed
        WebSocketChatManager.getInstance().setChatWindowOpen(false);
        
        // Don't disconnect WebSocket anymore - keep it running in background
        // if (chatWebSocketService != null) {
        //     chatWebSocketService.disconnect();
        // }
    }
    
    /**
     * Clear chat history when user logs out
     */
    public static void clearChatHistory() {
        historyLoaded = false;
        WebSocketChatManager.getInstance().clearChatData();
    }
}
