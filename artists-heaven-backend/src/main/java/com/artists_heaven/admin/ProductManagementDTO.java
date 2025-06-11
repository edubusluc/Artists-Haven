package com.artists_heaven.admin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductManagementDTO {
    private Integer notAvailableProducts;
    private Integer availableProducts;
    private Integer promotedProducts;
    private Integer totalProducts;

}
