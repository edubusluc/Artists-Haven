package com.artists_heaven.rewardCard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "RewardCardDTO", description = "Represents a reward card for a user, including required points, discount percentage, and redemption status.")
public class RewardCardDTO {

    @Schema(description = "Unique identifier of the reward card", example = "701")
    private Long id;

    @Schema(description = "Number of points required to redeem the card", example = "100")
    private int requiredPoints;

    @Schema(description = "Discount percentage provided by the reward card", example = "15")
    private int discountPercentage;

    @Schema(description = "Indicates whether the reward card has already been redeemed", example = "false")
    private boolean redeemed;

    public RewardCardDTO(RewardCard card) {
        this.id = card.getId();
        this.requiredPoints = card.getRequiredPoints();
        this.discountPercentage = card.getDiscountPercentage();
        this.redeemed = card.isRedeemed();
    }

}
