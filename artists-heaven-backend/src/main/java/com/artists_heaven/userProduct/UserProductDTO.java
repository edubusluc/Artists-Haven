package com.artists_heaven.userProduct;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "UserProductDTO", description = "Represents the data required to create or update a user's product, including its name, description, and images.")
public class UserProductDTO {

    @Schema(description = "Name of the product", example = "Handmade Wooden Chair")
    private String name;

    @Schema(description = "Detailed description of the product", example = "A beautifully handcrafted wooden chair made from oak.")
    private String description;

    @Schema(description = "List of URLs for the product images", example = "[\"https://example.com/image1.jpg\", \"https://example.com/image2.jpg\"]")
    private List<String> images;

}
