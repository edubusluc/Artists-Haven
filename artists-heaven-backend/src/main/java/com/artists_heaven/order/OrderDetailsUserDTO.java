package com.artists_heaven.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object representing the details of an order visible to a user.
 * <p>
 * This DTO extends {@link OrderDetailsDTO} but excludes sensitive information,
 * such as payment intent and email, to prevent exposing confidential data in API responses.
 * </p>
 * <p>
 * Fields inherited from {@link OrderDetailsDTO} include:
 * <ul>
 *     <li>Order ID</li>
 *     <li>List of purchased products</li>
 *     <li>Total price</li>
 *     <li>Order status</li>
 *     <li>Creation date</li>
 * </ul>
 * The following fields are explicitly hidden for security:
 * <ul>
 *     <li>{@code paymentIntent}: Set to {@code null} to avoid exposing payment information.</li>
 *     <li>{@code email}: Set to {@code null} to avoid exposing the user's email.</li>
 * </ul>
 * </p>
 */
@Getter
@Setter
@Schema(description = "Order details visible to the user, excluding sensitive payment information.")
public class OrderDetailsUserDTO extends OrderDetailsDTO {

    /**
     * Constructs an {@code OrderDetailsUserDTO} from an {@link Order} entity.
     * <p>
     * Sensitive fields such as {@code paymentIntent} and {@code email} are cleared to prevent
     * exposing confidential information in user-facing responses.
     * </p>
     *
     * @param order the order entity from which to create the DTO
     */
    public OrderDetailsUserDTO(Order order) {
        super(order);
        this.setPaymentIntent(null);
        this.setEmail(null);
    }
}
