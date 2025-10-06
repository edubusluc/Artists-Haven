package com.artists_heaven.shopping_cart;

import com.artists_heaven.product.Section;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object representing a product's basic information
 * to be used within a shopping cart item.
 */
@Getter
@Setter
@Schema(
    name = "ProductItemDTO",
    description = "Basic product information used inside shopping cart items."
)
public class ProductItemDTO {

    @Schema(description = "Unique identifier of the product", example = "12345", required = true)
    private Long id;

    @Schema(description = "Name of the product", example = "Abstract Landscape Painting", required = true)
    private String name;

    @Schema(description = "Price of the product", example = "199.99", required = true)
    private Float price;

    @Schema(description = "URL of the product image", example = "https://example.com/images/product1.jpg")
    private String imageUrl;

    private Section section;
}
