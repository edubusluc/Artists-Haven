package com.artists_heaven.admin;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.artists_heaven.entities.artist.Artist;
import com.artists_heaven.entities.artist.ArtistRepository;
import com.artists_heaven.verification.VerficationStatus;
import com.artists_heaven.verification.Verification;
import com.artists_heaven.verification.VerificationRepository;

import java.nio.file.Paths;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    ArtistRepository artistRepository;

    @Autowired
    VerificationRepository verificationRepository;

    @PostMapping("/validate_artist")
    public ResponseEntity<?> validateArtist(@RequestBody Map<String, Long> payload) {
        Long artistId = payload.get("id");

        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Artista no encontrado"));

        artist.setIsvalid(true);
        artistRepository.save(artist);

        Long verificationId = payload.get("verificationId");
        Verification verification = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Verificación no encontrado"));

        verification.setStatus(VerficationStatus.ACCEPTED);
        verificationRepository.save(verification);
        
        return ResponseEntity.ok(Map.of("message", "Artista verificado de forma correcta"));

    }

    ////////////////////////////////////////////
    // Get all verification request
    ////////////////////////////////////////////

    @GetMapping("/verification/pending")
    public ResponseEntity<?> getAllValidation() {
        List<Verification> verificationList = verificationRepository.findAll();

        return ResponseEntity.ok(verificationList);
    }

    @GetMapping("/verification_media/{fileName:.+}")
    public ResponseEntity<Resource> getVerificationVideo(@PathVariable String fileName) {
        // Obtén el directorio base del proyecto de forma dinámica
        String basePath = System.getProperty("user.dir") + "/artists-heaven-backend/src/main/resources/verification_media/";
        Path filePath = Paths.get(basePath, fileName);
        Resource resource = new FileSystemResource(filePath.toFile());
    
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
    
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "video/mp4")
                .body(resource);
    }
    

}
