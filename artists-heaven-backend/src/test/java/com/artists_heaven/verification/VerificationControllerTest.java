package com.artists_heaven.verification;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import com.artists_heaven.email.EmailSenderService;
import com.artists_heaven.entities.artist.Artist;
import com.artists_heaven.images.ImageServingUtil;

class VerificationControllerTest {

    @Mock
    private EmailSenderService emailSenderService;

    @Mock
    private VerificationService verificationService;

    @Mock
    private ImageServingUtil imageServingUtil;

    @InjectMocks
    private VerificationController verificationController;

    private final String UPLOAD_DIR = "artists-heaven-backend/src/main/resources/verification_media";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendValidation_ArtistNotFound() {
        String email = "test@example.com";
        MultipartFile video = mock(MultipartFile.class);

        when(verificationService.validateArtist(email)).thenReturn(null);

        ResponseEntity<Map<String, Object>> response = verificationController.sendValidation(email, video);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("User is not an artist", response.getBody().get("error"));
    }

    @Test
    void testSendValidation_ArtistNotEligible() {
        String email = "test@example.com";
        MultipartFile video = mock(MultipartFile.class);
        Artist artist = new Artist();

        when(verificationService.validateArtist(email)).thenReturn(artist);
        when(verificationService.isArtistEligibleForVerification(artist)).thenReturn(false);

        ResponseEntity<Map<String, Object>> response = verificationController.sendValidation(email, video);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("User is not eligible or already verified", response.getBody().get("error"));
    }

    @Test
    void testSendValidation_HasPendingVerification() {
        String email = "test@example.com";
        MultipartFile video = mock(MultipartFile.class);
        Artist artist = new Artist();

        when(verificationService.validateArtist(email)).thenReturn(artist);
        when(verificationService.isArtistEligibleForVerification(artist)).thenReturn(true);
        when(verificationService.hasPendingVerification(artist)).thenReturn(true);

        ResponseEntity<Map<String, Object>> response = verificationController.sendValidation(email, video);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("There is already a pending request for this user", response.getBody().get("error"));
    }

    @Test
    void testSendValidation_Success() throws IOException {
        String email = "test@example.com";
        MultipartFile video = mock(MultipartFile.class);
        Artist artist = new Artist();
        String videoUrl = "http://example.com/video.mp4";

        when(verificationService.validateArtist(email)).thenReturn(artist);
        when(verificationService.isArtistEligibleForVerification(artist)).thenReturn(true);
        when(verificationService.hasPendingVerification(artist)).thenReturn(false);
        when(imageServingUtil.saveImages(video, UPLOAD_DIR, "/verification_media/", true)).thenReturn(videoUrl);

        ResponseEntity<Map<String, Object>> response = verificationController.sendValidation(email, video);

        verify(verificationService).createVerification(artist, videoUrl);
        verify(emailSenderService).sendVerificationEmail(artist);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Request submitted successfully", response.getBody().get("message"));
    }

    @Test
    void testSendValidation_Exception() {
        String email = "test@example.com";
        MultipartFile video = mock(MultipartFile.class);

        when(verificationService.validateArtist(email)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<Map<String, Object>> response = verificationController.sendValidation(email, video);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error processing the request", response.getBody().get("error"));
    }
}