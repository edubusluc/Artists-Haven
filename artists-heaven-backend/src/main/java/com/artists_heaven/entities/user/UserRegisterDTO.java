package com.artists_heaven.entities.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "UserRegisterDTO", description = "Represents the data required to register a new user, including personal information, credentials, and address details.")
public class UserRegisterDTO {

    @Schema(description = "First name of the user", example = "Jane")
    private String firstName;

    @Schema(description = "Last name of the user", example = "Doe")
    private String lastName;

    @Schema(description = "Email address of the user, used for login and communication", example = "jane.doe@example.com")
    private String email;

    @Schema(description = "Username chosen by the user for login", example = "janedoe123")
    private String username;

    @Schema(description = "Password for the user account", example = "P@ssw0rd123")
    private String password;

    @Schema(description = "Phone number of the user", example = "+541112345678")
    private String phone;

    @Schema(description = "Country of residence of the user", example = "Argentina")
    private String country;

    @Schema(description = "Postal code of the user's address", example = "C1001")
    private String postalCode;

    @Schema(description = "City of residence of the user", example = "Buenos Aires")
    private String city;

    @Schema(description = "Street address of the user", example = "Av. Corrientes 1234")
    private String address;

}
