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
@Schema(name = "UserProfileDTO", description = "Represents the public profile information of a user.")
public class UserProfileDTO extends BaseUserProfileDTO {

    @Schema(description = "Role of the user in the system", example = "ARTIST")
    private String role;
    private String image;
    private String bannerImage;

    public UserProfileDTO(User user) {
        this.setId(user.getId());
        this.setUsername(user.getUsername());
        this.setEmail(user.getEmail());
        this.setFirstName(user.getFirstName());
        this.setLastName(user.getLastName());
        this.setAddress(user.getAddress());
        this.setCity(user.getCity());
        this.setPostalCode(user.getPostalCode());
        this.setCountry(user.getCountry());
        this.setPhone(user.getPhone());
        this.role = user.getRole().name();
    }

    public UserProfileDTO() {
        //TODO Auto-generated constructor stub
    }
}
