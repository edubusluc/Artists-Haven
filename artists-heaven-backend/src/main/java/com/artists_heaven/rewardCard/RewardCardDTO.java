package com.artists_heaven.rewardCard;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RewardCardDTO {
    private Long id;
    private int requiredPoints;
    private int discountPercentage;
    private boolean redeemed;

    public RewardCardDTO(RewardCard card) {
        this.id = card.getId();
        this.requiredPoints = card.getRequiredPoints();
        this.discountPercentage = card.getDiscountPercentage();
        this.redeemed = card.isRedeemed();
    }
}