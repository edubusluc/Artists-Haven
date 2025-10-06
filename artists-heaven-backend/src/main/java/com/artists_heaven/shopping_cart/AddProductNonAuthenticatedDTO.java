package com.artists_heaven.shopping_cart;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO used to add a product to a shopping cart for non-authenticated users.
 * Contains the shopping cart info, product ID, and selected size.
 */
@Getter
@Setter
@Schema(
    name = "AddProductNonAuthenticatedDTO",
    description = "Request payload to add a product to a non-authenticated user's shopping cart."
)
public class AddProductNonAuthenticatedDTO {

    /**
     * Shopping cart object for the current session/user.
     */
    @NotNull
    @Schema(description = "Shopping cart instance", required = true)
    private ShoppingCart shoppingCart;

    /**
     * ID of the product to add.
     */
    @NotNull
    @Schema(description = "ID of the product to add to the cart", example = "2001", required = true)
    private Long productId;

    /**
     * Size of the product selected.
     */
    @NotBlank
    @Schema(description = "Selected product size", example = "M", required = true)
    private String size;

    private String color;
}
