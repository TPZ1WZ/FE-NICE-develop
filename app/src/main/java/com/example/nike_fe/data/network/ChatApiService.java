package com.example.nike_fe.data.network;

import com.example.nike_fe.data.model.ChatRequest;
import com.example.nike_fe.data.model.ChatResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ChatApiService {
    
    @POST("api/v1/chat")
    Call<ChatResponse> sendMessage(@Body ChatRequest request);
    
    @GET("api/v1/chat/health")
    Call<Object> healthCheck();
}
