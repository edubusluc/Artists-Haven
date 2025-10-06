package com.artists_heaven.entities.artist;

import java.util.List;
import com.artists_heaven.event.Event;
import com.artists_heaven.product.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "ArtistDTO", description = "Represents an artist, including their name, events, products, and primary color for branding.")
public class ArtistDTO {

    @Schema(description = "Name of the artist", example = "John Doe")
    private String artistName;

    @Schema(description = "List of events associated with the artist", example = "[]")
    private List<Event> artistEvents;

    @Schema(description = "List of products associated with the artist", example = "[]")
    private List<Product> artistProducts;

    @Schema(description = "Primary color used for the artist's branding, in hexadecimal format", example = "#FF5733")
    private String primaryColor;

}
