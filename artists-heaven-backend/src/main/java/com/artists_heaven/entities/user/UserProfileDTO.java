package com.artists_heaven.entities.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object representing the profile details of a user.
 * Used to expose user information in API responses, excluding sensitive data.
 */
@Getter
@Setter
@Schema(
    name = "UserProfileDTO",
    description = "Represents the public profile information of a user."
)
public class UserProfileDTO {

    /**
     * Unique identifier of the user.
     */
    @Schema(description = "Unique ID of the user", example = "101")
    private Long id;

    /**
     * The username used for login and display.
     */
    @Schema(description = "Username of the user", example = "artlover92")
    private String username;

    /**
     * Email address of the user.
     */
    @Schema(description = "Email address of the user", example = "artlover@example.com")
    private String email;

    /**
     * First name of the user.
     */
    @Schema(description = "First name of the user", example = "Alice")
    private String firstName;

    /**
     * Last name of the user.
     */
    @Schema(description = "Last name of the user", example = "Smith")
    private String lastName;

    /**
     * Public artist name, if the user is an artist.
     */
    @Schema(description = "Artist name if applicable", example = "TheBrushMaster")
    private String artistName;

    /**
     * Full postal address of the user.
     */
    @Schema(description = "Postal address of the user", example = "123 Art Street")
    private String address;

    /**
     * Role of the user in the system (e.g., USER, ADMIN, ARTIST).
     */
    @Schema(description = "Role of the user in the system", example = "ARTIST")
    private String role;

    /**
     * City where the user is located.
     */
    @Schema(description = "City of residence", example = "Paris")
    private String city;

    /**
     * Default constructor for serialization/deserialization.
     */
    public UserProfileDTO() {
        // Required by frameworks like Jackson
    }

    /**
     * Constructs a UserProfileDTO from a User entity.
     *
     * @param user The User entity to convert.
     */
    public UserProfileDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.address = user.getAddress();
        this.city = user.getCity();
        this.role = user.getRole().name();
    }
}
