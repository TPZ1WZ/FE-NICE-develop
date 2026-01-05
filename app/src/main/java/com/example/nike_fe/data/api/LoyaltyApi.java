package com.example.nike_fe.data.api;

import com.example.nike_fe.data.model.CheckinResponse;
import com.example.nike_fe.data.model.CheckinStreakResponse;
import com.example.nike_fe.data.model.LoyaltyPointsResponse;
import com.example.nike_fe.data.model.TransactionHistoryItem;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * LoyaltyApi - API endpoints cho Nike Coin / Loyalty System
 */
public interface LoyaltyApi {

    /**
     * Lấy thông tin loyalty points của user
     */
    @GET("/api/v1/loyalty/points")
    Call<LoyaltyPointsResponse> getLoyaltyPoints(
            @Header("Authorization") String token
    );

    /**
     * Lấy thông tin checkin streak
     */
    @GET("/api/v1/loyalty/checkin/streak")
    Call<CheckinStreakResponse> getCheckinStreak(
            @Header("Authorization") String token
    );

    /**
     * Thực hiện checkin hàng ngày
     */
    @POST("/api/v1/loyalty/checkin")
    Call<CheckinResponse> performCheckin(
            @Header("Authorization") String token
    );

    /**
     * Lấy lịch sử giao dịch coin
     */
    @GET("/api/v1/loyalty/transactions")
    Call<List<TransactionHistoryItem>> getTransactionHistory(
            @Header("Authorization") String token
    );
}
