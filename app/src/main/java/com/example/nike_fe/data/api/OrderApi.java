package com.example.nike_fe.data.api;

import com.example.nike_fe.data.model.Order;
import com.example.nike_fe.data.model.OrderRequest;
import com.example.nike_fe.data.model.OrderResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface OrderApi {
    
    @POST("api/v1/orders")
    Call<OrderResponse> createOrder(
        @Header("Authorization") String token,
        @Body OrderRequest orderRequest
    );
    
    @GET("api/v1/orders")
    Call<List<Order>> getUserOrders(
        @Header("Authorization") String token
    );
    
    @GET("api/v1/orders/{id}")
    Call<Order> getOrderById(
        @Header("Authorization") String token,
        @Path("id") Long orderId
    );
    
    @PATCH("api/v1/orders/{id}/cancel")
    Call<Map<String, Object>> cancelOrder(
        @Header("Authorization") String token,
        @Path("id") Long orderId
    );
}
