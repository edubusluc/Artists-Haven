package com.artists_heaven.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(name = "OrderItemDTO", description = "Represents an item in an order, including product details, quantity, price, color, and section information.")
public class OrderItemDTO {

    @Schema(description = "Unique identifier of the order item", example = "501")
    private Long id;

    @Schema(description = "Identifier of the associated product", example = "101")
    private Long productId;

    @Schema(description = "Name of the product", example = "T-Shirt")
    private String name;

    @Schema(description = "Size of the product, if applicable", example = "M")
    private String size;

    @Schema(description = "Quantity of the product ordered", example = "2")
    private Integer quantity;

    @Schema(description = "Price per unit of the product", example = "29.99")
    private Float price;

    @Schema(description = "Color of the product, if applicable", example = "Red")
    private String color;

    @Schema(description = "Section or category of the product in the order, if applicable", example = "CLOTHING")
    private String section;

    public OrderItemDTO(OrderItem item) {
        this.id = item.getId();
        this.productId = item.getProductId();
        this.name = item.getName();
        this.size = item.getSize();
        this.quantity = item.getQuantity();
        this.price = item.getPrice();
        this.color = item.getColor();
        this.section = item.getSection() != null ? item.getSection().name() : null;
    }

}
