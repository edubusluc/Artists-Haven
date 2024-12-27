package com.artists_heaven.event;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventDTO {

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotNull
    private LocalDate date;

    @NotBlank
    private String location;

    private String moreInfo;

    @NotNull
    private Long artistId;

    private String image;

}
