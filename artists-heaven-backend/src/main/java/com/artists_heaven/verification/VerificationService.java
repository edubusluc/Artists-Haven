package com.artists_heaven.verification;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.artists_heaven.entities.artist.Artist;
import com.artists_heaven.entities.artist.ArtistRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class VerificationService {

    private final ArtistRepository artistRepository;

    private final VerificationRepository verificationRepository;

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

    public void refuseVerification(Long id) {
        Verification verification = verificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Verification not found"));

        verification.setStatus(VerificationStatus.REJECTED);
        verificationRepository.save(verification);
    }

}
