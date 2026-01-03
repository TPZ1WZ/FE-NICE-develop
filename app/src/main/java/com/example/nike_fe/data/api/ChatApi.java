package com.example.nike_fe.data.api;

import com.example.nike_fe.data.model.ChatMessage;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.*;

public interface ChatApi {

    @GET("api/v1/chat/history")
    Call<ApiResponse<List<ChatMessage>>> getChatHistory(@Header("Authorization") String token);

    @GET("api/v1/chat/unread-count")
    Call<ApiResponse<Long>> getUnreadCount(@Header("Authorization") String token);

    @POST("api/v1/chat/messages/{messageId}/read")
    Call<ApiResponse<String>> markAsRead(
            @Header("Authorization") String token,
            @Path("messageId") Long messageId
    );

    @POST("api/v1/chat/send")
    Call<ApiResponse<ChatMessage>> sendMessage(
            @Header("Authorization") String token,
            @Body Map<String, String> request
    );

    class ApiResponse<T> {
        private boolean success;
        private T data;
        private String message;

        public boolean isSuccess() { return success; }
        public T getData() { return data; }
        public String getMessage() { return message; }
    }
}
