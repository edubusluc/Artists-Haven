package com.artists_heaven.event;

import java.time.LocalDate;

import com.artists_heaven.entities.artist.Artist;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Entity
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotNull
    private LocalDate date;

    @NotBlank
    private String location;

    private String moreInfo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Artist artist;

    private String image;

}
