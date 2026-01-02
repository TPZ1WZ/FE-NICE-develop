package com.example.nike_fe.data.api;

import com.example.nike_fe.data.model.NotificationResponse;
import com.example.nike_fe.data.model.NotificationStatistics;
import com.example.nike_fe.data.model.UnreadCountResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * NotificationApi - Interface API cho thông báo
 * 
 * Endpoints:
 * - GET /api/notifications - Lấy danh sách thông báo
 * - GET /api/notifications/unread - Lấy thông báo chưa đọc
 * - GET /api/notifications/count-unread - Đếm thông báo chưa đọc
 * - PUT /api/notifications/{id}/read - Đánh dấu đã đọc
 * - PUT /api/notifications/read-all - Đánh dấu tất cả đã đọc
 * - DELETE /api/notifications/{id} - Xóa thông báo
 * - DELETE /api/notifications - Xóa tất cả
 * - GET /api/notifications/statistics - Thống kê
 */
public interface NotificationApi {

        /**
         * Lấy danh sách thông báo (có phân trang)
         * GET /api/notifications?page=0&size=20
         */
        @GET("api/notifications")
        Call<NotificationResponse> getNotifications(
                        @Header("Authorization") String token,
                        @Query("page") int page,
                        @Query("size") int size);

        /**
         * Lấy thông báo chưa đọc
         * GET /api/notifications/unread?page=0&size=20
         */
        @GET("api/notifications/unread")
        Call<NotificationResponse> getUnreadNotifications(
                        @Header("Authorization") String token,
                        @Query("page") int page,
                        @Query("size") int size);

        /**
         * Đếm số thông báo chưa đọc (hiển thị badge)
         * GET /api/notifications/count-unread
         * Response: {"count": 5}
         */
        @GET("api/notifications/count-unread")
        Call<UnreadCountResponse> getUnreadCount(@Header("Authorization") String token);

        /**
         * Đánh dấu thông báo đã đọc
         * PUT /api/notifications/{id}/read
         */
        @PUT("api/notifications/{id}/read")
        Call<Map<String, String>> markAsRead(
                        @Header("Authorization") String token,
                        @Path("id") Long notificationId);

        /**
         * Đánh dấu tất cả thông báo đã đọc
         * PUT /api/notifications/read-all
         */
        @PUT("api/notifications/read-all")
        Call<Map<String, String>> markAllAsRead(@Header("Authorization") String token);

        /**
         * Xóa thông báo
         * DELETE /api/notifications/{id}
         */
        @DELETE("api/notifications/{id}")
        Call<Map<String, String>> deleteNotification(
                        @Header("Authorization") String token,
                        @Path("id") Long notificationId);

        /**
         * Xóa tất cả thông báo
         * DELETE /api/notifications
         */
        @DELETE("api/notifications")
        Call<Map<String, String>> deleteAllNotifications(@Header("Authorization") String token);

        /**
         * Lấy thống kê thông báo theo loại
         * GET /api/notifications/statistics
         * Response: {"ORDER": 10, "FAVORITE": 5, "COUPON": 3}
         */
        @GET("api/notifications/statistics")
        Call<NotificationStatistics> getStatistics(@Header("Authorization") String token);
}
