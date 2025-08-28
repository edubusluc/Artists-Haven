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
import com.artists_heaven.exception.AppExceptions;
import com.artists_heaven.exception.AppExceptions.ResourceNotFoundException;

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
        assertThrows(ResourceNotFoundException.class, () -> {
            verificationService.refuseVerification(1L);
        });
    }

    @Test
    void validateArtistForVerification_ArtistNotFound_ThrowsForbiddenException() {
        // Arrange
        String email = "test@example.com";

        // Simulamos que el repositorio no encuentra el artista
        when(artistRepository.findByEmail(email)).thenReturn(null);

        // Act & Assert
        AppExceptions.ForbiddenActionException ex = assertThrows(
                AppExceptions.ForbiddenActionException.class,
                () -> verificationService.validateArtistForVerification(email));

        assertEquals("User is not an artist", ex.getMessage());
    }

    @Test
    void validateArtistForVerification_NotEligible_ThrowsForbiddenException() {
        // Arrange
        String email = "test@example.com";

        Artist artist = new Artist();
        artist.setIsVerificated(true);

        // Simulamos que el artista existe
        when(artistRepository.findByEmail(email)).thenReturn(artist);

        // Act & Assert
        AppExceptions.ForbiddenActionException ex = assertThrows(
                AppExceptions.ForbiddenActionException.class,
                () -> verificationService.validateArtistForVerification(email));

        assertEquals("User is not eligible or already verified", ex.getMessage());
    }

    @Test
    void validateArtistForVerification_HasPendingVerification_ThrowsDuplicateActionException() {
        // Arrange
        String email = "test@example.com";
        Artist artist = new Artist();
        artist.setId(1L);

        Verification verification = new Verification();
        verification.setStatus(VerificationStatus.PENDING);

        // Simular que el artista existe
        when(artistRepository.findByEmail(email)).thenReturn(artist);

        when(verificationRepository.findByArtistId(artist.getId()))
                .thenReturn(List.of(verification));

        // Act & Assert
        AppExceptions.DuplicateActionException ex = assertThrows(
                AppExceptions.DuplicateActionException.class,
                () -> verificationService.validateArtistForVerification(email));

        assertEquals("There is already a pending request for this user", ex.getMessage());
    }

    @Test
    void validateArtistForVerification_AllChecksPass_ReturnsArtist() {
        // Arrange
        String email = "test@example.com";
        Artist artist = new Artist();
        artist.setId(1L);

        // Simular que el artista existe
        when(artistRepository.findByEmail(email)).thenReturn(artist);

        Verification verification = new Verification();
        verification.setStatus(VerificationStatus.REJECTED);

        // Simular que no tiene verificaciones pendientes
        when(verificationRepository.findByArtistId(artist.getId()))
                .thenReturn(List.of(verification));

        // Act
        Artist result = verificationService.validateArtistForVerification(email);

        // Assert
        assertEquals(artist, result);
    }

}