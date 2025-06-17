package com.artists_heaven.rating;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object used to submit a product rating,
 * including the score and optional comment.
 */
@Getter
@Setter
@Schema(
    name = "RatingRequestDTO",
    description = "Request payload for submitting a product rating, including score and comment."
)
public class RatingRequestDTO {

    /**
     * ID of the product being rated.
     */
    @NotNull
    @Schema(description = "ID of the product to rate", example = "101", required = true)
    private Long productId;

    /**
     * Score given to the product (e.g., from 1 to 5).
     */
    @NotNull
    @Min(1)
    @Max(5)
    @Schema(description = "Rating score (1 to 5)", example = "4", required = true)
    private Integer score;

    /**
     * Optional comment provided by the user.
     */
    @Schema(description = "Optional comment about the product", example = "Amazing quality and fast delivery!")
    private String comment;
}
