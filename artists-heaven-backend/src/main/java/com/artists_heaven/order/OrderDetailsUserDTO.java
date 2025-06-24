package com.artists_heaven.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Order details visible to the user, excluding sensitive payment information.")
public class OrderDetailsUserDTO extends OrderDetailsDTO {
    public OrderDetailsUserDTO(Order order) {
        super(order);
        this.setPaymentIntent(null);
    }

}
