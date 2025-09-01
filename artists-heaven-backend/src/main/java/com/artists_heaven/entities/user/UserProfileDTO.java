package com.artists_heaven.entities.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object representing the public profile details of a user.
 * <p>
 * This DTO is used to expose user information in API responses while excluding sensitive data,
 * such as passwords or internal system fields. It extends {@link BaseUserProfileDTO} to include
 * common profile attributes.
 * </p>
 * <p>
 * Fields included:
 * <ul>
 *     <li>{@code id}: Unique identifier of the user.</li>
 *     <li>{@code username}: Username of the user.</li>
 *     <li>{@code email}: Email address of the user.</li>
 *     <li>{@code firstName}: First name of the user.</li>
 *     <li>{@code lastName}: Last name of the user.</li>
 *     <li>{@code address}: Address of the user.</li>
 *     <li>{@code city}: City of residence of the user.</li>
 *     <li>{@code postalCode}: Postal code of the user's address.</li>
 *     <li>{@code country}: Country of residence of the user.</li>
 *     <li>{@code phone}: Phone number of the user.</li>
 *     <li>{@code role}: Role of the user in the system (e.g., "ARTIST", "ADMIN").</li>
 *     <li>{@code image}: Profile picture URL of the user.</li>
 *     <li>{@code bannerImage}: Banner image URL of the user's profile.</li>
 * </ul>
 * </p>
 */
@Getter
@Setter
@Schema(name = "UserProfileDTO", description = "Represents the public profile information of a user.")
@NoArgsConstructor
public class UserProfileDTO extends BaseUserProfileDTO {

    @Schema(description = "Role of the user in the system", example = "ARTIST")
    private String role;

    @Schema(description = "Profile image URL of the user", example = "https://example.com/images/profile.jpg")
    private String image;

    @Schema(description = "Banner image URL displayed in the user's profile", example = "https://example.com/images/banner.jpg")
    private String bannerImage;

    /**
     * Constructs a {@code UserProfileDTO} from a {@link User} entity.
     *
     * @param user the user entity from which to create the DTO
     */
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
}
