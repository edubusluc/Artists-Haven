package com.artists_heaven.entities.user;

import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.artists_heaven.images.ImageServingUtil;
import com.artists_heaven.standardResponse.StandardResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    private final ImageServingUtil imageServingUtil;

    private static final String UPLOAD_DIR = "artists-heaven-backend/src/main/resources/mainArtist_media/";

    public UserController(UserService userService, ImageServingUtil imageServingUtil) {
        this.userService = userService;
        this.imageServingUtil = imageServingUtil;
    }

    @Operation(summary = "Register a new user", description = "Registers a new user with the provided user details.")
    @ApiResponse(responseCode = "201", description = "User successfully registered", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid user data", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    @PostMapping("/register")
    public ResponseEntity<StandardResponse<User>> registerUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "User object containing the registration details", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserRegisterDTO.class))) @RequestBody UserRegisterDTO userRegisterDTO,
            @RequestParam String lang) {

        User registeredUser = userService.registerUser(userRegisterDTO, lang);

        StandardResponse<User> response = new StandardResponse<>(
                "User successfully registered",
                registeredUser,
                HttpStatus.CREATED.value());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get authenticated user's profile", description = "Retrieves the profile information of the currently authenticated user.")
    @ApiResponse(responseCode = "200", description = "User profile retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized - user is not authenticated", content = @Content(mediaType = "application/json", schema = @Schema(example = "\"User is not authenticated\"")))
    @ApiResponse(responseCode = "500", description = "Internal server error while retrieving user profile", content = @Content(mediaType = "application/json", schema = @Schema(example = "\"Error retrieving user profile\"")))
    @GetMapping("/profile")
    public ResponseEntity<StandardResponse<UserProfileDTO>> getUserProfile(Principal principal) {
        UserProfileDTO userProfileDTO = userService.getUserProfile(principal);

        StandardResponse<UserProfileDTO> response = new StandardResponse<>(
                "User profile retrieved successfully",
                userProfileDTO,
                HttpStatus.OK.value());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update authenticated user's profile", description = "Updates the profile information of the currently authenticated user.")
    @ApiResponse(responseCode = "200", description = "User profile updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class), examples = @ExampleObject(value = "{\"message\": \"Profile updated successfully\", \"data\": null, \"status\": 200}")))
    @ApiResponse(responseCode = "401", description = "Unauthorized - user is not authenticated", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"User is not authenticated\", \"data\": null, \"status\": 401}")))
    @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"User not found\", \"data\": null, \"status\": 404}")))
    @ApiResponse(responseCode = "500", description = "Internal server error while updating user profile", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Unexpected error occurred\", \"data\": null, \"status\": 500}")))
    @PutMapping("/profile/edit")
    public ResponseEntity<StandardResponse<Void>> updateUserProfile(
            @ModelAttribute UserProfileUpdateDTO userProfileDTO,
            Principal principal,
            @RequestParam String lang) {

        String mainImage = "";

        MultipartFile image = userProfileDTO.getImage();

        if (image != null && !image.isEmpty()) {
            mainImage = imageServingUtil.saveImages(image, UPLOAD_DIR, "/mainArtist_media/", false);
        }

        userService.updateUserProfile(userProfileDTO, principal, mainImage, lang);

        return ResponseEntity.ok(new StandardResponse<>("Profile updated successfully", 200));
    }

}
