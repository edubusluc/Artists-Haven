package com.artists_heaven.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.data.domain.PageRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpHeaders;

import com.artists_heaven.email.EmailSenderService;
import com.artists_heaven.email.EmailType;
import com.artists_heaven.entities.artist.Artist;
import com.artists_heaven.entities.artist.ArtistRepository;
import com.artists_heaven.entities.user.UserProfileDTO;
import com.artists_heaven.order.Order;
import com.artists_heaven.order.OrderDetailsDTO;
import com.artists_heaven.order.OrderService;
import com.artists_heaven.order.OrderStatus;
import com.artists_heaven.page.PageResponse;
import com.artists_heaven.verification.Verification;
import com.artists_heaven.verification.VerificationRepository;
import com.artists_heaven.verification.VerificationStatus;

import jakarta.persistence.EntityNotFoundException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class AdminControllerTest {

        @Mock
        private Resource resource;

        @Mock
        private ArtistRepository artistRepository;

        @Mock
        private OrderService orderService;

        @Mock
        private EmailSenderService emailSenderService;

        @Mock
        private AdminService adminService;

        @Mock
        private VerificationRepository verificationRepository;

        @InjectMocks
        private AdminController adminController;

        private MockMvc mockMvc;

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);
                mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
        }

        @Test
        void testValidateArtistSuccess() {
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
                assertEquals("Artist verified successfully",
                                ((Map<String, String>) response.getBody()).get("message"));

                assertEquals(true, artist.getIsvalid());
                assertEquals(VerificationStatus.ACCEPTED, verification.getStatus());

                verify(artistRepository).save(artist);
                verify(verificationRepository).save(verification);
        }

        @Test
        void testValidateArtistNullArtist() {
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
                assertEquals("Artist not found", exception.getReason());
        }

        @Test
        void testValidateArtistNullVerification() {
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
                assertEquals("Verification not found", exception.getReason());
        }

        @Test
        void testGetAllValidation() throws Exception {
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
                                .andExpect(content().json(
                                                "[{\"id\":1,\"status\":\"PENDING\"},{\"id\":2,\"status\":\"PENDING\"}]"));

                ResponseEntity<?> response = adminController.getAllValidation();
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertEquals(verificationList, response.getBody());
        }

        @Test
        void testGetVerificationVideoSuccess() throws Exception {
                String fileName = "sample.mp4";
                String basePath = System.getProperty("user.dir")
                                + "/artists-heaven-backend/src/main/resources/verification_media/";
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
        void testGetVerificationVideoNotFound() throws Exception {
                // Arrange
                String fileName = "non-existent-video.mp4";
                when(resource.exists()).thenReturn(false);

                // Act & Assert
                mockMvc.perform(get("/api/admin/verification_media/{fileName}", fileName))
                                .andExpect(status().isNotFound());
        }

        @Test
        void testGetStaticsPerYear_Success() throws Exception {
                int year = 2024;

                when(orderService.getNumOrdersPerYear(year)).thenReturn(100);
                when(orderService.getIncomePerYear(year)).thenReturn(50000.0);
                when(emailSenderService.getEmailCounts(year)).thenReturn(Map.of(EmailType.ABUSE_REPORT, 20));
                when(adminService.countUsers()).thenReturn(80);
                when(adminService.countArtists()).thenReturn(10);
                when(adminService.getOrderStatusCounts(year)).thenReturn(Map.of(OrderStatus.DELIVERED, 60));
                when(adminService.getVerificationStatusCount(year)).thenReturn(Map.of(VerificationStatus.ACCEPTED, 5));
                when(adminService.getMostSoldItems(year)).thenReturn(Map.of("Product Test", 5));
                when(adminService.getMostCategory(year)).thenReturn(Map.of("Category Test", 5));
                when(adminService.getCountrySold(year)).thenReturn(Map.of("Country Test", 5));

                mockMvc.perform(get("/api/admin/staticsPerYear")
                                .param("year", String.valueOf(year)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.numOrders").value(100))
                                .andExpect(jsonPath("$.incomePerYear").value(50000.0))
                                .andExpect(jsonPath("$.emailCounts.ABUSE_REPORT").value(20))
                                .andExpect(jsonPath("$.numUsers").value(80))
                                .andExpect(jsonPath("$.numArtists").value(10))
                                .andExpect(jsonPath("$.orderStatusCounts.DELIVERED").value(60))
                                .andExpect(jsonPath("$.verificationStatusCounts.ACCEPTED").value(5))
                                .andExpect(jsonPath("$.orderItemCount['Product Test']").value(5))
                                .andExpect(jsonPath("$.categoryItemCount['Category Test']").value(5))
                                .andExpect(jsonPath("$.mostCountrySold['Country Test']").value(5));
        }

        @Test
        void testGetStaticsPerYear_Exception() throws Exception {
                when(orderService.getNumOrdersPerYear(Mockito.anyInt()))
                                .thenThrow(new RuntimeException("Error"));

                mockMvc.perform(get("/api/admin/staticsPerYear").param("year", "2024"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void testGetMonthlySalesData_Success() throws Exception {
                int year = 2024;

                List<MonthlySalesDTO> mockData = List.of(
                                new MonthlySalesDTO(1, 10L, 1000.0),
                                new MonthlySalesDTO(2, 15L, 2000.0));

                when(adminService.getMonthlySalesData(year)).thenReturn(mockData);

                mockMvc.perform(get("/api/admin/sales/monthly").param("year", String.valueOf(year)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].month").value(1))
                                .andExpect(jsonPath("$[0].totalOrders").value(10))
                                .andExpect(jsonPath("$[0].totalRevenue").value(1000.0))
                                .andExpect(jsonPath("$[1].month").value(2))
                                .andExpect(jsonPath("$[1].totalOrders").value(15))
                                .andExpect(jsonPath("$[1].totalRevenue").value(2000.0));
        }

        @Test
        void testGetMonthlySalesData_Exception() throws Exception {
                when(adminService.getMonthlySalesData(Mockito.anyInt()))
                                .thenThrow(new RuntimeException("DB Error"));

                mockMvc.perform(get("/api/admin/sales/monthly").param("year", "2024"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void testProductManagement() throws Exception {
                when(adminService.getNotAvailableProducts()).thenReturn(100);
                when(adminService.getAvailableProducts()).thenReturn(100);
                when(adminService.getPromotedProducts()).thenReturn(100);
                when(adminService.getTotalProducts()).thenReturn(100);

                mockMvc.perform(get("/api/admin/product-management"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.notAvailableProducts").value(100))
                                .andExpect(jsonPath("$.availableProducts").value(100))
                                .andExpect(jsonPath("$.promotedProducts").value(100))
                                .andExpect(jsonPath("$.totalProducts").value(100));

        }

        @Test
        void testGetUsers() {
                // Arrange
                int page = 0;
                int size = 6;
                String search = "john";
                PageRequest pageable = PageRequest.of(page, size);

                UserProfileDTO dto = new UserProfileDTO();
                dto.setId(1L);
                dto.setFirstName("John Doe");
                dto.setRole("USER");

                Page<UserProfileDTO> userPage = new PageImpl<>(List.of(dto), pageable, 1);

                when(adminService.getAllUsers(search, pageable)).thenReturn(userPage);

                // Act
                PageResponse<UserProfileDTO> response = adminController.getUsers(page, size, search);

                // Assert
                assertEquals(1, response.getTotalElements());
                assertEquals(1, response.getContent().size());
                assertEquals("John Doe", response.getContent().get(0).getFirstName());
        }

        @Test
        void testGetOrders() {

                int page = 0;
                int size = 6;
                PageRequest pageable = PageRequest.of(page, size);

                Order order = new Order();
                order.setId(1L);
                order.setStatus(OrderStatus.PAID);

                Page<Order> orderPage = new PageImpl<>(List.of(order), pageable, 1);

                when(adminService.getAllOrderSortByDate(pageable)).thenReturn(orderPage);

                // Act
                PageResponse<OrderDetailsDTO> response = adminController.getOrders(page, size);

                // Asserts
                assertEquals(1, response.getTotalElements());
                assertEquals(1, response.getContent().size());

        }

        @Test
        void testUpdateOrderStatus_Success() throws Exception {
                // Datos de prueba
                Long orderId = 1L;
                OrderStatus orderStatus = OrderStatus.PAID;
                OrderStatusUpdateDTO request = new OrderStatusUpdateDTO();
                request.setOrderId(orderId);
                request.setStatus(orderStatus);

                // Simulamos que el servicio actualiza correctamente el estado de la orden
                doNothing().when(adminService).updateOrderStatus(orderId, orderStatus);

                // Hacemos la petición POST
                mockMvc.perform(post("/api/admin/updateStatus")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"orderId\": 1, \"status\": \"PAID\"}"))
                                .andExpect(status().isOk())
                                .andExpect(content().string("Order status updated successfully."));
        }

        @Test
        void testUpdateOrderStatus_OrderNotFound() throws Exception {
                // Datos de prueba
                Long orderId = 1L;
                OrderStatus orderStatus = OrderStatus.PAID;
                OrderStatusUpdateDTO request = new OrderStatusUpdateDTO();
                request.setOrderId(orderId);
                request.setStatus(orderStatus);

                // Simulamos que el servicio lanza una EntityNotFoundException
                doThrow(new EntityNotFoundException("Order not found with id: " + orderId))
                                .when(adminService).updateOrderStatus(orderId, orderStatus);

                // Hacemos la petición POST
                mockMvc.perform(post("/api/admin/updateStatus")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"orderId\": 1, \"status\": \"PAID\"}"))
                                .andExpect(status().isNotFound()) // Verificamos que la respuesta es 404 NOT FOUND
                                .andExpect(content().string("Order not found with id: " + orderId)); // Verificamos el
                                                                                                     // mensaje de error
        }

        @Test
        void testUpdateOrderStatus_InternalServerError() throws Exception {
                // Datos de prueba
                Long orderId = 1L;
                OrderStatus orderStatus = OrderStatus.PAID;
                OrderStatusUpdateDTO request = new OrderStatusUpdateDTO();
                request.setOrderId(orderId);
                request.setStatus(orderStatus);

                // Simulamos una excepción genérica
                doThrow(new RuntimeException("Unexpected error")).when(adminService).updateOrderStatus(orderId,
                                orderStatus);

                // Hacemos la petición POST
                mockMvc.perform(post("/api/admin/updateStatus")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"orderId\": 1, \"status\": \"PAID\"}"))
                                .andExpect(status().isInternalServerError()) // Verificamos que la respuesta es 500
                                                                             // INTERNAL SERVER ERROR
                                .andExpect(content().string("An error occurred while updating the order status.")); // Verificamos
                                                                                                                    // el
                                                                                                                    // mensaje
                                                                                                                    // de
                                                                                                                    // error
        }
}
