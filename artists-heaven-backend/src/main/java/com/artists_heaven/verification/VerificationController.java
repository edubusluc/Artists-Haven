package com.artists_heaven.verification;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.artists_heaven.email.EmailSenderService;
import com.artists_heaven.entities.artist.Artist;
import com.artists_heaven.images.ImageServingUtil;
import com.artists_heaven.standardResponse.StandardResponse;

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

    private final ImageServingUtil imageServingUtil;

    private final MessageSource messageSource;

    private final String UPLOAD_DIR = "artists-heaven-backend/src/main/resources/verification_media";

    public VerificationController(EmailSenderService emailSenderService, VerificationService verificationService,
            ImageServingUtil imageServingUtil, MessageSource messageSource) {
        this.emailSenderService = emailSenderService;
        this.verificationService = verificationService;
        this.imageServingUtil = imageServingUtil;
        this.messageSource = messageSource;
    }

    @PostMapping("/send")
    @Operation(summary = "Submit artist verification request", description = "Allows an artist to submit a verification request by uploading a validation video and providing their email.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verification request submitted successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - user is not an artist or not eligible", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflict - duplicate request already exists", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error while processing the verification request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    })
    public ResponseEntity<StandardResponse<String>> sendValidation(
            @Parameter(description = "Email of the artist submitting the verification request", required = true) @RequestParam("email") String email,
            @Parameter(description = "Validation video file uploaded by the artist", required = true, content = @Content(mediaType = "video/*")) @RequestParam("video") MultipartFile video, @RequestParam String lang) {

        Artist artist = verificationService.validateArtistForVerification(email);

        String videoUrl = imageServingUtil.saveImages(video, UPLOAD_DIR, "/verification_media/", true);
        verificationService.createVerification(artist, videoUrl);

        emailSenderService.sendVerificationEmail(artist);

        Locale locale = new Locale(lang);
        String message = messageSource.getMessage("verification.message.successful", null, locale);

        return ResponseEntity.ok(
                new StandardResponse<>(message, HttpStatus.OK.value()));
    }

}
