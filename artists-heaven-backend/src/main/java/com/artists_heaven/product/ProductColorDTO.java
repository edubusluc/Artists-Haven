package com.artists_heaven.product;

import java.util.List;
import java.util.Map;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(name = "ProductColorDTO", description = "Represents a product variant by color, including its images, available sizes, stock, and model reference.")
public class ProductColorDTO {

    @Schema(description = "Unique identifier of the color variant", example = "301")
    private Long colorId;

    @Schema(description = "Name of the color", example = "Red")
    private String colorName;

    @Schema(description = "Hexadecimal code of the color", example = "#FF0000")
    private String hexCode;

    @Schema(description = "List of image URLs for this color variant", example = "[\"https://example.com/images/red_front.jpg\", \"https://example.com/images/red_back.jpg\"]")
    private List<String> images;

    @Schema(description = "Map of sizes to available quantity for each size", example = "{\"S\": 10, \"M\": 15, \"L\": 5}")
    private Map<String, Integer> sizes;

    @Schema(description = "Total number of available units for this color variant", example = "30")
    private Integer availableUnits;

    @Schema(description = "Reference code or model number of the product", example = "TSH-RED-001")
    private String modelReference;

}
