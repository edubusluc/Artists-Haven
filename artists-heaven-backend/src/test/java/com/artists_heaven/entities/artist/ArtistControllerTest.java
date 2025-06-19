package com.artists_heaven.entities.artist;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.artists_heaven.admin.MonthlySalesDTO;
import com.artists_heaven.entities.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

class ArtistControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ArtistService artistService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ArtistController artistController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockMvc = MockMvcBuilders.standaloneSetup(artistController).build();
    }

    @Test
    void testRegisterArtist() throws Exception {
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
    void testRegisterArtistSuccessBadRequest() throws Exception {
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

    @Test
    void testGetArtistDashboard_success() throws Exception {
        // Simulamos los servicios
        Artist artist = new Artist();
        artist.setId(1L);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(artist);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(artistService.isArtistVerificated(1L)).thenReturn("Verified");
        when(artistService.getFutureEvents(1L, 2023)).thenReturn(1);
        when(artistService.getPastEvents(1L, 2023)).thenReturn(1);
        when(artistService.getOrderItemCount(1L, 2023)).thenReturn(Map.of("Item1", 5));
        when(artistService.getMostCountrySold(1L, 2023)).thenReturn(Map.of("USA", 10));

        // Llamamos a la API y verificamos que la respuesta sea 200 OK y que el
        // contenido sea el esperado
        mockMvc.perform(get("/api/artists/dashboard")
                .param("year", "2023")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isVerificated").value("Verified"))
                .andExpect(jsonPath("$.futureEvents").value(1))
                .andExpect(jsonPath("$.pastEvents").value(1))
                .andExpect(jsonPath("$.orderItemCount['Item1']").value(5))
                .andExpect(jsonPath("$.mostCountrySold['USA']").value(10));
    }

    @Test
    void testGetArtistDashboard_errorHandling() throws Exception {
        // Simulamos los servicios, pero esta vez lanzamos una excepción en uno de ellos
        when(artistService.isArtistVerificated(1L)).thenThrow(new RuntimeException("Error fetching verification"));

        mockMvc.perform(get("/api/artists/dashboard")
                .param("year", "2023")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetMonthlySalesData_success() throws Exception {
        // Simulamos la autenticación del usuario
        Authentication authentication = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Artist artist = new Artist();
        artist.setId(1L);
        when(authentication.getPrincipal()).thenReturn(artist);

        // Simulamos las respuestas del servicio
        MonthlySalesDTO dto1 = new MonthlySalesDTO(); // Enero, 100 ventas
        dto1.setMonth(1);
        dto1.setTotalOrders(100L);
        MonthlySalesDTO dto2 = new MonthlySalesDTO(); // Enero, 100 ventas
        dto2.setMonth(2);
        dto2.setTotalOrders(200L);
        List<MonthlySalesDTO> monthlySalesData = Arrays.asList(dto1, dto2);

        when(artistService.getMonthlySalesDataPerArtist(1L, 2024)).thenReturn(monthlySalesData);

        // Llamamos a la API y verificamos que la respuesta sea 200 OK
        mockMvc.perform(get("/api/artists/sales/monthly")
                .param("year", "2024")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].month").value(1))
                .andExpect(jsonPath("$[0].totalOrders").value(100))
                .andExpect(jsonPath("$[1].month").value(2))
                .andExpect(jsonPath("$[1].totalOrders").value(200));
    }

    @Test
    void testGetMonthlySalesData_errorHandling() throws Exception {
        // Simulamos la autenticación del usuario
        Authentication authentication = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Artist artist = new Artist();
        artist.setId(1L);
        when(authentication.getPrincipal()).thenReturn(artist);

        // Simulamos que el servicio lance una excepción
        when(artistService.getMonthlySalesDataPerArtist(1L, 2024))
                .thenThrow(new RuntimeException("Error fetching sales data"));

        // Llamamos a la API y verificamos que la respuesta sea 400 Bad Request
        mockMvc.perform(get("/api/artists/sales/monthly")
                .param("year", "2024")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

}
