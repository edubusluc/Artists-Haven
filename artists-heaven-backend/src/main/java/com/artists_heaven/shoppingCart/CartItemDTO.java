package com.artists_heaven.shoppingCart;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartItemDTO {

    private ProductItemDTO product;
    private String size;
    private int quantity;

}
