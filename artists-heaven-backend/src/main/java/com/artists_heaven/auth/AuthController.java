package com.artists_heaven.auth;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.auth.InvalidCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.entities.user.UserRepository;
import com.artists_heaven.entities.user.UserRole;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    private final UserRepository userRepository;

    private final String TOKEN = "token";

    private final String ERROR = "error";

    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @Operation(summary = "User login", description = "Authenticates a user using email and password, returning a JWT token and the user's role upon success.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User authenticated successfully", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\", \"role\": \"ADMIN\"}"))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials or unauthorized access", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"error\": \"Invalid credentials or server error\"}")))
    })
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Login request payload containing user email and password", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginRequest.class))) @RequestBody LoginRequest loginRequest) {
        try {
            // Attempt to authenticate the user and generate a token
            String token = authService.login(loginRequest.getEmail(), loginRequest.getPassword());

            // Check if the generated token is null (authentication failed)
            if (token == null) {
                throw new InvalidCredentialsException("Invalid email or password");
            }

            // Retrieve the user from the database using the provided email
            User user = userRepository.findByEmail(loginRequest.getEmail());

            // Check if the user exists in the database
            if (user == null) {
                return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
            }

            // Retrieve the role of the authenticated user
            UserRole role = user.getRole();

            // Create a response map to include the token and user role
            Map<String, Object> response = new HashMap<>();
            response.put(TOKEN, token);
            response.put("role", role);

            // Return a successful response with the token and user role
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            // Handle general exceptions with optional logging for better debugging
            return new ResponseEntity<>(
                    Map.of(ERROR, "Invalid credentials or server error"),
                    HttpStatus.UNAUTHORIZED);
        }
    }

    @Operation(summary = "Google login", description = "Handles user login via Google authentication by validating the Google ID token and returning a JWT token along with user info.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User authenticated successfully with Google", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\", \"email\": \"user@example.com\", \"role\": \"USER\"}"))),
            @ApiResponse(responseCode = "401", description = "Invalid Google ID token", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"error\": \"Invalid ID token\"}"))),
            @ApiResponse(responseCode = "500", description = "Error verifying token", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"error\": \"Error verifying token\"}")))
    })
    @PostMapping("/google-login")
    public ResponseEntity<Map<String, String>> googleLogin(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Payload containing the Google ID token string", required = true, content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"idTokenString\": \"eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...\"}"))) @RequestBody Map<String, String> request) {
        String idTokenString = request.get("idTokenString");

        try {
            Map<String, String> map = authService.handleGoogleLogin(idTokenString);
            String token = map.get(TOKEN);
            String email = map.get("email");
            String role = map.get("role");

            if (token != null) {
                return new ResponseEntity<>(Map.of(TOKEN, token, "email", email, "role", role), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(Map.of(ERROR, "Invalid ID token"), HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of(ERROR, "Error verifying token"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
