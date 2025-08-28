package com.artists_heaven.entities.artist;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
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
import com.artists_heaven.event.Event;
import com.artists_heaven.event.EventService;
import com.artists_heaven.images.ImageServingUtil;
import com.artists_heaven.product.Product;
import com.artists_heaven.product.ProductService;

class ArtistControllerTest {

        private MockMvc mockMvc;

        @Mock
        private ArtistService artistService;

        @Mock
        private UserRepository userRepository;

        @Mock
        private ProductService productService;

        @Mock
        private EventService eventService;

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
        void testGetArtistDashboard_success() throws Exception {
                // Simulamos el artista autenticado
                Artist artist = new Artist();
                artist.setId(1L);

                Authentication authentication = mock(Authentication.class);
                when(authentication.getPrincipal()).thenReturn(artist);
                SecurityContext securityContext = mock(SecurityContext.class);
                when(securityContext.getAuthentication()).thenReturn(authentication);
                SecurityContextHolder.setContext(securityContext);

                // Simulamos el DTO que devuelve artistService.getArtistDashboard
                ArtistDashboardDTO dashboardDTO = new ArtistDashboardDTO();
                dashboardDTO.setIsVerificated("Verified");
                dashboardDTO.setFutureEvents(1);
                dashboardDTO.setPastEvents(1);
                dashboardDTO.setOrderItemCount(Map.of("Item1", 5));
                dashboardDTO.setMostCountrySold(Map.of("USA", 10));

                // Mockeamos el servicio
                when(artistService.getArtistDashboard(1L, 2023)).thenReturn(dashboardDTO);

                // Llamada al endpoint y verificaciones
                mockMvc.perform(get("/api/artists/dashboard")
                                .param("year", "2023")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.isVerificated").value("Verified"))
                                .andExpect(jsonPath("$.data.futureEvents").value(1))
                                .andExpect(jsonPath("$.data.pastEvents").value(1))
                                .andExpect(jsonPath("$.data.orderItemCount.Item1").value(5))
                                .andExpect(jsonPath("$.data.mostCountrySold.USA").value(10))
                                .andExpect(jsonPath("$.message").value("Dashboard retrieved successfully"))
                                .andExpect(jsonPath("$.status").value(200));
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
                                .andExpect(jsonPath("$.data[0].month").value(1))
                                .andExpect(jsonPath("$.data[0].totalOrders").value(100))
                                .andExpect(jsonPath("$.data[1].month").value(2))
                                .andExpect(jsonPath("$.data[1].totalOrders").value(200));
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
                                .andDo(print())
                                .andExpect(jsonPath("$.data[0].name").value("Test Artist"));
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

        @Test
        void testGetArtistById_success() throws Exception {
                // Preparar datos simulados
                Long artistId = 1L;
                String artistName = "Duki";

                Artist artist = new Artist();
                artist.setId(artistId);
                artist.setArtistName(artistName);
                artist.setMainColor("#FF0000");
                artist.setBannerPhoto("banner.jpg");

                Product product1 = new Product();
                product1.setName("Camiseta Tour");
                product1.setPrice(20.0f);

                Product product2 = new Product();
                product2.setName("Gorra");
                product2.setPrice(15.0f);

                List<Product> mockProducts = List.of(product1, product2);

                Event event1 = new Event();
                event1.setName("Tour 2025");
                event1.setDate(LocalDate.of(2025, 10, 10));
                Event event2 = new Event();
                event2.setName("Festival");
                event2.setDate(LocalDate.of(2025, 7, 1));

                List<Event> mockEvents = List.of(event1, event2);

                ArtistDTO dto = new ArtistDTO();
                dto.setArtistName(artistName);
                dto.setArtistProducts(mockProducts);
                dto.setArtistEvents(mockEvents);
                dto.setBannerPhoto(artist.getBannerPhoto());
                dto.setPrimaryColor(artist.getMainColor());

                when(artistService.getArtistWithDetails(artistId)).thenReturn(dto);


                // Llamar a la API y verificar la respuesta
                mockMvc.perform(get("/api/artists/{artistId}", artistId)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andDo(print())
                                .andExpect(jsonPath("$.data.artistName").value("Duki"))
                                .andExpect(jsonPath("$.data.artistProducts[0].name").value("Camiseta Tour"))
                                .andExpect(jsonPath("$.data.artistProducts[1].name").value("Gorra"))
                                .andExpect(jsonPath("$.data.artistEvents[0].name").value("Tour 2025"))
                                .andExpect(jsonPath("$.data.artistEvents[1].name").value("Festival"))
                                .andExpect(jsonPath("$.data.primaryColor").value("#FF0000"))
                                .andExpect(jsonPath("$.data.bannerPhoto").value("banner.jpg"));
        }

        @AfterEach
        void tearDown() {
                SecurityContextHolder.clearContext();
        }

}
