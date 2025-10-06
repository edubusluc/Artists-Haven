package com.artists_heaven.event;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object representing an event organized or attended by an artist.
 * <p>
 * This DTO is used to transfer essential event information in API responses
 * without exposing unnecessary internal data.
 * </p>
 * <p>
 * Fields included:
 * <ul>
 *     <li>{@code id}: Unique identifier of the event.</li>
 *     <li>{@code name}: Name or title of the event.</li>
 *     <li>{@code description}: Detailed description of the event.</li>
 *     <li>{@code date}: Date when the event takes place.</li>
 *     <li>{@code location}: Physical location or venue of the event.</li>
 *     <li>{@code moreInfo}: Optional additional information, e.g., a link or extra details.</li>
 *     <li>{@code artistId}: Unique identifier of the artist associated with the event.</li>
 *     <li>{@code image}: Optional image URL or base64-encoded image representing the event.</li>
 *     <li>{@code latitude}: Latitude coordinate of the event location.</li>
 *     <li>{@code longitude}: Longitude coordinate of the event location.</li>
 *     <li>{@code artistName}: Name of the associated artist.</li>
 *     <li>{@code color}: Main color associated with the artist (for UI purposes).</li>
 * </ul>
 * </p>
 */
@Getter
@Setter
@Schema(name = "EventDTO", description = "Represents event data including title, description, date, location, and artist.")
@NoArgsConstructor
public class EventDTO {

    @NotBlank
    @Schema(description = "Name or title of the event", example = "Modern Art Expo 2025")
    private String name;

    @NotBlank
    @Schema(description = "Detailed description of the event", example = "An international art expo showcasing modern installations.")
    private String description;

    @NotNull
    @Schema(description = "Date of the event", example = "2025-10-15", type = "string", format = "date")
    private LocalDate date;

    @NotBlank
    @Schema(description = "Location where the event takes place", example = "Berlin Art Center")
    private String location;

    @Schema(description = "Optional additional information (e.g., external link or details)", example = "https://example.com/events/modern-art-expo")
    private String moreInfo;

    @NotNull
    @Schema(description = "Unique ID of the artist hosting or featured in the event", example = "42")
    private Long artistId;

    @Schema(description = "Optional image associated with the event", example = "https://example.com/images/event-banner.jpg")
    private String image;

    @Schema(description = "Latitude coordinate of the event location", example = "52.5200")
    private double latitude;

    @Schema(description = "Longitude coordinate of the event location", example = "13.4050")
    private double longitude;

    @Schema(description = "Name of the artist associated with the event", example = "John Doe")
    private String artistName;

    @Schema(description = "Main color associated with the artist (for UI display purposes)", example = "#FF5733")
    private String color;

    @Schema(description = "Unique identifier of the event", example = "101")
    private Long id;

    /**
     * Constructs an {@code EventDTO} from an {@link Event} entity.
     *
     * @param event the event entity from which to create the DTO
     */
    public EventDTO(Event event) {
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
