package com.example.nike_fe.ui.chat;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nike_fe.R;
import com.example.nike_fe.adapter.ChatUserAdapter;
import com.example.nike_fe.data.api.RetrofitClient;
import com.example.nike_fe.data.api.UserApi;
import com.example.nike_fe.data.model.User;
import com.example.nike_fe.data.model.ChatMessage;
import com.example.nike_fe.service.WebSocketChatManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatRoomListFragment extends DialogFragment {

    private RecyclerView rvUserList;
    private EditText etSearch;
    private ImageView ivClose;
    private TextView tvDragHandle;
    private TextView tvUserCount;
    private View layoutEmpty;
    private View rootView;

    private ChatUserAdapter adapter;
    private WebSocketChatManager.UnreadMessageListener unreadListener;

    // For dragging
    private float dX, dY;
    private boolean isDragging = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.ChatDialogTheme);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setGravity(Gravity.BOTTOM | Gravity.END);

            WindowManager.LayoutParams params = window.getAttributes();
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.85);
            params.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.65);
            params.x = 20;
            params.y = 100;
            window.setAttributes(params);
        }

        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_room_list, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rootView = view;
        initViews(view);
        setupRecyclerView();
        setupListeners();
        // setupDraggable(view); // Disabled - popup fixed in place
        setupWebSocketListener();
        loadUsers();
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unreadListener != null) {
            WebSocketChatManager.getInstance().removeListener(unreadListener);
        }
    }

    private void initViews(View view) {
        rvUserList = view.findViewById(R.id.rvUserList);
        etSearch = view.findViewById(R.id.etSearch);
        ivClose = view.findViewById(R.id.ivClose);
        tvDragHandle = view.findViewById(R.id.tvDragHandle);
        tvUserCount = view.findViewById(R.id.tvUserCount);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
    }

    private void setupRecyclerView() {
        adapter = new ChatUserAdapter(requireContext(), user -> {
            // Open chat with selected user
            openChatWithUser(user);
        });

        rvUserList.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvUserList.setAdapter(adapter);
    }

    private void setupListeners() {
        ivClose.setOnClickListener(v -> dismiss());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private void setupWebSocketListener() {
        unreadListener = new WebSocketChatManager.UnreadMessageListener() {
            @Override
            public void onUnreadCountChanged(int count) {
                // Not needed for user list
            }
            
            @Override
            public void onNewMessage(ChatMessage message) {
                // Not needed for user list
            }
            
            @Override
            public void onUserUnreadChanged(Long userId, int count) {
                // Update adapter when user unread count changes
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (adapter != null) {
                            adapter.updateUnreadCounts();
                        }
                    });
                }
            }
        };
        WebSocketChatManager.getInstance().addListener(unreadListener);
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

    private void loadUsers() {
        String token = RetrofitClient.getInstance(requireContext()).getToken();
        if (token == null) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        UserApi userApi = RetrofitClient.getInstance(requireContext()).getUserApi();
        userApi.getAllUsers("Bearer " + token).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<User> users = response.body();
                    
                    if (users.isEmpty()) {
                        layoutEmpty.setVisibility(View.VISIBLE);
                        rvUserList.setVisibility(View.GONE);
                    } else {
                        layoutEmpty.setVisibility(View.GONE);
                        rvUserList.setVisibility(View.VISIBLE);
                        adapter.setUsers(users);
                        tvUserCount.setText(users.size() + " người dùng");
                    }
                } else {
                    Toast.makeText(requireContext(), "Không thể tải danh sách người dùng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Toast.makeText(requireContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openChatWithUser(User user) {
        // Clear unread count for this user
        WebSocketChatManager.getInstance().clearUnreadForUser(user.getId());
        
        // Close this dialog
        dismiss();

        // Open chat fragment with selected user
        WebSocketChatFragment chatFragment = WebSocketChatFragment.newInstance(user.getId(), user.getFullName());
        chatFragment.show(getParentFragmentManager(), "WebSocketChat");
    }
}
