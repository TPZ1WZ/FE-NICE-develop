package com.example.nike_fe.data.api;

import com.example.nike_fe.data.model.DashboardStats;
import com.example.nike_fe.data.model.Order;
import com.example.nike_fe.data.model.OrderStatusDistribution;
import com.example.nike_fe.data.model.Product;
import com.example.nike_fe.data.model.RevenueChartData;
import com.example.nike_fe.data.model.TopProductsResponse;
import com.example.nike_fe.data.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AdminApi {
    
    @GET("api/admin/dashboard/statistics")
    Call<DashboardStats> getDashboardStats(@Header("Authorization") String token);
    
    @GET("api/admin/dashboard/order-status")
    Call<OrderStatusDistribution> getOrderStatusDistribution(@Header("Authorization") String token);
    
    @GET("api/admin/dashboard/top-products")
    Call<TopProductsResponse> getTopProducts(
        @Header("Authorization") String token,
        @Query("limit") int limit
    );
    
    @GET("api/admin/dashboard/charts/revenue")
    Call<RevenueChartData> getRevenueChart(
        @Header("Authorization") String token,
        @Query("days") int days
    );
    
    @GET("api/v1/admin/products")
    Call<List<Product>> getAllProducts(
        @Header("Authorization") String token,
        @Query("page") int page,
        @Query("size") int size
    );
    
    @GET("api/v1/admin/orders")
    Call<List<Order>> getAllOrders(
        @Header("Authorization") String token,
        @Query("status") String status
    );
    
    @PATCH("api/v1/admin/orders/{id}/status")
    Call<Order> updateOrderStatus(
        @Header("Authorization") String token,
        @Path("id") Long orderId,
        @Query("status") String status
    );
    
    @GET("api/v1/admin/users")
    Call<List<User>> getAllUsers(@Header("Authorization") String token);
}
