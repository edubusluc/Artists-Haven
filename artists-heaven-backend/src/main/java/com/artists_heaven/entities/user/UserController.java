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
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Endpoint to retrieve all users
    @GetMapping("/list")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

     // Endpoint to register a new user
    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody User user) {
        try {
            // Call the service to register the user
            User registeredUser = userService.registerUser(user);
            // Return the registered user with a CREATED status
            return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // Return a BAD_REQUEST status if an exception occurs during registration
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Endpoint to get the profile of the currently authenticated user
    @GetMapping("/profile")
    public ResponseEntity<Object> getUserProfile(Principal principal) {
        try {
            // Call the service to get the user profile
            UserProfileDTO userProfileDTO = userService.getUserProfile(principal);
            // Return the profile data with an OK status
            return ResponseEntity.ok(userProfileDTO);
        } catch (Unauthorized e) {
            // Return UNAUTHORIZED status if the user is not authenticated
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            // Return INTERNAL_SERVER_ERROR if any other exception occurs
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener el perfil del usuario");
        }
    }

     // Endpoint to update the profile of the currently authenticated user
    @PutMapping("/profile/edit")
    public ResponseEntity<Object> updateUserProfile(@RequestBody UserProfileDTO userProfileDTO, Principal principal) {
        try {
            // Call the service to update the user profile
            userService.updateUserProfile(userProfileDTO, principal);
            // Return success message if the profile was updated
            return ResponseEntity.ok(Map.of("message", "Perfil actualizado correctamente"));
        } catch (Unauthorized e) {
            // Return UNAUTHORIZED status if the user is not authenticated
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            // Return INTERNAL_SERVER_ERROR if any other exception occurs during update
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar el perfil del usuario");
        }
    }
}
