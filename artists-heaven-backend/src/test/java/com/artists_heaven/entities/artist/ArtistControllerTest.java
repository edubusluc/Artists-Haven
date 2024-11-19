package com.artists_heaven.entities.artist;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;


public class ArtistControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ArtistService artistService;

    @InjectMocks
    private ArtistController artistController;

    private ObjectMapper objectMapper = new ObjectMapper(); // Para convertir objetos a JSON

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        mockMvc = MockMvcBuilders.standaloneSetup(artistController).build();
    }

    @Test
    public void testRegisterArtistSuccess() throws Exception {
        // Crear el artista con datos válidos
        Artist artist = new Artist();
        artist.setEmail("newartist@example.com");
        artist.setArtistName("New Test Artist");
        artist.setFirstName("New");
        artist.setLastName("Artist");
        artist.setUrl("https://www.new-artist-pages.com");
        artist.setPassword("newpassword");

        // Simular la respuesta del servicio con el artista registrado
        when(artistService.registerArtist(any(Artist.class))).thenReturn(artist);

        // Realizar la solicitud POST y verificar el resultado
        mockMvc.perform(post("/api/artists/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(artist)))  
                .andExpect(status().isCreated());  
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
        when(artistService.registerArtist(any(Artist.class))).thenThrow(new IllegalArgumentException("Invalid user data"));
        
        mockMvc.perform(post("/api/artists/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(artist)))  
                .andExpect(status().isBadRequest());
    }   

}
