package com.artists_heaven.entities.artist;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema
public class ArtistDashboardDTO {

    private String isVerificated;
    private Integer futureEvents;
    private Integer pastEvents;
    private Map<String, Integer> orderItemCount;
    private Map<String, Integer> mostCountrySold;

}
