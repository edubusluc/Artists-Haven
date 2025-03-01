package com.artists_heaven.shopping_cart;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartItemDTO {

    private ProductItemDTO product;
    private String size;
    private Integer quantity;

}
