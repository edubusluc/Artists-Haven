package com.artists_heaven.shopping_cart;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object used to add a product to the shopping cart,
 * specifying the product ID and chosen size.
 */
@Getter
@Setter
@Schema(
    name = "AddProductDTO",
    description = "Request payload for adding a product to the shopping cart with selected size."
)
public class AddProductDTO {

    /**
     * ID of the product to be added to the cart.
     */
    @NotNull
    @Schema(description = "ID of the product to add to the cart", example = "2001", required = true)
    private Long productId;

    /**
     * Size selected for the product (e.g., S, M, L).
     */
    @NotBlank
    @Schema(description = "Size of the product selected", example = "M", required = true)
    private String size;
}
