package com.artists_heaven.verification;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;
import com.artists_heaven.entities.artist.Artist;
import com.artists_heaven.entities.artist.ArtistRepository;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

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

    @ParameterizedTest
    @MethodSource("provideInvalidFileNames")
    void testSaveFile_InvalidFileName(String originalFileName, String expectedMessage) {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn(originalFileName);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            verificationService.saveFile(file);
        });

        assertEquals(expectedMessage, exception.getMessage());
    }

    private static Stream<Arguments> provideInvalidFileNames() {
        return Stream.of(
                Arguments.of(null, "El nombre del archivo no puede ser nulo o vacío"),
                Arguments.of(" ", "El nombre del archivo no puede ser nulo o vacío"),
                Arguments.of("../test.mp4", "La entrada está fuera del directorio objetivo"));
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
    void testSaveFile_EmptyFile() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            verificationService.saveFile(file);
        });

        assertEquals("No se ha enviado ningún archivo", exception.getMessage());
    }

    @Test
    void testSaveFile_IOException() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("test.mp4");
        when(file.getInputStream()).thenThrow(new IOException("Test IOException"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            verificationService.saveFile(file);
        });

        assertEquals("Error al guardar la imagen.", exception.getMessage());
    }

    @Test
    void testSaveFile_NullFileName() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            verificationService.saveFile(file);
        });

        assertEquals("El nombre del archivo no puede ser nulo o vacío", exception.getMessage());
    }

    @Test
    void testSaveFile_EmptyFileName() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn(" ");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            verificationService.saveFile(file);
        });

        assertEquals("El nombre del archivo no puede ser nulo o vacío", exception.getMessage());
    }

    @Test
    void testSaveFile_InvalidPath() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("../test.mp4");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            verificationService.saveFile(file);
        });

        assertEquals("La entrada está fuera del directorio objetivo", exception.getMessage());
    }

}