package com.artists_heaven.entities.artist;

import java.util.Map;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "ArtistDashboardDTO", description = "Represents aggregated dashboard data for an artist, including verification status, event counts, and sales statistics.")
public class ArtistDashboardDTO {

    @Schema(description = "Indicates whether the artist is verified ('true' or 'false')", example = "true")
    private String isVerificated;

    @Schema(description = "Number of upcoming/future events for the artist", example = "5")
    private Integer futureEvents;

    @Schema(description = "Number of past events the artist has performed", example = "12")
    private Integer pastEvents;

    @Schema(description = "Map of product names to the number of times each item was ordered", example = "{\"T-Shirt\": 20, \"Cap\": 15}")
    private Map<String, Integer> orderItemCount;

    @Schema(description = "Map of country names to the number of items sold in each country", example = "{\"USA\": 50, \"Argentina\": 30}")
    private Map<String, Integer> mostCountrySold;

}
