package com.artists_heaven.admin;

import com.artists_heaven.order.OrderStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderStatusUpdateDTO {

    private Long orderId;
    private OrderStatus status;
    


}
