package com.artists_heaven.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object representing product management statistics,
 * such as availability and promotion status.
 */
@Getter
@Setter
@Schema(
    name = "ProductManagementDTO",
    description = "Represents product-related statistics, including availability and promotional status."
)
public class ProductManagementDTO {

    /**
     * Number of products that are currently not available (e.g., out of stock or unpublished).
     */
    @Schema(description = "Number of products that are currently not available", example = "45")
    private Integer notAvailableProducts;

    /**
     * Number of products that are currently available for purchase.
     */
    @Schema(description = "Number of products currently available for purchase", example = "350")
    private Integer availableProducts;

    /**
     * Number of products that are currently promoted or featured.
     */
    @Schema(description = "Number of products currently promoted or featured", example = "25")
    private Integer promotedProducts;

    /**
     * Total number of products in the system.
     */
    @Schema(description = "Total number of products", example = "420")
    private Integer totalProducts;
}
