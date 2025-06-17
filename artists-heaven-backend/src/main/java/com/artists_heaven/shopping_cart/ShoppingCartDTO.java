package com.artists_heaven.shopping_cart;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object representing a shopping cart,
 * including its ID and the list of items it contains.
 */
@Getter
@Setter
@Schema(
    name = "ShoppingCartDTO",
    description = "Represents a shopping cart with its unique ID and list of cart items."
)
public class ShoppingCartDTO {

    @Schema(description = "Unique identifier of the shopping cart", example = "7890")
    private Long id;

    @Schema(description = "List of items contained in the shopping cart", required = true)
    private List<CartItemDTO> items;
}
