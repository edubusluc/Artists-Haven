package com.artists_heaven.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object representing the sales summary for a specific month.
 * Contains the month number, total number of orders, and total revenue generated.
 */
@Getter
@Setter
@Schema(
    name = "MonthlySalesDTO",
    description = "Represents monthly sales data including total orders and total revenue."
)
public class MonthlySalesDTO {

    /**
     * The month number (1 for January, 2 for February, etc.).
     */
    @Schema(description = "Month number (1 = January, 2 = February, etc.)", example = "1", required = true)
    private Integer month;

    /**
     * Total number of orders placed in the given month.
     */
    @Schema(description = "Total number of orders placed in the month", example = "1500", required = true)
    private Long totalOrders;

    /**
     * Total revenue generated from all orders in the given month.
     */
    @Schema(description = "Total revenue generated in the month", example = "12345.67", required = true)
    private Double totalRevenue;

    /**
     * Constructor to create a MonthlySalesDTO instance.
     *
     * @param month        The month number (1-12).
     * @param totalOrders  Total number of orders in the month.
     * @param totalRevenue Total revenue generated in the month.
     */
    public MonthlySalesDTO(Integer month, Long totalOrders, Double totalRevenue) {
        this.month = month;
        this.totalOrders = totalOrders;
        this.totalRevenue = totalRevenue;
    }

    public MonthlySalesDTO() {
    }

}
