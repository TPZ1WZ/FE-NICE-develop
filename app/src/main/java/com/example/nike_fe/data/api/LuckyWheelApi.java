package com.example.nike_fe.data.api;

import com.example.nike_fe.data.model.BatchProbabilityRequest;
import com.example.nike_fe.data.model.LuckyWheelStatistics;
import com.example.nike_fe.data.model.Prize;
import com.example.nike_fe.data.model.SpinHistoryItem;
import com.example.nike_fe.data.model.SpinResponse;
import com.example.nike_fe.data.model.UserSpinManagement;
import com.example.nike_fe.data.model.WheelConfig;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * LuckyWheelApi - API endpoints cho vòng quay may mắn
 * Base URL: http://10.0.2.2:8080/api/v1/lucky-wheel
 */
public interface LuckyWheelApi {

    /**
     * Lấy danh sách phần thưởng
     * GET /api/v1/lucky-wheel/prizes
     */
    @GET("api/v1/lucky-wheel/prizes")
    Call<List<Prize>> getPrizes();

    /**
     * Quay thưởng
     * POST /api/v1/lucky-wheel/spin
     */
    @POST("api/v1/lucky-wheel/spin")
    Call<SpinResponse> spin(@Header("Authorization") String token);

    /**
     * Lấy số lượt quay còn lại
     * GET /api/v1/lucky-wheel/remaining-spins
     */
    @GET("api/v1/lucky-wheel/remaining-spins")
    Call<Map<String, Integer>> getRemainingSpins(@Header("Authorization") String token);

    /**
     * Lấy lịch sử quay
     * GET /api/v1/lucky-wheel/history?page=0&size=10
     */
    @GET("api/v1/lucky-wheel/history")
    Call<Map<String, Object>> getHistory(
            @Header("Authorization") String token,
            @Query("page") int page,
            @Query("size") int size);

    /**
     * Nhận thưởng
     * POST /api/v1/lucky-wheel/claim/{historyId}
     */
    @POST("api/v1/lucky-wheel/claim/{historyId}")
    Call<Map<String, Object>> claimPrize(@Path("historyId") Long historyId);

    // ============== ADMIN APIs ==============

    // 1. Quản lý phần thưởng
    @GET("api/v1/admin/lucky-wheel/prizes")
    Call<List<Prize>> getAllPrizes(@Header("Authorization") String token);

    @POST("api/v1/admin/lucky-wheel/prizes")
    Call<Prize> createPrize(@Header("Authorization") String token, @Body Prize prize);

    @PUT("api/v1/admin/lucky-wheel/prizes/{id}")
    Call<Prize> updatePrize(@Header("Authorization") String token, @Path("id") Long id, @Body Prize prize);

    @DELETE("api/v1/admin/lucky-wheel/prizes/{id}")
    Call<Map<String, Object>> deletePrize(@Header("Authorization") String token, @Path("id") Long id);

    // 2. Cấu hình xác suất
    @POST("api/v1/admin/lucky-wheel/prizes/batch-probability")
    Call<List<Prize>> batchUpdateProbabilities(@Header("Authorization") String token,
            @Body BatchProbabilityRequest request);

    // 3. Quản lý sự kiện & cấu hình
    @GET("api/v1/lucky-wheel/config")
    Call<WheelConfig> getPublicConfig();

    @GET("api/v1/admin/lucky-wheel/config")
    Call<WheelConfig> getConfig(@Header("Authorization") String token);

    @PUT("api/v1/admin/lucky-wheel/config")
    Call<WheelConfig> updateConfig(@Header("Authorization") String token, @Body WheelConfig config);

    @POST("api/v1/admin/lucky-wheel/toggle")
    Call<WheelConfig> toggleWheelStatus(@Header("Authorization") String token, @Query("active") Boolean active);

    @POST("api/v1/admin/lucky-wheel/event-schedule")
    Call<WheelConfig> updateEventSchedule(
            @Header("Authorization") String token,
            @Query("eventName") String eventName,
            @Query("startDate") String startDate,
            @Query("endDate") String endDate,
            @Query("isTimeRestricted") Boolean isTimeRestricted);

    // 4. Quản lý lượt quay user
    @GET("api/v1/admin/lucky-wheel/user-spins")
    Call<List<UserSpinManagement>> getUserSpinManagement(@Header("Authorization") String token);

    @POST("api/v1/admin/lucky-wheel/user-spins/{userId}/reset")
    Call<Map<String, Object>> resetUserSpins(@Header("Authorization") String token, @Path("userId") Long userId);

    @POST("api/v1/admin/lucky-wheel/user-spins/{userId}/grant")
    Call<Map<String, Object>> grantBonusSpins(
            @Header("Authorization") String token,
            @Path("userId") Long userId,
            @Query("bonusSpins") Integer bonusSpins);

    // 5. Thống kê
    @GET("api/v1/admin/lucky-wheel/statistics")
    Call<LuckyWheelStatistics> getStatistics(@Header("Authorization") String token);
}
