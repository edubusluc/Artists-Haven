package com.artists_heaven.auth;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.http.auth.InvalidCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.artists_heaven.configuration.RefreshToken;
import com.artists_heaven.configuration.RefreshTokenService;
import com.artists_heaven.email.EmailSenderService;
import com.artists_heaven.entities.user.PasswordService;
import com.artists_heaven.entities.user.User;
import com.artists_heaven.entities.user.UserRepository;

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
    private final RefreshTokenService refreshTokenService;
    private final PasswordService passwordService;
    private final EmailSenderService emailSenderService;
    private final static String ERROR = "error";

    public AuthController(AuthService authService, UserRepository userRepository,
            RefreshTokenService refreshTokenService, PasswordService passwordService,
            EmailSenderService emailSenderService) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
        this.passwordService = passwordService;
        this.emailSenderService = emailSenderService;
    }

    @Operation(summary = "User login", description = "Authenticates a user using email and password, returning an Access Token and Refresh Token along with the user's role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User authenticated successfully", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"accessToken\": \"...\", \"refreshToken\": \"...\", \"role\": \"ADMIN\"}"))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials or unauthorized access", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"error\": \"Invalid credentials or server error\"}")))
    })
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Login request payload containing user email and password", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginRequest.class))) @RequestBody LoginRequest loginRequest) {
        try {
            // Generate access token
            String accessToken = authService.login(loginRequest.getEmail(), loginRequest.getPassword());

            if (accessToken == null) {
                throw new InvalidCredentialsException("Invalid email or password");
            }

            // Find user by email
            Optional<User> optionalUser = userRepository.findByEmail(loginRequest.getEmail());
            if (!optionalUser.isPresent()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(ERROR, "User not found"));
            }

            User user = optionalUser.get();

            // Generate refresh token
            RefreshToken refreshToken = refreshTokenService.createOrUpdateRefreshToken(user);

            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("token", accessToken);
            response.put("refreshToken", refreshToken.getToken());
            response.put("role", user.getRole());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(ERROR, "Invalid credentials or server error"));
        }
    }

    @Operation(summary = "Google login", description = "Handles user login via Google authentication by validating the Google ID token and returning an Access Token, Refresh Token, and user info.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User authenticated successfully with Google", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"accessToken\": \"...\", \"refreshToken\": \"...\", \"email\": \"user@example.com\", \"role\": \"USER\"}"))),
            @ApiResponse(responseCode = "401", description = "Invalid Google ID token", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"error\": \"Invalid ID token\"}"))),
            @ApiResponse(responseCode = "500", description = "Error verifying token", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"error\": \"Error verifying token\"}")))
    })
    @PostMapping("/google-login")
    public ResponseEntity<Map<String, Object>> googleLogin(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Payload containing the Google ID token string", required = true, content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"idTokenString\": \"...\"}"))) @RequestBody Map<String, String> request) {
        String idTokenString = request.get("idTokenString");

        try {
            Map<String, String> map = authService.handleGoogleLogin(idTokenString);
            String accessToken = map.get("token");
            String email = map.get("email");
            String role = map.get("role");

            if (accessToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(ERROR, "Invalid ID token"));
            }

            // Find user by email
            Optional<User> optionalUser = userRepository.findByEmail(email);
            if (!optionalUser.isPresent()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(ERROR, "User not found"));
            }

            User user = optionalUser.get();

            // Generate refresh token
            RefreshToken refreshToken = refreshTokenService.createOrUpdateRefreshToken(user);

            return ResponseEntity.ok(Map.of(
                    "accessToken", accessToken,
                    "refreshToken", refreshToken.getToken(),
                    "email", email,
                    "role", role));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(ERROR, "Error verifying token"));
        }
    }

    @Operation(summary = "Refresh JWT tokens", description = "Generates a new access token and refresh token by validating and rotating the provided refresh token.")
    @ApiResponse(responseCode = "200", description = "Tokens successfully refreshed", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"accessToken\": \"...\", \"refreshToken\": \"...\"}")))
    @ApiResponse(responseCode = "400", description = "Missing refresh token in request", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"error\": \"Refresh token is required\"}")))
    @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"error\": \"Invalid or expired refresh token\"}")))
    @PostMapping("/refresh-token")
    public ResponseEntity<Map<String, String>> refreshToken(@RequestBody Map<String, String> request) {
        String refreshTokenStr = request.get("refreshToken");

        if (refreshTokenStr == null || refreshTokenStr.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(ERROR, "Refresh token is required"));
        }

        try {
            RefreshToken oldToken = refreshTokenService.findByToken(refreshTokenStr)
                    .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

            refreshTokenService.verifyExpiration(oldToken);

            // Eliminar el token usado (rotación)
            refreshTokenService.deleteByUser(oldToken.getUser());

            // Crear nuevo refresh token
            RefreshToken newRefreshToken = refreshTokenService.createOrUpdateRefreshToken(oldToken.getUser());

            // Generar nuevo access token
            String newAccessToken = authService.generateToken(oldToken.getUser());

            return ResponseEntity.ok(Map.of(
                    "accessToken", newAccessToken,
                    "refreshToken", newRefreshToken.getToken()));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(ERROR, "Invalid or expired refresh token"));
        }
    }

    @Operation(summary = "Change password", description = "Allows an authenticated user to change their password by providing the current and new password.")
    @ApiResponse(responseCode = "200", description = "Password successfully updated", content = @Content(mediaType = "application/json", schema = @Schema(example = "\"Contraseña actualizada\"")))
    @ApiResponse(responseCode = "400", description = "Invalid current password", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"error\": \"Current password is incorrect\"}")))
    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@AuthenticationPrincipal User user,
            @RequestBody ChangePasswordRequest request) {
        passwordService.changePassword(user, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok("Contraseña actualizada");
    }

    @Operation(summary = "Forgot password", description = "Initiates the password reset process by generating a reset token and sending a reset link to the user's email.")
    @ApiResponse(responseCode = "200", description = "Reset email sent if the account exists", content = @Content(mediaType = "application/json", schema = @Schema(example = "\"Correo enviado si la cuenta existe\"")))
    @ApiResponse(responseCode = "400", description = "Missing or invalid email in request", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"error\": \"Invalid email address\"}")))
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        String token = passwordService.createPasswordResetToken(request.getEmail());
        // Aquí envías el email:
        String resetLink = "http://localhost:3000/reset-password?token=" + token;

        emailSenderService.sendPasswordResetEmail(request.getEmail(), resetLink);

        return ResponseEntity.ok("Correo enviado si la cuenta existe");
    }

    @Operation(summary = "Reset password", description = "Resets the user's password using a valid reset token and the new password provided.")
    @ApiResponse(responseCode = "200", description = "Password successfully reset", content = @Content(mediaType = "application/json", schema = @Schema(example = "\"Contraseña restablecida\"")))
    @ApiResponse(responseCode = "400", description = "Invalid or expired reset token", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"error\": \"Invalid or expired reset token\"}")))
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        passwordService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok("Contraseña restablecida");
    }

}
