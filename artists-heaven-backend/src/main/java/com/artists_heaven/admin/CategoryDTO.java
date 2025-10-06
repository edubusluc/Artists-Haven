package com.artists_heaven.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "CategoryDTO", description = "Represents a product category with its identifier and name.")
public class CategoryDTO {

    @Schema(description = "Unique identifier of the category", example = "101")
    private Long id;

    @Schema(description = "Name of the category", example = "T-Shirts")
    private String name;

    public CategoryDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
