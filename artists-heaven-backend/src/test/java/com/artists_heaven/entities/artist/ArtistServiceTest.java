package com.artists_heaven.entities.artist;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import com.artists_heaven.entities.user.UserRole;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ArtistServiceTest {

    @Autowired 
    private ArtistService artistService;

    @Autowired
    private ArtistRepository artistRepository;

    private static Artist artist;

    @BeforeAll
    public static void setup() {
        artist = new Artist();
        artist.setEmail("test@example.com");
        artist.setFirstName("Artist name");
        artist.setLastName("Artist lastName");
        artist.setArtistName("Test Artist");
        artist.setUrl("https://www.artist-pages.com");
        artist.setPassword("password");
    }

    @Test
    @Transactional
    public void testRegisterArtist() {
        artistService.registerArtist(artist);

        List<Artist> users = artistRepository.findAll();
        Artist artist_test = users.get(0);

        assertThat(artist_test.getPassword()).isNotEqualTo("password");
        assertThat(artist_test.getRole()).isEqualTo(UserRole.ARTIST);
        
    }

    @Test
    @Transactional
    public void testRegisterArtistDuplicateEmail() {
        artistService.registerArtist(artist);

        Artist artist_duplicate_email = new Artist();
        artist_duplicate_email.setEmail("test@example.com");
        artist_duplicate_email.setFirstName("Other Artist name");
        artist_duplicate_email.setLastName("Other Artist lastName");
        artist_duplicate_email.setArtistName("Other Test Artist");
        artist_duplicate_email.setUrl("https://www.other-artist-pages.com");
        artist_duplicate_email.setPassword("otherpassword");

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            artistService.registerArtist(artist_duplicate_email);  // Intentamos registrar el segundo artista
        });
        Assertions.assertTrue(exception.getMessage().contains("El correo electrónico ya está registrado."));
        
    }

    @Test
    @Transactional
    public void testRegisterArtistDuplicateArtistName() {
        artistService.registerArtist(artist);

        Artist artist_duplicate_artist_name = new Artist();
        artist_duplicate_artist_name.setEmail("othertest@example.com");
        artist_duplicate_artist_name.setFirstName("Other Artist name");
        artist_duplicate_artist_name.setLastName("Other Artist lastName");
        artist_duplicate_artist_name.setArtistName("Test Artist");
        artist_duplicate_artist_name.setUrl("https://www.other-artist-pages.com");
        artist_duplicate_artist_name.setPassword("otherpassword");

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            artistService.registerArtist(artist_duplicate_artist_name);  // Intentamos registrar el segundo artista
        });
        Assertions.assertTrue(exception.getMessage().contains("Ya existe un usuario con ese nombre registrado"));
        
    }
    
}
