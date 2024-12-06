package com.artists_heaven.entities.artist;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.MediaType;

import com.artists_heaven.entities.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ArtistControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ArtistService artistService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ArtistController artistController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        mockMvc = MockMvcBuilders.standaloneSetup(artistController).build();
    }

    @Test
    public void testRegisterArtist() throws Exception {
        // Crear un objeto Artist para enviar como payload
        Artist artist = new Artist();
        artist.setFirstName("John");
        artist.setLastName("Doe");
        artist.setUsername("johndoe");
        artist.setEmail("johndoe@example.com");
        artist.setPassword("password123");
        artist.setArtistName("John's Art");
        artist.setUrl("http://example.com");

        // Configurar el servicio mock para devolver el artista al registrarse
        Mockito.when(artistService.registerArtist(Mockito.any(Artist.class))).thenReturn(artist);

        // Realizar la solicitud POST y verificar la respuesta
        mockMvc.perform(MockMvcRequestBuilders.post("/api/artists/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(artist)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("John"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value("Doe"));
    }


    @Test
    public void testRegisterArtistSuccessBadRequest() throws Exception {
        Artist artist = new Artist();
        artist.setEmail("invalid-email@example.com");
        artist.setArtistName("Invalid Artist");
        artist.setFirstName("Invalid");
        artist.setLastName("Artist");
        artist.setUrl("invalid-artist.com");
        artist.setPassword("password");
        when(artistService.registerArtist(any(Artist.class)))
                .thenThrow(new IllegalArgumentException("Invalid user data"));

        mockMvc.perform(post("/api/artists/register")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(artist)))
                .andExpect(status().isBadRequest());
    }

}
