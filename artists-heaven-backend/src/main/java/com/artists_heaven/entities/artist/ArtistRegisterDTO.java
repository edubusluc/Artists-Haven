package com.artists_heaven.entities.artist;

import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(name = "ArtistRegisterDTO", description = "Represents the data required to register a new artist, including personal info, credentials, branding, and profile image.")
@Getter
@Setter
public class ArtistRegisterDTO {

    @Schema(description = "First name of the artist", example = "John")
    private String firstName;

    @Schema(description = "Last name of the artist", example = "Doe")
    private String lastName;

    @Schema(description = "Email address of the artist, used for login and communication", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Password for the artist account", example = "P@ssw0rd123")
    private String password;

    @Schema(description = "Stage name or professional name of the artist", example = "Johnny D")
    private String artistName;

    @Schema(description = "Personal or promotional URL for the artist", example = "https://johnnyd.com")
    private String url;

    @Schema(description = "Profile image file for the artist", type = "string", format = "binary")
    private MultipartFile image;

    @Schema(description = "Primary color used for the artist's branding in hexadecimal format", example = "#FF5733")
    private String color;

}
