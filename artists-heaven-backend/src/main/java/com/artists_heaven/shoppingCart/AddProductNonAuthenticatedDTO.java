package com.artists_heaven.shoppingCart;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddProductNonAuthenticatedDTO {

    private ShoppingCart shoppingCart ;
    private Long productId;
    private String size;

}
