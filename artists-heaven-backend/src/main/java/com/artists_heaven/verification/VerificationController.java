package com.artists_heaven.verification;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.artists_heaven.auth.LoginRequest;
import com.artists_heaven.email.EmailSenderService;
import com.artists_heaven.entities.artist.Artist;
import com.artists_heaven.entities.artist.ArtistRepository;

@RestController
@RequestMapping("/api/verification")
public class VerificationController {

    @Autowired
    EmailSenderService emailSenderService;

    @Autowired
    ArtistRepository artistRepository;

    @Autowired
    VerificationRepository verificationRepository;

    private String url = "artists-heaven-backend/src/verification_media";

    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendValidation(
            @RequestParam("email") String email,
            @RequestParam("video") MultipartFile video) {

        try {
            // Validación de la existencia y estado del artista
            Artist artist = validateArtist(email);
            if (artist == null) {
                return new ResponseEntity<>(Map.of("error", "El usuario no es un artista"), HttpStatus.UNAUTHORIZED);
            }

            // Verifica si el artista ya está validado o tiene una solicitud pendiente
            if (!isArtistEligibleForVerification(artist)) {
                return new ResponseEntity<>(Map.of("error", "Usuario no válido o ya verificado"),
                        HttpStatus.UNAUTHORIZED);
            }

            // Verifica si ya existe una solicitud pendiente
            if (hasPendingVerification(artist)) {
                return new ResponseEntity<>(Map.of("error", "Ya existe una solicitud para este usuario"),
                        HttpStatus.UNAUTHORIZED);
            }

            // Guarda el archivo y crea la verificación
            String videoUrl = saveFile(video);
            createVerification(artist, videoUrl);

            // Envía el correo de verificación
            emailSenderService.sendVerificationEmail(artist);

            return ResponseEntity.ok(Map.of("message", "Solicitud enviada correctamente"));
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("error", "Error al procesar la solicitud"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Artist validateArtist(String email) {
        return artistRepository.findByEmail(email);
    }

    private boolean isArtistEligibleForVerification(Artist artist) {
        return artist != null && !artist.getIsvalid();
    }

    private boolean hasPendingVerification(Artist artist) {
        List<Verification> verificationCheck = verificationRepository.findByArtistId(artist.getId());
        return verificationCheck.stream().anyMatch(v -> v.getStatus() == VerficationStatus.PENDING);
    }

    private void createVerification(Artist artist, String videoUrl) {
        Verification verification = new Verification();
        verification.setArtist(artist);
        verification.setVideoUrl(videoUrl);
        verification.setDate(LocalDateTime.now());
        verification.setStatus(VerficationStatus.PENDING);
        verificationRepository.save(verification);
    }

    private String saveFile(MultipartFile file) throws IOException {
        // Aquí puedes usar un servicio de almacenamiento en la nube o guardar
        // localmente.
        String directory = url;
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(directory, fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return filePath.toString();
    }
}
