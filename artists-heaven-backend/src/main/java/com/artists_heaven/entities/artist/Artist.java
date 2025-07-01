package com.artists_heaven.entities.artist;

import org.hibernate.validator.constraints.URL;

import com.artists_heaven.entities.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@DiscriminatorValue("ARTIST")
public class Artist extends User {

    @Column(name = "artist_name", unique = true)
    private String artistName;

    @Column(name = "artist_url")
    @URL(message = "La URL proporcionada no es válida")
    private String url;

    @Column(name = "is_verificated")
    private Boolean isVerificated = false;

    @NotNull
    private String mainViewPhoto;

    @NotNull
    private String mainColor;

    @NotNull
    private String bannerPhoto;


}