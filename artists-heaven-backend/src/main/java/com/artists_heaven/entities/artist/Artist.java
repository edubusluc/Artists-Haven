package com.artists_heaven.entities.artist;

import org.hibernate.validator.constraints.URL;

import com.artists_heaven.entities.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@DiscriminatorValue("ARTIST")
public class Artist extends User {

    @Column(name = "artist_name", unique = true, length = 50)
    @NotBlank(message = "El nombre artístico es obligatorio")
    @Size(max = 50, message = "El nombre artístico no debe superar los 50 caracteres")
    private String artistName;

    @Column(name = "artist_url", length = 255)
    @Size(max = 255, message = "La URL no debe superar los 255 caracteres")
    @URL(message = "La URL proporcionada no es válida")
    private String url;

    @Column(name = "is_verificated")
    private Boolean isVerificated = false;

    @Column(name = "main_view_photo", nullable = false, length = 255)
    @NotBlank(message = "La imagen principal es obligatoria")
    @Size(max = 255, message = "La URL de la imagen principal no debe superar los 255 caracteres")
    private String mainViewPhoto;

    @Column(name = "main_color", nullable = false, length = 7)
    @NotBlank(message = "El color principal es obligatorio")
    @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "El color debe estar en formato hexadecimal, por ejemplo: #FFFFFF")
    private String mainColor;

}