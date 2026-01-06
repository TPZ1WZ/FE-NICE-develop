package com.example.nike_fe.data.api;

import com.example.nike_fe.data.model.LuckyWheelReward;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.*;

public interface LuckyWheelAdminApi {

    @GET("/api/v1/admin/lucky-wheel/status")
    Call<StatusResponse> getStatus(@Header("Authorization") String authorization);

    @POST("/api/v1/admin/lucky-wheel/toggle")
    Call<ToggleResponse> toggleWheel(
            @Header("Authorization") String authorization,
            @Query("enabled") boolean enabled
    );

    @GET("/api/v1/admin/lucky-wheel/rewards")
    Call<List<LuckyWheelReward>> getRewards(@Header("Authorization") String authorization);

    @POST("/api/v1/admin/lucky-wheel/rewards")
    Call<LuckyWheelReward> createReward(
            @Header("Authorization") String authorization,
            @Body LuckyWheelReward reward
    );

    @PUT("/api/v1/admin/lucky-wheel/rewards/{id}")
    Call<LuckyWheelReward> updateReward(
            @Header("Authorization") String authorization,
            @Path("id") Long id,
            @Body LuckyWheelReward reward
    );

    @DELETE("/api/v1/admin/lucky-wheel/rewards/{id}")
    Call<Map<String, Object>> deleteReward(
            @Header("Authorization") String authorization,
            @Path("id") Long id
    );

    class StatusResponse {
        private boolean enabled;
        private long totalRewards;
        private long activeRewards;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public long getTotalRewards() { return totalRewards; }
        public void setTotalRewards(long totalRewards) { this.totalRewards = totalRewards; }

        public long getActiveRewards() { return activeRewards; }
        public void setActiveRewards(long activeRewards) { this.activeRewards = activeRewards; }
    }

    class ToggleResponse {
        private boolean success;
        private boolean enabled;
        private String message;

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
