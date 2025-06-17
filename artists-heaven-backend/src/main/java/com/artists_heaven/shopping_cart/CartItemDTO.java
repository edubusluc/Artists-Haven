package com.artists_heaven.shopping_cart;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object representing an item in a shopping cart,
 * including the product details, selected size, and quantity.
 */
@Getter
@Setter
@Schema(
    name = "CartItemDTO",
    description = "Represents an item in the shopping cart with product details, size, and quantity."
)
public class CartItemDTO {

    /**
     * Product details for the item.
     */
    @NotNull
    @Schema(description = "Details of the product", required = true)
    private ProductItemDTO product;

    /**
     * Selected size of the product.
     */
    @NotBlank
    @Schema(description = "Selected size of the product", example = "M", required = true)
    private String size;

    /**
     * Quantity of this product and size in the cart (must be at least 1).
     */
    @NotNull
    @Min(1)
    @Schema(description = "Quantity of the product", example = "2", required = true)
    private Integer quantity;
}
