package com.example.nike_fe.data.api;

import com.example.nike_fe.data.model.FavoriteProduct;
import com.example.nike_fe.data.model.FavoriteResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.*;

public interface FavoriteApi {
    
    /**
     * Thêm sản phẩm vào danh sách yêu thích
     * POST /api/v1/favorites/{productId}
     */
    @POST("/api/v1/favorites/{productId}")
    Call<Map<String, Object>> addToFavorites(
            @Path("productId") Long productId,
            @Header("Authorization") String token
    );
    
    /**
     * Xóa sản phẩm khỏi danh sách yêu thích
     * DELETE /api/v1/favorites/{productId}
     */
    @DELETE("/api/v1/favorites/{productId}")
    Call<Map<String, Object>> removeFromFavorites(
            @Path("productId") Long productId,
            @Header("Authorization") String token
    );
    
    /**
     * Lấy danh sách sản phẩm yêu thích
     * GET /api/v1/favorites?page=0&size=20
     */
    @GET("/api/v1/favorites")
    Call<FavoriteResponse> getUserFavorites(
            @Query("page") int page,
            @Query("size") int size,
            @Header("Authorization") String token
    );
    
    /**
     * Kiểm tra sản phẩm đã được yêu thích chưa
     * GET /api/v1/favorites/check/{productId}
     */
    @GET("/api/v1/favorites/check/{productId}")
    Call<Map<String, Object>> checkFavorite(
            @Path("productId") Long productId,
            @Header("Authorization") String token
    );
    
    /**
     * Đếm số lượng sản phẩm yêu thích
     * GET /api/v1/favorites/count
     */
    @GET("/api/v1/favorites/count")
    Call<Map<String, Object>> countFavorites(
            @Header("Authorization") String token
    );
    
    /**
     * Lấy danh sách product IDs đã yêu thích
     * GET /api/v1/favorites/product-ids
     */
    @GET("/api/v1/favorites/product-ids")
    Call<Map<String, Object>> getFavoriteProductIds(
            @Header("Authorization") String token
    );
}
