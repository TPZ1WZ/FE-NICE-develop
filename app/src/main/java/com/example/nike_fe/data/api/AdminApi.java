package com.example.nike_fe.data.api;

import com.example.nike_fe.data.model.Category;
import com.example.nike_fe.data.model.DashboardStats;
import com.example.nike_fe.data.model.Order;
import com.example.nike_fe.data.model.OrderStatusDistribution;
import com.example.nike_fe.data.model.Product;
import com.example.nike_fe.data.model.RevenueChartData;
import com.example.nike_fe.data.model.TopProductsResponse;
import com.example.nike_fe.data.model.User;
import com.example.nike_fe.data.model.UserListResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AdminApi {

        @GET("api/v1/admin/dashboard/statistics")
        Call<DashboardStats> getDashboardStats(@Header("Authorization") String token);

        @GET("api/v1/admin/dashboard/order-status")
        Call<OrderStatusDistribution> getOrderStatusDistribution(@Header("Authorization") String token);

        @GET("api/v1/admin/dashboard/top-products")
        Call<TopProductsResponse> getTopProducts(
                        @Header("Authorization") String token,
                        @Query("limit") int limit);

        @GET("api/v1/admin/dashboard/charts/revenue")
        Call<RevenueChartData> getRevenueChart(
                        @Header("Authorization") String token,
                        @Query("days") int days);

        @GET("api/v1/admin/products")
        Call<List<Product>> getAllProducts(
                        @Header("Authorization") String token,
                        @Query("page") int page,
                        @Query("size") int size);

        @GET("api/v1/admin/orders")
        Call<List<Order>> getAllOrders(
                        @Header("Authorization") String token,
                        @Query("status") String status);

        @GET("api/v1/admin/orders/{id}")
        Call<Order> getOrderById(
                        @Header("Authorization") String token,
                        @Path("id") Long orderId);

        @PATCH("api/v1/admin/orders/{id}/status")
        Call<Order> updateOrderStatus(
                        @Header("Authorization") String token,
                        @Path("id") Long orderId,
                        @Query("status") String status);

        @GET("api/v1/admin/users")
        Call<List<User>> getAllUsers(@Header("Authorization") String token);

        // Category APIs
        @GET("api/admin/categories")
        Call<List<Category>> getAllCategories(@Header("Authorization") String token);

        @POST("api/admin/categories")
        Call<Category> createCategory(
                        @Header("Authorization") String token,
                        @Body Category category);

        @PUT("api/admin/categories/{id}")
        Call<Category> updateCategory(
                        @Header("Authorization") String token,
                        @Path("id") Long id,
                        @Body Category category);

        @DELETE("api/admin/categories/{id}")
        Call<Void> deleteCategory(
                        @Header("Authorization") String token,
                        @Path("id") Long id);

        // User Management APIs
        @GET("api/admin/users")
        Call<UserListResponse> getUsers(
                        @Header("Authorization") String token,
                        @Query("search") String search);

        @GET("api/admin/users/{id}")
        Call<User> getUserById(
                        @Header("Authorization") String token,
                        @Path("id") Long id);

        @PUT("/api/admin/users/{id}")
        Call<User> updateUser(
                        @Header("Authorization") String token,
                        @Path("id") Long id,
                        @Body User user);

        // Review Management APIs
        @GET("api/v1/admin/reviews")
        Call<java.util.Map<String, Object>> getAllReviews(
                        @Header("Authorization") String token,
                        @Query("page") int page,
                        @Query("size") int size,
                        @Query("status") String status,
                        @Query("rating") Integer rating,
                        @Query("productId") Long productId);

        @GET("api/v1/admin/reviews/statistics")
        Call<java.util.Map<String, Object>> getReviewStatistics(
                        @Header("Authorization") String token);

        @GET("api/v1/admin/reviews/pending")
        Call<java.util.List<com.example.nike_fe.data.model.Review>> getPendingReviews(
                        @Header("Authorization") String token);

        @PATCH("api/v1/admin/reviews/{reviewId}/approve")
        Call<java.util.Map<String, Object>> approveReview(
                        @Header("Authorization") String token,
                        @Path("reviewId") Long reviewId);

        @PUT("api/v1/admin/reviews/{reviewId}/reject")
        Call<java.util.Map<String, Object>> rejectReview(
                        @Header("Authorization") String token,
                        @Path("reviewId") Long reviewId,
                        @Body java.util.Map<String, String> body);

        @DELETE("api/v1/admin/reviews/{reviewId}")
        Call<java.util.Map<String, Object>> deleteReview(
                        @Header("Authorization") String token,
                        @Path("reviewId") Long reviewId);

        @POST("api/v1/admin/reviews/{reviewId}/admin-reply")
        Call<java.util.Map<String, Object>> replyToReview(
                        @Header("Authorization") String token,
                        @Path("reviewId") Long reviewId,
                        @Body java.util.Map<String, String> body);

        @POST("api/v1/admin/reviews/bulk-approve")
        Call<java.util.Map<String, Object>> bulkApproveReviews(
                        @Header("Authorization") String token,
                        @Body java.util.Map<String, java.util.List<Long>> body);

        @DELETE("api/v1/admin/reviews/bulk-delete")
        Call<java.util.Map<String, Object>> bulkDeleteReviews(
                        @Header("Authorization") String token,
                        @Body java.util.Map<String, java.util.List<Long>> body);

        // Settings APIs
        @POST("/auth/change-password")
        Call<String> changePassword(
                        @Header("Authorization") String token,
                        @Body com.example.nike_fe.data.model.ChangePasswordRequest request);

        @GET("/api/admin/settings")
        Call<com.example.nike_fe.data.model.StoreSettings> getStoreSettings(
                        @Header("Authorization") String token);

        @PUT("/api/admin/settings")
        Call<com.example.nike_fe.data.model.StoreSettings> updateStoreSettings(
                        @Header("Authorization") String token,
                        @Body com.example.nike_fe.data.model.StoreSettings settings);
}
