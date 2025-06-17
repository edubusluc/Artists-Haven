package com.artists_heaven.entities.artist;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.entities.user.UserRepository;
import com.artists_heaven.entities.user.UserRole;

class ArtistServiceTest {

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ArtistService artistService;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterArtist_EmailAlreadyExists() {
        Artist artist = new Artist();
        artist.setEmail("test@example.com");

        User userEmail = new User();
        userEmail.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com")).thenReturn(userEmail);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            artistService.registerArtist(artist);
        });

        assertEquals("El correo electrónico ya está registrado.", exception.getMessage());
    }

    @Test
    void testRegisterArtist_ArtistNameAlreadyExists() {
        Artist artist = new Artist();
        artist.setArtistName("existingArtist");

        when(userRepository.findByEmail(anyString())).thenReturn(null);
        when(artistRepository.existsByArtistName("existingArtist")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            artistService.registerArtist(artist);
        });

        assertEquals("Ya existe un usuario con ese nombre registrado", exception.getMessage());
    }

    @Test
    void testRegisterArtist_Success() {
        Artist artist = new Artist();
        artist.setArtistName("newArtist");
        artist.setEmail("test@example.com");
        artist.setPassword("password");

        when(userRepository.findByEmail(anyString())).thenReturn(null);
        when(artistRepository.existsByArtistName(anyString())).thenReturn(false);
        when(artistRepository.save(any(Artist.class))).thenReturn(artist);

        Artist savedArtist = artistService.registerArtist(artist);

        assertNotNull(savedArtist);
        assertEquals("newArtist", savedArtist.getArtistName());
        assertEquals("newArtist", savedArtist.getUsername());
        assertEquals(UserRole.ARTIST, savedArtist.getRole());
        assertTrue(passwordEncoder.matches("password", savedArtist.getPassword()));
    }
}
