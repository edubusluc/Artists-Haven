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

    /**
     * Retrieves an artist by their email address.
     *
     * @param email the email of the artist
     * @return the Artist entity if found, or null if not found
     */
    public Artist validateArtist(String email) {
        return artistRepository.findByEmail(email);
    }

    /**
     * Checks whether an artist is eligible for verification.
     * An artist is eligible if they exist and have not yet been verified.
     *
     * @param artist the Artist entity to check
     * @return true if the artist is eligible for verification, false otherwise
     */
    public boolean isArtistEligibleForVerification(Artist artist) {
        return artist != null && !artist.getIsVerificated();
    }

    /**
     * Checks whether an artist has any pending verification requests.
     *
     * @param artist the Artist entity to check
     * @return true if there is at least one pending verification request, false
     *         otherwise
     */
    public boolean hasPendingVerification(Artist artist) {
        List<Verification> verificationCheck = verificationRepository.findByArtistId(artist.getId());
        return verificationCheck.stream().anyMatch(v -> v.getStatus() == VerificationStatus.PENDING);
    }

    /**
     * Creates a new verification request for an artist.
     * The status of the request will be set to PENDING and the current date/time is
     * recorded.
     *
     * @param artist   the Artist entity for which the verification is requested
     * @param videoUrl the URL of the verification video provided by the artist
     */
    public void createVerification(Artist artist, String videoUrl) {
        Verification verification = new Verification();
        verification.setArtist(artist);
        verification.setVideoUrl(videoUrl);
        verification.setDate(LocalDateTime.now());
        verification.setStatus(VerificationStatus.PENDING);
        verificationRepository.save(verification);
    }

    /**
     * Refuses (rejects) a verification request by its ID.
     * Updates the status of the request to REJECTED.
     *
     * @param id the ID of the verification request to refuse
     * @throws ResourceNotFoundException if the verification request does not exist
     */
    public void refuseVerification(Long id) {
        Verification verification = verificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Verification not found"));

        verification.setStatus(VerificationStatus.REJECTED);
        verificationRepository.save(verification);
    }

    /**
     * Validates an artist for verification, checking all eligibility rules.
     * Throws exceptions if the artist does not exist, is not eligible, or already
     * has a pending request.
     *
     * @param email the email of the artist to validate
     * @return the Artist entity if all checks pass
     * @throws AppExceptions.ForbiddenActionException if the user is not an artist
     *                                                or is not eligible
     * @throws AppExceptions.DuplicateActionException if there is already a pending
     *                                                verification request
     */
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
