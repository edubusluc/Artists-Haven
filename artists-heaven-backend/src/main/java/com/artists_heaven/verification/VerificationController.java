package com.artists_heaven.verification;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.artists_heaven.email.EmailSenderService;
import com.artists_heaven.entities.artist.Artist;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/verification")
public class VerificationController {

    private final EmailSenderService emailSenderService;

    private final VerificationService verificationService;

    private final String ERROR = "error";

    public VerificationController(EmailSenderService emailSenderService, VerificationService verificationService) {
        this.emailSenderService = emailSenderService;
        this.verificationService = verificationService;
    }

    @PostMapping("/send")
    @Operation(summary = "Submit artist verification request", description = "Allows an artist to submit a verification request by uploading a validation video and providing their email.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verification request submitted successfully", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"message\":\"Request submitted successfully\"}"))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - user is not an artist or not eligible for verification", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"error\":\"User is not an artist\"}"))),
            @ApiResponse(responseCode = "500", description = "Internal server error while processing the verification request", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"error\":\"Error processing the request\"}")))
    })
    public ResponseEntity<Map<String, Object>> sendValidation(
            @Parameter(description = "Email of the artist submitting the verification request", required = true) @RequestParam("email") String email,
            @Parameter(description = "Validation video file uploaded by the artist", required = true, content = @Content(mediaType = "video/*")) @RequestParam("video") MultipartFile video) {

        try {
            // Validate existence and status of the artist
            Artist artist = verificationService.validateArtist(email);
            if (artist == null) {
                return new ResponseEntity<>(Map.of(ERROR, "User is not an artist"), HttpStatus.UNAUTHORIZED);
            }

            // Check if the artist is already verified or has a pending request
            if (!verificationService.isArtistEligibleForVerification(artist)) {
                return new ResponseEntity<>(Map.of(ERROR, "User is not eligible or already verified"),
                        HttpStatus.UNAUTHORIZED);
            }

            // Check if there is an existing pending verification request
            if (verificationService.hasPendingVerification(artist)) {
                return new ResponseEntity<>(Map.of(ERROR, "There is already a pending request for this user"),
                        HttpStatus.UNAUTHORIZED);
            }

            // Save the video file and create the verification request
            String videoUrl = verificationService.saveFile(video);
            verificationService.createVerification(artist, videoUrl);

            // Send verification email
            emailSenderService.sendVerificationEmail(artist);

            return ResponseEntity.ok(Map.of("message", "Request submitted successfully"));
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of(ERROR, "Error processing the request"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
