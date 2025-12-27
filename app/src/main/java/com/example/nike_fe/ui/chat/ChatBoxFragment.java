package com.example.nike_fe.ui.chat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nike_fe.R;
import com.example.nike_fe.adapter.ChatMessageAdapter;
import com.example.nike_fe.data.model.ChatMessage;
import com.example.nike_fe.data.model.ChatRequest;
import com.example.nike_fe.data.model.ChatResponse;
import com.example.nike_fe.data.network.ApiClient;
import com.example.nike_fe.data.network.ChatApiService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatBoxFragment extends DialogFragment {

    private RecyclerView rvMessages;
    private EditText etMessage;
    private ImageButton btnSend;
    private ImageButton btnClose;
    private TextView tvDragHandle;
    private ProgressBar progressBar;
    private View rootView;

    private ChatMessageAdapter adapter;
    private List<ChatMessage> messageList = new ArrayList<>();
    private ChatApiService chatApiService;
    private String sessionId;

    // For dragging
    private float dX, dY;
    private boolean isDragging = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.ChatDialogTheme);
        
        // Initialize session ID
        sessionId = UUID.randomUUID().toString();
        
        // Initialize API service
        chatApiService = ApiClient.getInstance().create(ChatApiService.class);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        
        // Make dialog draggable
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setGravity(Gravity.BOTTOM | Gravity.END);
            
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            params.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.7);
            params.x = 20; // margin from right
            params.y = 100; // margin from bottom
            window.setAttributes(params);
        }
        
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_box, container, false);
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
        
        // Show welcome message
        addBotMessage("Xin chào! Tôi là trợ lý AI của Nike Store. Tôi có thể giúp gì cho bạn? 😊");
    }

    private void initViews(View view) {
        rvMessages = view.findViewById(R.id.rvMessages);
        etMessage = view.findViewById(R.id.etMessage);
        btnSend = view.findViewById(R.id.btnSend);
        btnClose = view.findViewById(R.id.btnClose);
        tvDragHandle = view.findViewById(R.id.tvDragHandle);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        adapter = new ChatMessageAdapter(messageList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(adapter);
    }

    private void setupListeners() {
        btnSend.setOnClickListener(v -> sendMessage());
        btnClose.setOnClickListener(v -> dismiss());
        
        etMessage.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupDraggable(View view) {
        tvDragHandle.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    dX = event.getRawX() - getDialog().getWindow().getAttributes().x;
                    dY = event.getRawY() + getDialog().getWindow().getAttributes().y;
                    isDragging = false;
                    return true;

                case MotionEvent.ACTION_MOVE:
                    isDragging = true;
                    WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
                    params.x = (int) (event.getRawX() - dX);
                    params.y = (int) (dY - event.getRawY());
                    getDialog().getWindow().setAttributes(params);
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

    private void sendMessage() {
        String message = etMessage.getText().toString().trim();
        if (message.isEmpty()) {
            return;
        }

        // Clear input
        etMessage.setText("");

        // Add user message to UI
        addUserMessage(message);

        // Show loading
        showLoading(true);

        // Create request
        ChatRequest request = new ChatRequest();
        request.setMessage(message);
        request.setSessionId(sessionId);

        // Call API
        chatApiService.sendMessage(request).enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(@NonNull Call<ChatResponse> call, @NonNull Response<ChatResponse> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ChatResponse chatResponse = response.body();
                    addBotMessage(chatResponse.getMessage());
                } else {
                    addBotMessage("Xin lỗi, đã có lỗi xảy ra. Vui lòng thử lại sau.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ChatResponse> call, @NonNull Throwable t) {
                showLoading(false);
                addBotMessage("Không thể kết nối đến server. Vui lòng kiểm tra kết nối internet.");
            }
        });
    }

    private void addUserMessage(String message) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setMessage(message);
        chatMessage.setFromUser(true);
        chatMessage.setTimestamp(System.currentTimeMillis());
        
        messageList.add(chatMessage);
        adapter.notifyItemInserted(messageList.size() - 1);
        rvMessages.scrollToPosition(messageList.size() - 1);
    }

    private void addBotMessage(String message) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setMessage(message);
        chatMessage.setFromUser(false);
        chatMessage.setTimestamp(System.currentTimeMillis());
        
        messageList.add(chatMessage);
        adapter.notifyItemInserted(messageList.size() - 1);
        rvMessages.scrollToPosition(messageList.size() - 1);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSend.setEnabled(!show);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            // Animate entrance
            View decorView = getDialog().getWindow().getDecorView();
            decorView.setAlpha(0f);
            decorView.setScaleX(0.8f);
            decorView.setScaleY(0.8f);
            decorView.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200)
                    .start();
        }
    }

    @Override
    public void dismiss() {
        // Animate exit
        if (getDialog() != null && getDialog().getWindow() != null) {
            View decorView = getDialog().getWindow().getDecorView();
            decorView.animate()
                    .alpha(0f)
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .setDuration(200)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            ChatBoxFragment.super.dismiss();
                        }
                    })
                    .start();
        } else {
            super.dismiss();
        }
    }
}
