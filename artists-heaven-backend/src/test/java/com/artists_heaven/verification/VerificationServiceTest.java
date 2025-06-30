package com.artists_heaven.verification;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.artists_heaven.entities.artist.Artist;
import com.artists_heaven.entities.artist.ArtistRepository;

import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.Optional;

class VerificationServiceTest {

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private VerificationRepository verificationRepository;

    @InjectMocks
    private VerificationService verificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testValidateArtist() {
        String email = "test@example.com";
        Artist artist = new Artist();
        when(artistRepository.findByEmail(email)).thenReturn(artist);

        Artist result = verificationService.validateArtist(email);

        assertEquals(artist, result);
    }

    @Test
    void testIsArtistEligibleForVerification_True() {
        Artist artist = new Artist();
        artist.setIsVerificated(false);

        boolean result = verificationService.isArtistEligibleForVerification(artist);

        assertTrue(result);
    }

    @Test
    void testIsArtistEligibleForVerification_False() {
        Artist artist = new Artist();
        artist.setIsVerificated(true);

        boolean result = verificationService.isArtistEligibleForVerification(artist);

        assertFalse(result);
    }

    @Test
    void testHasPendingVerification_True() {
        Artist artist = new Artist();
        artist.setId(1L);
        Verification verification = new Verification();
        verification.setStatus(VerificationStatus.PENDING);
        when(verificationRepository.findByArtistId(artist.getId())).thenReturn(List.of(verification));

        boolean result = verificationService.hasPendingVerification(artist);

        assertTrue(result);
    }

    @Test
    void testCreateVerification() {
        Artist artist = new Artist();
        artist.setId(1L);
        String videoUrl = "http://example.com/video.mp4";

        verificationService.createVerification(artist, videoUrl);

        ArgumentCaptor<Verification> verificationCaptor = ArgumentCaptor.forClass(Verification.class);
        verify(verificationRepository).save(verificationCaptor.capture());

        Verification savedVerification = verificationCaptor.getValue();
        assertEquals(artist, savedVerification.getArtist());
        assertEquals(videoUrl, savedVerification.getVideoUrl());
        assertNotNull(savedVerification.getDate());
        assertEquals(VerificationStatus.PENDING, savedVerification.getStatus());
    }

    @Test
    void testHasPendingVerification_False() {
        Artist artist = new Artist();
        artist.setId(1L);
        when(verificationRepository.findByArtistId(artist.getId())).thenReturn(List.of());

        boolean result = verificationService.hasPendingVerification(artist);

        assertFalse(result);
    }

    @Test
    void refuseVerfication() {
        Verification verification = new Verification();
        verification.setId(1l);
        verification.setStatus(VerificationStatus.PENDING);

        when(verificationRepository.findById(1l)).thenReturn(Optional.of(verification));

        verificationService.refuseVerification(1L);
        assertEquals(VerificationStatus.REJECTED, verification.getStatus());
    }

    @Test
    void refuseVerification_shouldThrowException_whenVerificationNotFound() {
        when(verificationRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> {
            verificationService.refuseVerification(1L);
        });
    }

}