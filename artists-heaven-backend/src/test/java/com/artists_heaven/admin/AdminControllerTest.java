package com.artists_heaven.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.Files;

import org.springframework.core.io.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpHeaders;

import com.artists_heaven.entities.artist.Artist;
import com.artists_heaven.entities.artist.ArtistRepository;
import com.artists_heaven.verification.Verification;
import com.artists_heaven.verification.VerificationRepository;
import com.artists_heaven.verification.VerificationStatus;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;;

public class AdminControllerTest {

    @Mock
    private Resource resource;

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private VerificationRepository verificationRepository;

    @InjectMocks
    private AdminController adminController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
    }

    @Test
    public void testValidateArtistSuccess() {
        // Arrange
        Long artistId = 1L;
        Artist artist = new Artist();
        artist.setId(artistId);
        artist.setIsvalid(false);

        Verification verification = new Verification();
        verification.setId(1L);
        verification.setStatus(VerificationStatus.PENDING);

        Map<String, Long> payload = new HashMap<>();
        payload.put("id", 1L);
        payload.put("verificationId", 1L);

        when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));
        when(verificationRepository.findById(1L)).thenReturn(Optional.of(verification));

        // Act
        ResponseEntity<?> response = adminController.validateArtist(payload);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Artista verificado de forma correcta", ((Map<String, String>) response.getBody()).get("message"));

        assertEquals(true, artist.getIsvalid());
        assertEquals(VerificationStatus.ACCEPTED, verification.getStatus());

        verify(artistRepository).save(artist);
        verify(verificationRepository).save(verification);
    }

    @Test
    public void testValidateArtistNullArtist() {
        // Arrange
        Long artistId = 1L;
        Artist artist = new Artist();
        artist.setId(artistId);
        artist.setIsvalid(false);

        Verification verification = new Verification();
        verification.setId(1L);
        verification.setStatus(VerificationStatus.PENDING);

        Map<String, Long> payload = new HashMap<>();
        payload.put("id", 1L);
        payload.put("verificationId", 1L);

        when(artistRepository.findById(artistId)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> adminController.validateArtist(payload));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Artista no encontrado", exception.getReason());
    }

    @Test
    public void testValidateArtistNullVerification() {
        // Arrange
        Long artistId = 1L;
        Artist artist = new Artist();
        artist.setId(artistId);
        artist.setIsvalid(false);

        Verification verification = new Verification();
        verification.setId(1L);
        verification.setStatus(VerificationStatus.PENDING);

        Map<String, Long> payload = new HashMap<>();
        payload.put("id", 1L);
        payload.put("verificationId", 1L);

        when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));

        when(verificationRepository.findById(1L)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> adminController.validateArtist(payload));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Verificación no encontrada", exception.getReason());
    }

    @Test
    public void testGetAllValidation() throws Exception {
        // Arrange
        Verification verification1 = new Verification();
        verification1.setId(1L);
        verification1.setStatus(VerificationStatus.PENDING);

        Verification verification2 = new Verification();
        verification2.setId(2L);
        verification2.setStatus(VerificationStatus.PENDING);

        List<Verification> verificationList = Arrays.asList(verification1, verification2);

        when(verificationRepository.findAll()).thenReturn(verificationList);

        // Act & Assert
        mockMvc.perform(get("/api/admin/verification/pending"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"id\":1,\"status\":\"PENDING\"},{\"id\":2,\"status\":\"PENDING\"}]"));

        ResponseEntity<?> response = adminController.getAllValidation();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(verificationList, response.getBody());
    }

    @Test
    public void testGetVerificationVideoSuccess() throws Exception {
        String fileName = "sample.mp4";
        String basePath = System.getProperty("user.dir") + "/artists-heaven-backend/src/main/resources/verification_media/";
        Path filePath = Paths.get(basePath, fileName);

        // Crea un archivo de muestra para la prueba
        Files.deleteIfExists(filePath);
        Files.createDirectories(filePath.getParent());
        Files.createFile(filePath);

        when(resource.exists()).thenReturn(true);

        mockMvc.perform(get("/api/admin/verification_media/" + fileName))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "video/mp4"));

        // Elimina el archivo de muestra después de la prueba
        Files.deleteIfExists(filePath);
    }


    @Test
    public void testGetVerificationVideoNotFound() throws Exception {
        // Arrange
        String fileName = "non-existent-video.mp4";
        when(resource.exists()).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/admin/verification_media/{fileName}", fileName))
                .andExpect(status().isNotFound());
    }

}
