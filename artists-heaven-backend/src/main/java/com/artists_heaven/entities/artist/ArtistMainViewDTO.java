package com.artists_heaven.entities.artist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema
@Getter
@Setter
public class ArtistMainViewDTO {
    private Long id;
    private String name;
    private String mainPhoto;

    public ArtistMainViewDTO(Artist artist){
        this.id = artist.getId();
        this.name = artist.getArtistName();
        this.mainPhoto = artist.getMainViewPhoto();
    }

}
