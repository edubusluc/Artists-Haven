package com.artists_heaven.admin;

import java.util.Map;

import com.artists_heaven.email.EmailType;
import com.artists_heaven.order.OrderStatus;
import com.artists_heaven.verification.VerificationStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderStatisticsDTO {
    private Integer numOrders;
    private Double incomePerYear;
    private Map<EmailType, Integer> emailCounts;
    private Integer numUsers;
    private Integer numArtists;
    private Map<OrderStatus, Integer> orderStatusCounts;
    private Map<VerificationStatus, Integer> verificationStatusCounts;
    private Map<String, Integer> orderItemCount;
    private Map<String, Integer> categoryItemCount;
    private Map<String, Integer> mostCountrySold;
}
