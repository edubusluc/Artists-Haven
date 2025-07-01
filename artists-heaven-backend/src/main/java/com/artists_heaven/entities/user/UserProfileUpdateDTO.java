package com.artists_heaven.entities.user;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "UserProfileUpdateDTO", description = "DTO for updating user profile information, including image uploads.")
public class UserProfileUpdateDTO extends BaseUserProfileDTO {

    @Schema(description = "Profile image file")
    private MultipartFile image;

    @Schema(description = "Banner image file")
    private MultipartFile bannerImage;
}
