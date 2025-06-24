package com.artists_heaven.entities.artist;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema
@Getter
@Setter
public class ArtistRegisterDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String artistName;
    private String url;

    private MultipartFile image;

}
