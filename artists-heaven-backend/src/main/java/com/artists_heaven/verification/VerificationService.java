package com.artists_heaven.verification;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.artists_heaven.entities.artist.Artist;
import com.artists_heaven.entities.artist.ArtistRepository;

@Service
public class VerificationService {

    private final ArtistRepository artistRepository;

    private final VerificationRepository verificationRepository;

    private static final String UPLOAD_DIR = "artists-heaven-backend/src/main/resources/verification_media";
    private static final Path TARGET_PATH = new File(UPLOAD_DIR).toPath().normalize();

    public VerificationService(ArtistRepository artistRepository, VerificationRepository verificationRepository) {
        this.artistRepository = artistRepository;
        this.verificationRepository = verificationRepository;
    }

    public Artist validateArtist(String email) {
        return artistRepository.findByEmail(email);
    }

    public boolean isArtistEligibleForVerification(Artist artist) {
        return artist != null && !artist.getIsVerificated();
    }

    public boolean hasPendingVerification(Artist artist) {
        List<Verification> verificationCheck = verificationRepository.findByArtistId(artist.getId());
        return verificationCheck.stream().anyMatch(v -> v.getStatus() == VerificationStatus.PENDING);
    }

    public void createVerification(Artist artist, String videoUrl) {
        Verification verification = new Verification();
        verification.setArtist(artist);
        verification.setVideoUrl(videoUrl);
        verification.setDate(LocalDateTime.now());
        verification.setStatus(VerificationStatus.PENDING);
        verificationRepository.save(verification);
    }

    public String saveFile(MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("No se ha enviado ningún archivo");
        }

        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del archivo no puede ser nulo o vacío");
        }

        // Asegurarse de que el nombre del archivo esté limpio y no sea nulo
        String fileName = StringUtils.cleanPath(originalFileName.trim());
        Path targetPath = Paths.get(UPLOAD_DIR, fileName).normalize();

        if (!targetPath.startsWith(TARGET_PATH)) {
            throw new IllegalArgumentException("La entrada está fuera del directorio objetivo");
        }

        try {
            // Guardar el archivo en el directorio especificado
            Files.copy(file.getInputStream(), targetPath);
            // Construir la URL relativa del archivo
            fileName = "/verification_media/" + fileName;
        } catch (IOException e) {
            // Lanzar una excepción si ocurre un error al guardar el archivo
            throw new IllegalArgumentException("Error al guardar la imagen.", e);
        }

        return fileName;
    }

}
