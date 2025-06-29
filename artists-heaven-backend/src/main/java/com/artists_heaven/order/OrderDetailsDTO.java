package com.artists_heaven.order;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object representing the full details of an order,
 * including customer information, shipping details, order status, and items.
 */
@Getter
@Setter
@Schema(name = "OrderDetailsDTO", description = "Detailed representation of an order, including items, user, address, and payment info.")
public class OrderDetailsDTO {

    @Schema(description = "Unique internal ID of the order", example = "123")
    private Long id;

    @Schema(description = "Public identifier of the order (e.g., shown to user)", example = "202406150001")
    private String identifier;

    @Schema(description = "Total price of the order", example = "149.99")
    private Float totalPrice;

    @Schema(description = "Current status of the order", example = "SHIPPED")
    private OrderStatus status;

    @Schema(description = "First line of the delivery address", example = "123 Main St")
    private String addressLine1;

    @Schema(description = "Second line of the delivery address (optional)", example = "Apt 4B")
    private String addressLine2;

    @Schema(description = "Postal or ZIP code", example = "90210")
    private String postalCode;

    @Schema(description = "City for the delivery address", example = "Los Angeles")
    private String city;

    @Schema(description = "Country for the delivery address", example = "United States")
    private String country;

    @Schema(description = "ID of the user who placed the order", example = "42")
    private Long userId;

    @Schema(description = "List of items included in the order")
    private List<OrderItem> items;

    @Schema(description = "Date when the order was created", example = "2025-06-15", type = "string", format = "date")
    private LocalDateTime createdDate;

    @Schema(description = "Email associated with the order", example = "customer@example.com")
    private String email;

    @Schema(description = "Phone number provided for the order", example = "+1-555-123-4567")
    private String phone;

    @Schema(description = "Stripe or payment gateway intent/ID for this order", example = "pi_3NMFAI2eZvKYlo2CgE")
    private String paymentIntent;

    /**
     * Constructs an OrderDetailsDTO from an Order entity.
     *
     * @param order The Order entity containing all the necessary data.
     */
    public OrderDetailsDTO(Order order) {
        this.id = order.getId();
        this.identifier = order.getIdentifier().toString();
        this.totalPrice = order.getTotalPrice();
        this.status = order.getStatus();
        this.addressLine1 = order.getAddressLine1();
        this.addressLine2 = order.getAddressLine2();
        this.postalCode = order.getPostalCode();
        this.city = order.getCity();
        this.country = order.getCountry();
        this.userId = order.getUser() != null ? order.getUser().getId() : null;
        this.items = order.getItems();
        this.createdDate = order.getCreatedDate();
        this.email = order.getEmail();
        this.phone = order.getPhone();
        this.paymentIntent = order.getPaymentIntent();
    }
}
