package com.artists_heaven.event;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object representing an event organized or attended by an
 * artist.
 * Contains essential event details such as name, description, date, and
 * location.
 */
@Getter
@Setter
@Schema(name = "EventDTO", description = "Represents event data including title, description, date, location, and artist.")
public class EventDTO {

    /**
     * Name or title of the event.
     */
    @NotBlank
    @Schema(description = "Name or title of the event", example = "Modern Art Expo 2025", required = true)
    private String name;

    /**
     * Detailed description of the event.
     */
    @NotBlank
    @Schema(description = "Detailed description of the event", example = "An international art expo showcasing modern installations.", required = true)
    private String description;

    /**
     * Date when the event will take place.
     */
    @NotNull
    @Schema(description = "Date of the event", example = "2025-10-15", required = true, type = "string", format = "date")
    private LocalDate date;

    /**
     * Physical location of the event (e.g., venue or city).
     */
    @NotBlank
    @Schema(description = "Location where the event takes place", example = "Berlin Art Center", required = true)
    private String location;

    /**
     * Optional field with additional information or external links.
     */
    @Schema(description = "Optional additional information (e.g., external link or details)", example = "https://example.com/events/modern-art-expo")
    private String moreInfo;

    /**
     * ID of the artist associated with the event.
     */
    @NotNull
    @Schema(description = "Unique ID of the artist hosting or featured in the event", example = "42", required = true)
    private Long artistId;

    /**
     * Optional image URL or base64-encoded image related to the event.
     */
    @Schema(description = "Optional image associated with the event", example = "https://example.com/images/event-banner.jpg")
    private String image;

    private double latitude;

    private double longitude;

    private String artistName;

    private String color;

    private Long id;

    public EventDTO(){
        
    }

    public EventDTO(Event event){
        this.id = event.getId();
        this.name = event.getName();
        this.description = event.getDescription();
        this.date = event.getDate();
        this.location = event.getLocation();
        this.moreInfo = event.getMoreInfo();
        this.artistId = event.getArtist().getId();
        this.image = event.getImage();
        this.latitude = event.getLatitude();
        this.longitude = event.getLongitude();
        this.artistName = event.getArtist().getArtistName();
        this.color = event.getArtist().getMainColor();

    }
}
