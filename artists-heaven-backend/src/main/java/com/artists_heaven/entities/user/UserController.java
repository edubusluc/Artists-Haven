package com.artists_heaven.entities.user;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException.Unauthorized;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Retrieve all users", description = "Fetches a list of all registered users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of users retrieved successfully", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = User.class))))
    })
    @GetMapping("/list")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @Operation(summary = "Register a new user", description = "Registers a new user with the provided user details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User successfully registered", content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400", description = "Invalid user data", content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/register")
    public ResponseEntity<User> registerUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "User object containing the user details", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))) @RequestBody User user) {
        try {
            User registeredUser = userService.registerUser(user);
            return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @Operation(summary = "Get authenticated user's profile", description = "Retrieves the profile information of the currently authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User profile retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserProfileDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - user is not authenticated", content = @Content(mediaType = "application/json", schema = @Schema(example = "\"User is not authenticated\""))),
            @ApiResponse(responseCode = "500", description = "Internal server error while retrieving user profile", content = @Content(mediaType = "application/json", schema = @Schema(example = "\"Error retrieving user profile\"")))
    })
    @GetMapping("/profile")
    public ResponseEntity<Object> getUserProfile(Principal principal) {
        try {
            UserProfileDTO userProfileDTO = userService.getUserProfile(principal);
            return ResponseEntity.ok(userProfileDTO);
        } catch (Unauthorized e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving user profile");
        }
    }

    @Operation(summary = "Update authenticated user's profile", description = "Updates the profile information of the currently authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User profile updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"message\": \"Profile updated successfully\"}"))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - user is not authenticated", content = @Content(mediaType = "application/json", schema = @Schema(example = "\"User is not authenticated\""))),
            @ApiResponse(responseCode = "500", description = "Internal server error while updating user profile", content = @Content(mediaType = "application/json", schema = @Schema(example = "\"Error updating user profile\"")))
    })
    @PutMapping("/profile/edit")
    public ResponseEntity<Object> updateUserProfile(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "User profile data to update", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserProfileDTO.class))) @RequestBody UserProfileDTO userProfileDTO,
            Principal principal) {
        try {
            userService.updateUserProfile(userProfileDTO, principal);
            return ResponseEntity.ok(Map.of("message", "Profile updated successfully"));
        } catch (Unauthorized e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating user profile");
        }
    }

}
