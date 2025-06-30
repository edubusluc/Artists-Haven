package com.artists_heaven.entities.artist;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.artists_heaven.admin.MonthlySalesDTO;
import com.artists_heaven.entities.user.UserRepository;
import com.artists_heaven.images.ImageServingUtil;

class ArtistControllerTest {

        private MockMvc mockMvc;

        @Mock
        private ArtistService artistService;

        @Mock
        private UserRepository userRepository;

        @Mock
        private ImageServingUtil imageServingUtil;

        @InjectMocks
        private ArtistController artistController;

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);

                mockMvc = MockMvcBuilders.standaloneSetup(artistController).build();
        }

        @Test
        void testRegisterArtist() throws Exception {
                // Crear un objeto DTO simulado
                ArtistRegisterDTO artistRegisterDTO = new ArtistRegisterDTO();
                artistRegisterDTO.setFirstName("John");
                artistRegisterDTO.setLastName("Doe");
                artistRegisterDTO.setEmail("johndoe@example.com");
                artistRegisterDTO.setPassword("password123");
                artistRegisterDTO.setArtistName("John's Art");
                artistRegisterDTO.setUrl("http://example.com");

                // Simular imagen enviada como archivo
                MockMultipartFile image = new MockMultipartFile("image", "photo.jpg", "image/jpeg",
                                "fake image content".getBytes());

                // Configurar mocks
                Artist artist = new Artist();
                artist.setMainViewPhoto("mianViewPhoto.jpg");

                when(imageServingUtil.saveImages(image, "artists-heaven-backend/src/main/resources/mainArtist_media/",
                                "/mainArtist_media/", false))
                                .thenReturn("mianViewPhoto.jpg");
                when(artistService.registerArtist(any(), any())).thenReturn(artist);

                // Ejecutar solicitud como multipart
                mockMvc.perform(multipart("/api/artists/register")
                                .file(image)
                                .param("firstName", artistRegisterDTO.getFirstName())
                                .param("lastName", artistRegisterDTO.getLastName())
                                .param("email", artistRegisterDTO.getEmail())
                                .param("password", artistRegisterDTO.getPassword())
                                .param("artistName", artistRegisterDTO.getArtistName())
                                .param("url", artistRegisterDTO.getUrl()))
                                .andExpect(status().isCreated());
        }

        @Test
        void testRegisterArtistSuccessBadRequest() throws Exception {
                // Simular archivo enviado
                MockMultipartFile image = new MockMultipartFile("image", "photo.jpg", "image/jpeg", "img".getBytes());

                // Simular que el servicio lanza excepción por datos inválidos
                when(imageServingUtil.saveImages(image, "artists-heaven-backend/src/main/resources/mainArtist_media/",
                                "/mainArtist_media/", false))
                                .thenReturn("photo.jpg");
                when(artistService.registerArtist(any(), any()))
                                .thenThrow(new IllegalArgumentException("Invalid user data"));

                mockMvc.perform(multipart("/api/artists/register")
                                .file(image)
                                .param("email", "invalid-email@example.com")
                                .param("artistName", "Invalid Artist")
                                .param("firstName", "Invalid")
                                .param("lastName", "Artist")
                                .param("url", "invalid-artist.com")
                                .param("password", "password"))
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
                when(artistService.isArtistVerificated(1L))
                                .thenThrow(new RuntimeException("Error fetching verification"));

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

        @Test
        void testGetArtistMainView_Success() throws Exception {
                // Crear artista simulado
                Artist artist = new Artist();
                artist.setId(1L);
                artist.setArtistName("Test Artist");

                List<Artist> mockArtists = List.of(artist);
                when(artistService.getValidArtists()).thenReturn(mockArtists);

                mockMvc.perform(get("/api/artists/main"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].name").value("Test Artist"));
        }

        @Test
        void testGetArtistMainView_Failure() throws Exception {
                when(artistService.getValidArtists()).thenThrow(new RuntimeException("Something went wrong"));

                mockMvc.perform(get("/api/artists/main"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void testGetProductImage_Success() throws Exception {
                String testFileName = "test-image.png";

                // Crear archivo temporal simulado en la ruta real
                String basePath = System.getProperty("user.dir")
                                + "/artists-heaven-backend/src/main/resources/mainArtist_media/";
                Files.createDirectories(Paths.get(basePath));

                Path imagePath = Paths.get(basePath + testFileName);
                Files.write(imagePath, new byte[] { (byte) 137, 80, 78, 71 });

                mockMvc.perform(get("/api/artists/mainArtist_media/" + testFileName))
                                .andExpect(status().isOk());

                // Limpieza
                Files.deleteIfExists(imagePath);
        }

        @AfterEach
        void tearDown() {
                SecurityContextHolder.clearContext();
        }

}
