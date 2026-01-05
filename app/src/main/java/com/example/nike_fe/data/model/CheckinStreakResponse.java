package com.example.nike_fe.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CheckinStreakResponse {
    
    @SerializedName("currentStreak")
    private Integer currentStreak;
    
    @SerializedName("hasCheckedInToday")
    private Boolean hasCheckedInToday;
    
    @SerializedName("todayReward")
    private Integer todayReward;
    
    @SerializedName("lastCheckinDate")
    private String lastCheckinDate;
    
    @SerializedName("totalCheckins")
    private Long totalCheckins;
    
    @SerializedName("weeklyRewards")
    private List<DayRewardInfo> weeklyRewards;

    // Getters and Setters
    public Integer getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(Integer currentStreak) {
        this.currentStreak = currentStreak;
    }

    public Boolean getHasCheckedInToday() {
        return hasCheckedInToday;
    }

    public void setHasCheckedInToday(Boolean hasCheckedInToday) {
        this.hasCheckedInToday = hasCheckedInToday;
    }

    public Integer getTodayReward() {
        return todayReward;
    }

    public void setTodayReward(Integer todayReward) {
        this.todayReward = todayReward;
    }

    public String getLastCheckinDate() {
        return lastCheckinDate;
    }

    public void setLastCheckinDate(String lastCheckinDate) {
        this.lastCheckinDate = lastCheckinDate;
    }

    public Long getTotalCheckins() {
        return totalCheckins;
    }

    public void setTotalCheckins(Long totalCheckins) {
        this.totalCheckins = totalCheckins;
    }

    public List<DayRewardInfo> getWeeklyRewards() {
        return weeklyRewards;
    }

    public void setWeeklyRewards(List<DayRewardInfo> weeklyRewards) {
        this.weeklyRewards = weeklyRewards;
    }

    public static class DayRewardInfo {
        @SerializedName("dayNumber")
        private Integer dayNumber;
        
        @SerializedName("rewardAmount")
        private Integer rewardAmount;
        
        @SerializedName("isBonus")
        private Boolean isBonus;
        
        @SerializedName("status")
        private String status; // PAST, TODAY, FUTURE

        // Getters and Setters
        public Integer getDayNumber() {
            return dayNumber;
        }

        public void setDayNumber(Integer dayNumber) {
            this.dayNumber = dayNumber;
        }

        public Integer getRewardAmount() {
            return rewardAmount;
        }

        public void setRewardAmount(Integer rewardAmount) {
            this.rewardAmount = rewardAmount;
        }

        public Boolean getIsBonus() {
            return isBonus;
        }

        public void setIsBonus(Boolean isBonus) {
            this.isBonus = isBonus;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
