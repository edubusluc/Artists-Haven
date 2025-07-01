package com.artists_heaven.entities.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseUserProfileDTO {

    @Schema(description = "Unique ID of the user", example = "101")
    private Long id;

    @Schema(description = "Username of the user", example = "artlover92")
    private String username;

    @Schema(description = "Email address of the user", example = "artlover@example.com")
    private String email;

    @Schema(description = "First name", example = "Alice")
    private String firstName;

    @Schema(description = "Last name", example = "Smith")
    private String lastName;

    @Schema(description = "Artist name", example = "TheBrushMaster")
    private String artistName;

    @Schema(description = "Address", example = "123 Art Street")
    private String address;

    @Schema(description = "City", example = "Paris")
    private String city;

    @Schema(description = "Postal code", example = "75000")
    private String postalCode;

    @Schema(description = "Country", example = "France")
    private String country;

    @Schema(description = "Phone number", example = "+33 6 12 34 56 78")
    private String phone;

    @Schema(description = "Main color for the profile", example = "#FF5733")
    private String color;


}
