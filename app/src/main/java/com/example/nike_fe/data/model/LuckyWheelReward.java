package com.example.nike_fe.data.model;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

public class LuckyWheelReward {
    
    @SerializedName("id")
    private Long id;
    
    @SerializedName("position")
    private Integer position;
    
    @SerializedName("rewardType")
    private String rewardType;
    
    @SerializedName("coinAmount")
    private Integer coinAmount;
    
    @SerializedName("weight")
    private Integer weight;
    
    @SerializedName("probability")
    private BigDecimal probability;
    
    @SerializedName("iconName")
    private String iconName;
    
    @SerializedName("label")
    private String label;
    
    @SerializedName("isActive")
    private Boolean isActive;

    // Constructors
    public LuckyWheelReward() {}

    public LuckyWheelReward(Integer position, String rewardType, Integer coinAmount, 
                           Integer weight, String iconName, String label, Boolean isActive) {
        this.position = position;
        this.rewardType = rewardType;
        this.coinAmount = coinAmount;
        this.weight = weight;
        this.iconName = iconName;
        this.label = label;
        this.isActive = isActive;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }

    public String getRewardType() { return rewardType; }
    public void setRewardType(String rewardType) { this.rewardType = rewardType; }

    public Integer getCoinAmount() { return coinAmount; }
    public void setCoinAmount(Integer coinAmount) { this.coinAmount = coinAmount; }

    public Integer getWeight() { return weight; }
    public void setWeight(Integer weight) { this.weight = weight; }

    public BigDecimal getProbability() { return probability; }
    public void setProbability(BigDecimal probability) { this.probability = probability; }

    public String getIconName() { return iconName; }
    public void setIconName(String iconName) { this.iconName = iconName; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
