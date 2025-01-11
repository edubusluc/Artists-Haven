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



@RestController
@RequestMapping("/api/verification")
public class VerificationController {

    
    private final EmailSenderService emailSenderService;

    private final VerificationService verificationService;


    public VerificationController(EmailSenderService emailSenderService, VerificationService verificationService) {
        this.emailSenderService = emailSenderService;
        this.verificationService = verificationService;
    }

    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendValidation(
            @RequestParam("email") String email,
            @RequestParam("video") MultipartFile video) {

        try {
            // Validación de la existencia y estado del artista
            Artist artist = verificationService.validateArtist(email);
            if (artist == null) {
                return new ResponseEntity<>(Map.of("error", "El usuario no es un artista"), HttpStatus.UNAUTHORIZED);
            }

            // Verifica si el artista ya está validado o tiene una solicitud pendiente
            if (!verificationService.isArtistEligibleForVerification(artist)) {
                return new ResponseEntity<>(Map.of("error", "Usuario no válido o ya verificado"),
                        HttpStatus.UNAUTHORIZED);
            }

            // Verifica si ya existe una solicitud pendiente
            if (verificationService.hasPendingVerification(artist)) {
                return new ResponseEntity<>(Map.of("error", "Ya existe una solicitud para este usuario"),
                        HttpStatus.UNAUTHORIZED);
            }

            // Guarda el archivo y crea la verificación
            String videoUrl = verificationService.saveFile(video);
            verificationService.createVerification(artist, videoUrl);

            // Envía el correo de verificación
            emailSenderService.sendVerificationEmail(artist);

            return ResponseEntity.ok(Map.of("message", "Solicitud enviada correctamente"));
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("error", "Error al procesar la solicitud"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

     
}
