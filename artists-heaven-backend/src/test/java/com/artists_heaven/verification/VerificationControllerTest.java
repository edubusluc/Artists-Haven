package com.artists_heaven.verification;

import com.artists_heaven.entities.artist.Artist;
import com.artists_heaven.entities.artist.ArtistRepository;
import com.artists_heaven.email.EmailSenderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class VerificationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EmailSenderService emailSenderService;

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private VerificationRepository verificationRepository;

    @InjectMocks
    private VerificationController verificationController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(verificationController).build();
    }

    @Test
    public void testSendValidation_Success() throws Exception {
        // Arrange
        String email = "artist@example.com";
        Artist artist = new Artist();
        artist.setEmail(email);
        artist.setIsvalid(false);
        artist.setId(1L);

        when(artistRepository.findByEmail(email)).thenReturn(artist);
        when(verificationRepository.findByArtistId(artist.getId())).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(((MockMultipartHttpServletRequestBuilder) multipart("/api/verification/send")
                .param("email", email))
                .file(new MockMultipartFile("video", "test-video.mp4", "video/mp4", new byte[0])) // Proper way to add a
                                                                                                  // file
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Solicitud enviada correctamente"));

        verify(artistRepository, times(1)).findByEmail(email);
        verify(verificationRepository, times(1)).findByArtistId(artist.getId());
        verify(emailSenderService, times(1)).sendVerificationEmail(artist);
    }

    @Test
    public void testSendValidation_ArtistNotFound() throws Exception {
        // Arrange
        String email = "unknown@example.com";
        when(artistRepository.findByEmail(email)).thenReturn(null);

        // Act & Assert
        mockMvc.perform(((MockMultipartHttpServletRequestBuilder) multipart("/api/verification/send")
                .param("email", email))
                .file(new MockMultipartFile("video", "test-video.mp4", "video/mp4", new byte[0])) // Proper way to add a
                                                                                                  // file
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("El usuario no es un artista"));

        verify(artistRepository, times(1)).findByEmail(email);
        verify(verificationRepository, never()).findByArtistId(anyLong());
        verify(emailSenderService, never()).sendVerificationEmail(any(Artist.class));
    }

    @Test
    public void testSendValidation_ArtistNotElegible() throws Exception {

        // Arrange
        String email = "artist@example.com";
        Artist artist = new Artist();
        artist.setEmail(email);
        artist.setIsvalid(true); // No válido para verificación
        artist.setId(1L);

        when(artistRepository.findByEmail(email)).thenReturn(artist);
        when(verificationRepository.findByArtistId(artist.getId())).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(((MockMultipartHttpServletRequestBuilder) multipart("/api/verification/send")
                .param("email", email))
                .file(new MockMultipartFile("video", "test-video.mp4", "video/mp4", new byte[0])) // Proper way to add a
                                                                                                  // file
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Usuario no válido o ya verificado"));
    }

    @Test
    public void testSendValidation_WithPendingVerification() throws Exception {

        // Arrange
        String email = "artist@example.com";
        Artist artist = new Artist();
        artist.setEmail(email);
        artist.setIsvalid(false); // No válido para verificación
        artist.setId(1L);
        Verification verificationPending = new Verification();
        verificationPending.setStatus(VerificationStatus.PENDING);

        List<Verification> list = new ArrayList<>();
        list.add(verificationPending);

        when(artistRepository.findByEmail(email)).thenReturn(artist);
        when(verificationRepository.findByArtistId(artist.getId())).thenReturn(list);

        // Act & Assert
        mockMvc.perform(((MockMultipartHttpServletRequestBuilder) multipart("/api/verification/send")
                .param("email", email))
                .file(new MockMultipartFile("video", "test-video.mp4", "video/mp4", new byte[0])) // Proper way to add a
                                                                                                  // file
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Ya existe una solicitud para este usuario"));
    }

    @Test
    public void testSendValidation_ExceptionHandling() throws Exception {
        // Arrange
        String email = "artist@example.com";
        Artist artist = new Artist();
        artist.setEmail(email);
        artist.setIsvalid(true);
        artist.setId(1L);

        // Simular la excepción al llamar al método findByEmail
        when(artistRepository.findByEmail(email)).thenThrow(new RuntimeException("Error en la base de datos"));

        // Act & Assert
        mockMvc.perform(((MockMultipartHttpServletRequestBuilder) multipart("/api/verification/send")
                .param("email", email))
                .file(new MockMultipartFile("video", "test-video.mp4", "video/mp4", new byte[0])) // Proper way to add a
                                                                                                  // file
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isInternalServerError()) // Verifica el estado 500 INTERNAL_SERVER_ERROR
                .andExpect(jsonPath("$.error").value("Error al procesar la solicitud"));

        verify(artistRepository, times(1)).findByEmail(email);
    }

}
