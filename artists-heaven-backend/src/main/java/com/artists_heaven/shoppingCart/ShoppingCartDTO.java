package com.artists_heaven.shoppingCart;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShoppingCartDTO {
    private Long id;
    private List<CartItemDTO> items;

    
}
