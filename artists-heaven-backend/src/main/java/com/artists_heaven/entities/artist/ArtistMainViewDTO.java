package com.artists_heaven.entities.artist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(name = "ArtistMainViewDTO", description = "Represents the main view of an artist, including their identifier, name, and main photo URL.")
@Getter
@Setter
public class ArtistMainViewDTO {

    @Schema(description = "Unique identifier of the artist", example = "101")
    private Long id;

    @Schema(description = "Name of the artist", example = "John Doe")
    private String name;

    @Schema(description = "URL of the artist's main photo", example = "https://example.com/images/john_doe_main.jpg")
    private String mainPhoto;

    public ArtistMainViewDTO(Artist artist) {
        this.id = artist.getId();
        this.name = artist.getArtistName();
        this.mainPhoto = artist.getMainViewPhoto();
    }

}
