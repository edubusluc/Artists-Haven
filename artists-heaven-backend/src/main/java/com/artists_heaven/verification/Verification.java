package com.artists_heaven.verification;

import java.time.LocalDateTime;

import com.artists_heaven.entities.artist.Artist;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Verification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "artist_id", nullable = false)
    @NotNull(message = "El artista es obligatorio para la verificación")
    private Artist artist;

    @Column(length = 255)
    @Size(max = 255, message = "La URL del video no debe superar los 255 caracteres")
    private String videoUrl;

    @NotNull(message = "La fecha de verificación es obligatoria")
    @Column(nullable = false, updatable = false)
    private LocalDateTime date = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @NotNull(message = "El estado de verificación es obligatorio")
    @Column(nullable = false)
    private VerificationStatus status;

}
