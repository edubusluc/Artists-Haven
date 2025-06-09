package com.artists_heaven.admin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MonthlySalesDTO {
    private Integer month;
    private Long totalOrders; 
    private Double totalRevenue; 

    public MonthlySalesDTO(Integer month, Long totalOrders, Double totalRevenue) {
        this.month = month;
        this.totalOrders = totalOrders;
        this.totalRevenue = totalRevenue;
    }

}