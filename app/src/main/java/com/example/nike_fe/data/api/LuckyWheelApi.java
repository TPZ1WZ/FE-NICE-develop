package com.example.nike_fe.data.api;

import com.example.nike_fe.data.model.SpinRequest;
import com.example.nike_fe.data.model.SpinResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

import java.util.Map;

public interface LuckyWheelApi {

    @POST("/api/v1/lucky-wheel/spin")
    Call<SpinResponse> spin(
            @Header("Authorization") String authorization,
            @Body SpinRequest request
    );

    @GET("/api/v1/lucky-wheel/info")
    Call<SpinStatusResponse> getInfo(
            @Header("Authorization") String authorization
    );

    @POST("/api/v1/lucky-wheel/track-product/{productId}")
    Call<Map<String, Object>> trackProductView(
            @Header("Authorization") String authorization,
            @Path("productId") Long productId
    );

    class SpinStatusResponse {
        private int currentPoints;  // Backend field name
        private boolean hasFreeSpinToday;
        private int todaySpins;  // Backend field name
        private int spinCost;  // Backend field name
        private int totalCoinsWon;
        private long productsViewedToday;  // NEW: số sản phẩm đã xem hôm nay
        private int requiredProductViews;  // NEW: số sản phẩm cần xem (3)
        private boolean wheelEnabled;  // NEW: vòng quay có được bật không

        public int getCurrentCoins() {
            return currentPoints;  // For backward compatibility
        }

        public void setCurrentCoins(int currentCoins) {
            this.currentPoints = currentCoins;
        }

        public int getCurrentPoints() {
            return currentPoints;
        }

        public void setCurrentPoints(int currentPoints) {
            this.currentPoints = currentPoints;
        }

        public boolean isHasFreeSpinToday() {
            return hasFreeSpinToday;
        }

        public void setHasFreeSpinToday(boolean hasFreeSpinToday) {
            this.hasFreeSpinToday = hasFreeSpinToday;
        }

        public int getTodaySpinCount() {
            return todaySpins;  // For backward compatibility
        }

        public void setTodaySpinCount(int todaySpinCount) {
            this.todaySpins = todaySpinCount;
        }

        public int getTodaySpins() {
            return todaySpins;
        }

        public void setTodaySpins(int todaySpins) {
            this.todaySpins = todaySpins;
        }

        public int getSpinCost() {
            return spinCost;
        }

        public void setSpinCost(int spinCost) {
            this.spinCost = spinCost;
        }

        public int getTotalCoinsWon() {
            return totalCoinsWon;
        }

        public void setTotalCoinsWon(int totalCoinsWon) {
            this.totalCoinsWon = totalCoinsWon;
        }

        public long getProductsViewedToday() {
            return productsViewedToday;
        }

        public void setProductsViewedToday(long productsViewedToday) {
            this.productsViewedToday = productsViewedToday;
        }

        public int getRequiredProductViews() {
            return requiredProductViews;
        }

        public void setRequiredProductViews(int requiredProductViews) {
            this.requiredProductViews = requiredProductViews;
        }

        public boolean isWheelEnabled() {
            return wheelEnabled;
        }

        public void setWheelEnabled(boolean wheelEnabled) {
            this.wheelEnabled = wheelEnabled;
        }

        public int getMaxFreeSpins() {
            return 1;  // Max daily spins = 1 (updated logic)
        }
    }
}
