package com.artists_heaven.auth;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.auth.InvalidCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.artists_heaven.configuration.JwtTokenProvider;
import com.artists_heaven.entities.user.User;
import com.artists_heaven.entities.user.UserRepository;
import com.artists_heaven.entities.user.UserRole;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    private final UserRepository userRepository;

    public AuthController(AuthService authService, UserRepository userRepository, JwtTokenProvider jwtTokenProvider,
            PasswordEncoder passwordEncoder, TokenVerifier tokenVerifier) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    // Endpoint to handle user login requests
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest loginRequest) {
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
            response.put("token", token);
            response.put("role", role);

            // Return a successful response with the token and user role
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            // Handle general exceptions with optional logging for better debugging
            return new ResponseEntity<>(Map.of("error", "Invalid credentials or server error"),
                    HttpStatus.UNAUTHORIZED);
        }
    }

    // Endpoint to handle login requests via Google authentication
    @PostMapping("/google-login")
    public ResponseEntity<Map<String, String>> googleLogin(@RequestBody Map<String, String> request) {
        // Extract the ID token string from the request payload
        String idTokenString = request.get("idTokenString");

        try {
            // Validate and process the Google ID token using the authentication service
            Map<String, String> map = authService.handleGoogleLogin(idTokenString);

             // Retrieve the generated JWT token and user email from the response map
            String token = map.get("token");
            String email = map.get("email");

            // Check if the token is successfully generated
            if (token != null) {
                // Return a successful response containing the JWT token and user email
                return new ResponseEntity<>(Map.of("token", token, "email",email), HttpStatus.OK);
            } else {
                 // Return an Unauthorized response if the ID token is invalid
                return new ResponseEntity<>(Map.of("error", "Invalid ID token"), HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            // Handle exceptions with a generic error message and internal server error status
            return new ResponseEntity<>(Map.of("error", "Error verifying token"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
