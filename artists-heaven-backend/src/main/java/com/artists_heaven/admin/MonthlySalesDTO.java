package com.artists_heaven.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "MonthlySalesDTO", description = "Represents monthly sales data including total orders and total revenue.")
@AllArgsConstructor
@NoArgsConstructor
public class MonthlySalesDTO {

    /**
     * The month number (1 for January, 2 for February, etc.).
     */
    @Schema(description = "Month number (1 = January, 2 = February, etc.)", example = "1")
    private Integer month;

    /**
     * Total number of orders placed in the given month.
     */
    @Schema(description = "Total number of orders placed in the month", example = "1500")
    private Long totalOrders;

    /**
     * Total revenue generated from all orders in the given month.
     */
    @Schema(description = "Total revenue generated in the month", example = "12345.67")
    private Double totalRevenue;

}
