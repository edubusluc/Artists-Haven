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

public class VerificationControllerTest {

    @Mock
    private EmailSenderService emailSenderService;

    @Mock
    private VerificationService verificationService;

    @InjectMocks
    private VerificationController verificationController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSendValidation_ArtistNotFound() {
        String email = "test@example.com";
        MultipartFile video = mock(MultipartFile.class);

        when(verificationService.validateArtist(email)).thenReturn(null);

        ResponseEntity<Map<String, Object>> response = verificationController.sendValidation(email, video);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("El usuario no es un artista", response.getBody().get("error"));
    }

    @Test
    public void testSendValidation_ArtistNotEligible() {
        String email = "test@example.com";
        MultipartFile video = mock(MultipartFile.class);
        Artist artist = new Artist();

        when(verificationService.validateArtist(email)).thenReturn(artist);
        when(verificationService.isArtistEligibleForVerification(artist)).thenReturn(false);

        ResponseEntity<Map<String, Object>> response = verificationController.sendValidation(email, video);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Usuario no válido o ya verificado", response.getBody().get("error"));
    }

    @Test
    public void testSendValidation_HasPendingVerification() {
        String email = "test@example.com";
        MultipartFile video = mock(MultipartFile.class);
        Artist artist = new Artist();

        when(verificationService.validateArtist(email)).thenReturn(artist);
        when(verificationService.isArtistEligibleForVerification(artist)).thenReturn(true);
        when(verificationService.hasPendingVerification(artist)).thenReturn(true);

        ResponseEntity<Map<String, Object>> response = verificationController.sendValidation(email, video);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Ya existe una solicitud para este usuario", response.getBody().get("error"));
    }

    @Test
    public void testSendValidation_Success() throws IOException {
        String email = "test@example.com";
        MultipartFile video = mock(MultipartFile.class);
        Artist artist = new Artist();
        String videoUrl = "http://example.com/video.mp4";

        when(verificationService.validateArtist(email)).thenReturn(artist);
        when(verificationService.isArtistEligibleForVerification(artist)).thenReturn(true);
        when(verificationService.hasPendingVerification(artist)).thenReturn(false);
        when(verificationService.saveFile(video)).thenReturn(videoUrl);

        ResponseEntity<Map<String, Object>> response = verificationController.sendValidation(email, video);

        verify(verificationService).createVerification(artist, videoUrl);
        verify(emailSenderService).sendVerificationEmail(artist);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Solicitud enviada correctamente", response.getBody().get("message"));
    }

    @Test
    public void testSendValidation_Exception() {
        String email = "test@example.com";
        MultipartFile video = mock(MultipartFile.class);

        when(verificationService.validateArtist(email)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<Map<String, Object>> response = verificationController.sendValidation(email, video);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error al procesar la solicitud", response.getBody().get("error"));
    }
}