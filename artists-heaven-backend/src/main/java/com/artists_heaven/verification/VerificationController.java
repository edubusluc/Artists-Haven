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

@RestController
@RequestMapping("/api/verification")
public class VerificationController {

    private final EmailSenderService emailSenderService;

    private final VerificationService verificationService;

    private final ImageServingUtil imageServingUtil;

    private final MessageSource messageSource;

    public VerificationController(EmailSenderService emailSenderService, VerificationService verificationService,
            ImageServingUtil imageServingUtil, MessageSource messageSource) {
        this.emailSenderService = emailSenderService;
        this.verificationService = verificationService;
        this.imageServingUtil = imageServingUtil;
        this.messageSource = messageSource;
    }

    @PostMapping("/send")
    @Operation(summary = "Submit artist verification request", description = "Allows an artist to submit a verification request by uploading a validation video and providing their email.")
    @ApiResponse(responseCode = "200", description = "Verification request submitted successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden - user is not an artist or not eligible", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    @ApiResponse(responseCode = "409", description = "Conflict - duplicate request already exists", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error while processing the verification request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    public ResponseEntity<StandardResponse<String>> sendValidation(
            @Parameter(description = "Email of the artist submitting the verification request", required = true) @RequestParam("email") String email,
            @Parameter(description = "Validation video file uploaded by the artist", required = true, content = @Content(mediaType = "video/*")) @RequestParam("video") MultipartFile video,
            @RequestParam String lang) {

        // Validar que el artista puede enviar verificación
        Artist artist = verificationService.validateArtistForVerification(email);

        // Validar y guardar el video de verificación usando la función genérica
        if (video == null || video.isEmpty()) {
            throw new IllegalArgumentException("The validation video file is empty or invalid.");
        }

        String videoUrl = imageServingUtil.saveMediaFile(
                video, // Archivo a guardar
                "verification_media", // Carpeta física
                "/verification_media/", // URL pública para frontend
                true // Permitimos videos
        );

        // Crear la verificación en base de datos
        verificationService.createVerification(artist, videoUrl);

        // Enviar correo de confirmación
        emailSenderService.sendVerificationEmail(artist);

        // Mensaje localizado
        Locale locale = new Locale(lang);
        String message = messageSource.getMessage("verification.message.successful", null, locale);

        return ResponseEntity.ok(new StandardResponse<>(message, HttpStatus.OK.value()));
    }

}
