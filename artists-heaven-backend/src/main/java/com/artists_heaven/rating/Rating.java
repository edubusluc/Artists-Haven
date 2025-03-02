package com.artists_heaven.rating;

import org.hibernate.validator.constraints.Length;

import com.artists_heaven.entities.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Max(5)
    @Positive
    @NotNull
    private Integer score;

    @Length(max = 255)
    private String comment;

    @ManyToOne
    @NotNull
    @JsonIgnore
    private User user;

    @JsonProperty("email") // Esto asegura que solo se incluya el email del usuario
    public String getUserEmail() {
        return user != null ? user.getEmail() : null;
    }


}
