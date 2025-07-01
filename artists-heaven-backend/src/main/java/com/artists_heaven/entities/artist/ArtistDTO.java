package com.artists_heaven.entities.artist;

import java.util.List;

import com.artists_heaven.event.Event;
import com.artists_heaven.product.Product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema
public class ArtistDTO {

    private String artistName;
    private List<Event> artistEvents;
    private List<Product> artistProducts;
    private String primaryColor;
    private String bannerPhoto;


}
