package com.artists_heaven.admin;

import com.artists_heaven.order.OrderStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object used to update the status of an existing order.
 */
@Getter
@Setter
@Schema(
    name = "OrderStatusUpdateDTO",
    description = "Represents the payload for updating the status of a specific order."
)
public class OrderStatusUpdateDTO {

    /**
     * Unique identifier of the order to be updated.
     */
    @Schema(description = "Unique ID of the order", example = "1024", required = true)
    private Long orderId;

    /**
     * New status to apply to the order.
     */
    @Schema(description = "New status to set for the order", example = "SHIPPED", required = true)
    private OrderStatus status;
}
