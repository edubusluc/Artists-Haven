package com.artists_heaven.admin;

import java.util.Map;

import com.artists_heaven.email.EmailType;
import com.artists_heaven.order.OrderStatus;
import com.artists_heaven.verification.VerificationStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object representing global order and user statistics for the admin dashboard.
 */
@Getter
@Setter
@Schema(
    name = "OrderStatisticsDTO",
    description = "Aggregated statistics related to orders, users, emails, and product categories."
)
public class OrderStatisticsDTO {

    /**
     * Total number of orders placed.
     */
    @Schema(description = "Total number of orders placed", example = "2532")
    private Integer numOrders;

    /**
     * Total income generated in the current year.
     */
    @Schema(description = "Total income generated this year", example = "98567.45")
    private Double incomePerYear;

    /**
     * Number of emails sent, grouped by email type (e.g., ORDER_CONFIRMATION, PASSWORD_RESET).
     */
    @Schema(description = "Count of emails sent by type",
            example = "{ \"ORDER_CONFIRMATION\": 1200, \"PASSWORD_RESET\": 340 }")
    private Map<EmailType, Integer> emailCounts;

    /**
     * Total number of registered users.
     */
    @Schema(description = "Total number of registered users", example = "1789")
    private Integer numUsers;

    /**
     * Total number of registered artists.
     */
    @Schema(description = "Total number of registered artists", example = "342")
    private Integer numArtists;

    /**
     * Number of orders grouped by their status (e.g., PENDING, SHIPPED, DELIVERED).
     */
    @Schema(description = "Count of orders by status",
            example = "{ \"PENDING\": 530, \"DELIVERED\": 1845 }")
    private Map<OrderStatus, Integer> orderStatusCounts;

    /**
     * Number of users grouped by their verification status (e.g., VERIFIED, UNVERIFIED).
     */
    @Schema(description = "Count of users by verification status",
            example = "{ \"VERIFIED\": 1500, \"UNVERIFIED\": 289 }")
    private Map<VerificationStatus, Integer> verificationStatusCounts;

    /**
     * Number of items sold, grouped by item name.
     */
    @Schema(description = "Number of items sold per item name",
            example = "{ \"Print A\": 45, \"Canvas B\": 80 }")
    private Map<String, Integer> orderItemCount;

    /**
     * Number of items sold, grouped by category.
     */
    @Schema(description = "Number of items sold per category",
            example = "{ \"Posters\": 300, \"Sculptures\": 120 }")
    private Map<String, Integer> categoryItemCount;

    /**
     * Number of orders placed, grouped by the country of the customer.
     */
    @Schema(description = "Top countries by number of sales",
            example = "{ \"United States\": 1345, \"Germany\": 678 }")
    private Map<String, Integer> mostCountrySold;
}
