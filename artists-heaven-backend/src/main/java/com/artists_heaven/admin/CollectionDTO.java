package com.artists_heaven.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "CollectionDTO", description = "Represents a product collection, including its identifier, name, and promotion status.")
public class CollectionDTO {

    @Schema(description = "Unique identifier of the collection", example = "2001")
    private Long id;

    @Schema(description = "Name of the collection", example = "Summer Collection 2025")
    private String name;

    @Schema(description = "Indicates whether the collection is promoted or featured", example = "true")
    private Boolean isPromoted;

    public CollectionDTO(Long id, String name, Boolean isPromoted) {
        this.id = id;
        this.name = name;
        this.isPromoted = isPromoted;
    }
}
