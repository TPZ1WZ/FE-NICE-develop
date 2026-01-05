package com.example.nike_fe.data.api;

import com.example.nike_fe.data.model.SpinRequest;
import com.example.nike_fe.data.model.SpinResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

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

    class SpinStatusResponse {
        private int currentPoints;  // Backend field name
        private boolean hasFreeSpinToday;
        private int todaySpins;  // Backend field name
        private int spinCost;  // Backend field name
        private int totalCoinsWon;

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

        public int getMaxFreeSpins() {
            return 999;  // Max daily spins from backend
        }
    }
}
