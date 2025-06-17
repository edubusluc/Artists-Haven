package com.artists_heaven.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object used to promote a product by applying a discount percentage.
 */
@Getter
@Setter
@Schema(
    name = "PromoteDTO",
    description = "DTO used to apply a discount promotion to a product."
)
public class PromoteDTO {

    /**
     * ID of the product to promote.
     */
    @Schema(description = "ID of the product to be promoted", example = "102", required = true)
    private Long id;

    /**
     * Discount percentage to apply (e.g., 10 for 10% off).
     */
    @Schema(description = "Discount percentage to apply (0–100)", example = "20", required = true)
    private Integer discount;
}
