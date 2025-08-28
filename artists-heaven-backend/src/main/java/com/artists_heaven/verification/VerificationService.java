package com.artists_heaven.verification;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.artists_heaven.entities.artist.Artist;
import com.artists_heaven.entities.artist.ArtistRepository;
import com.artists_heaven.exception.AppExceptions;
import com.artists_heaven.exception.AppExceptions.ResourceNotFoundException;

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
                .orElseThrow(() -> new ResourceNotFoundException("Verification not found"));

        verification.setStatus(VerificationStatus.REJECTED);
        verificationRepository.save(verification);
    }

    public Artist validateArtistForVerification(String email) {
        Artist artist = validateArtist(email);
        if (artist == null) {
            throw new AppExceptions.ForbiddenActionException("User is not an artist");
        }

        if (!isArtistEligibleForVerification(artist)) {
            throw new AppExceptions.ForbiddenActionException("User is not eligible or already verified");
        }

        if (hasPendingVerification(artist)) {
            throw new AppExceptions.DuplicateActionException("There is already a pending request for this user");
        }

        return artist;
    }

}
