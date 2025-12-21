package com.example.nike_fe.data.api;

import com.example.nike_fe.data.model.AddToCartRequest;
import com.example.nike_fe.data.model.CartResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CartApi {

        @GET("/api/v1/carts")
        Call<CartResponse> getCart(@Header("Authorization") String token);

        @GET("/api/v1/carts/count")
        Call<Map<String, Integer>> getCartCount(@Header("Authorization") String token);

        @POST("/api/v1/carts/add")
        Call<Map<String, String>> addToCart(
                        @Header("Authorization") String token,
                        @Body AddToCartRequest request);

        @PATCH("/api/v1/carts/update")
        Call<Map<String, String>> updateQuantity(
                        @Header("Authorization") String token,
                        @Body AddToCartRequest request);

        @DELETE("/api/v1/carts/remove")
        Call<Map<String, String>> removeItem(
                        @Header("Authorization") String token,
                        @Query("productId") Long productId,
                        @Query("size") String size);

        @POST("/api/v1/carts/reorder/{orderId}")
        Call<Map<String, Object>> reorderFromOrder(
                        @Header("Authorization") String token,
                        @Path("orderId") Long orderId);
}
