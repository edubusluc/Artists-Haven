package com.artists_heaven.entities.artist;

import java.util.Set;

import org.hibernate.validator.constraints.URL;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.event.Event;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@DiscriminatorValue("ARTIST")
public class Artist extends User {

    @Column(name = "artist_name")
    private String artistName;

    @Column(name = "artist_url")
    @URL(message = "La URL proporcionada no es válida")
    private String url;

    @Column(name = "is_valid")
    private Boolean isvalid = false;

    @OneToMany(mappedBy = "artist")
    @JsonIgnore
    private Set<Event> events;
}