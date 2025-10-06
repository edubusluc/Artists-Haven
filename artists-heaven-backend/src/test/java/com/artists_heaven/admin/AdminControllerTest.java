package com.artists_heaven.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
import com.artists_heaven.email.EmailSenderService;
import com.artists_heaven.email.EmailType;
import com.artists_heaven.entities.artist.ArtistRepository;
import com.artists_heaven.entities.artist.ArtistService;
import com.artists_heaven.entities.user.User;
import com.artists_heaven.entities.user.UserProfileDTO;
import com.artists_heaven.exception.AppExceptions;
import com.artists_heaven.images.ImageServingUtil;
import com.artists_heaven.order.Order;
import com.artists_heaven.order.OrderDetailsDTO;
import com.artists_heaven.order.OrderService;
import com.artists_heaven.order.OrderStatus;
import com.artists_heaven.page.PageResponse;
import com.artists_heaven.product.CategoryRepository;
import com.artists_heaven.product.Product;
import com.artists_heaven.product.ProductService;
import com.artists_heaven.standardResponse.StandardResponse;
import com.artists_heaven.userProduct.UserProduct;
import com.artists_heaven.userProduct.UserProductService;
import com.artists_heaven.verification.Verification;
import com.artists_heaven.verification.VerificationRepository;
import com.artists_heaven.verification.VerificationService;
import com.artists_heaven.verification.VerificationStatus;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.hamcrest.Matchers.hasSize;

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

        @Mock
        private CategoryRepository categoryRepository;

        @Mock
        private VerificationService verificationService;

        @Mock
        private ProductService productService;

        @Mock
        private ArtistService artistService;

        @Mock
        private UserProductService userProductService;

        @Mock
        private ImageServingUtil imageServingUtil;

        @InjectMocks
        private AdminController adminController;

        private MockMvc mockMvc;

        private ObjectMapper objectMapper = new ObjectMapper();

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);
                mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
        }

        @Test
        void testValidateArtistNullArtist() {
                // Arrange
                Map<String, Long> payload = new HashMap<>();
                payload.put("id", 1L);
                payload.put("verificationId", 1L);

                // Simulamos que el servicio lanza la excepción
                Mockito.doThrow(new AppExceptions.ResourceNotFoundException("Artist not found"))
                                .when(artistService).validateArtist(1L, 1L);

                // Act & Assert
                AppExceptions.ResourceNotFoundException exception = assertThrows(
                                AppExceptions.ResourceNotFoundException.class,
                                () -> adminController.validateArtist(payload));

                assertEquals("Artist not found", exception.getMessage());
        }

        @Test
        void testValidateArtistNullVerification() {
                // Arrange
                Map<String, Long> payload = new HashMap<>();
                payload.put("id", 1L);
                payload.put("verificationId", 10L);

                // Simulamos que el servicio lanza la excepción
                Mockito.doThrow(new AppExceptions.ResourceNotFoundException("Verification not found"))
                                .when(artistService).validateArtist(1L, 10L);

                // Act & Assert
                AppExceptions.ResourceNotFoundException exception = assertThrows(
                                AppExceptions.ResourceNotFoundException.class,
                                () -> adminController.validateArtist(payload));

                assertEquals("Verification not found", exception.getMessage());
        }

        @Test
        void testGetAllValidation() throws Exception {
                // Arrange
                Verification verification1 = new Verification();
                verification1.setId(1L);
                verification1.setStatus(VerificationStatus.PENDING);
                verification1.setDate(LocalDateTime.now());

                Verification verification2 = new Verification();
                verification2.setId(2L);
                verification2.setStatus(VerificationStatus.PENDING);
                verification2.setDate(LocalDateTime.now());

                List<Verification> verificationList = Arrays.asList(verification1, verification2);

                when(verificationRepository.findAll(Sort.by(Sort.Direction.DESC, "date"))).thenReturn(verificationList);

                // Act & Assert
                mockMvc.perform(get("/api/admin/verification/pending"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Pending verifications retrieved"))
                                .andExpect(jsonPath("$.status").value(200))
                                .andExpect(jsonPath("$.data[0].id").value(1))
                                .andExpect(jsonPath("$.data[0].status").value("PENDING"))
                                .andExpect(jsonPath("$.data[1].id").value(2))
                                .andExpect(jsonPath("$.data[1].status").value("PENDING"));

                ResponseEntity<StandardResponse<List<Verification>>> response = adminController
                                .getAllPendingVerifications();

                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertEquals("Pending verifications retrieved", response.getBody().getMessage());
                assertEquals(200, response.getBody().getStatus());
                assertEquals(verificationList, response.getBody().getData());
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
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Yearly statistics retrieved successfully"))
                                .andExpect(jsonPath("$.status").value(200))
                                .andExpect(jsonPath("$.data.numOrders").value(100))
                                .andExpect(jsonPath("$.data.incomePerYear").value(50000.0))
                                .andExpect(jsonPath("$.data.emailCounts.ABUSE_REPORT").value(20))
                                .andExpect(jsonPath("$.data.numUsers").value(80))
                                .andExpect(jsonPath("$.data.numArtists").value(10))
                                .andExpect(jsonPath("$.data.orderStatusCounts.DELIVERED").value(60))
                                .andExpect(jsonPath("$.data.verificationStatusCounts.ACCEPTED").value(5))
                                .andExpect(jsonPath("$.data.orderItemCount['Product Test']").value(5))
                                .andExpect(jsonPath("$.data.categoryItemCount['Category Test']").value(5))
                                .andExpect(jsonPath("$.data.mostCountrySold['Country Test']").value(5));
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
                                .andExpect(jsonPath("$.data[0].month").value(1))
                                .andExpect(jsonPath("$.data[0].totalOrders").value(10))
                                .andExpect(jsonPath("$.data[0].totalRevenue").value(1000.0))
                                .andExpect(jsonPath("$.data[1].month").value(2))
                                .andExpect(jsonPath("$.data[1].totalOrders").value(15))
                                .andExpect(jsonPath("$.data[1].totalRevenue").value(2000.0));
        }

        @Test
        void testProductManagement() throws Exception {
                when(adminService.getNotAvailableProducts()).thenReturn(100);
                when(adminService.getAvailableProducts()).thenReturn(100);
                when(adminService.getPromotedProducts()).thenReturn(100);
                when(adminService.getTotalProducts()).thenReturn(100);

                mockMvc.perform(get("/api/admin/product-management"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.notAvailableProducts").value(100))
                                .andExpect(jsonPath("$.data.availableProducts").value(100))
                                .andExpect(jsonPath("$.data.promotedProducts").value(100))
                                .andExpect(jsonPath("$.data.totalProducts").value(100));

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
                order.setIdentifier(10L);

                Page<Order> orderPage = new PageImpl<>(List.of(order), pageable, 1);

                when(adminService.getOrdersFiltered(null, null, pageable)).thenReturn(orderPage);

                // Act
                PageResponse<OrderDetailsDTO> response = adminController.getOrders(page, size, null, null);

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
                                .andExpect(jsonPath("$.message").value("Order status updated successfully"));
        }

        @Test
        void refuseVerification_shouldReturnOk_whenVerificationExists() throws Exception {
                Long verificationId = 1L;

                mockMvc.perform(post("/api/admin/" + verificationId + "/refuse"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Verification rejected successfully"));
        }

        @Test
        void disableProduct_ReturnsOk_WhenSuccessful() throws Exception {
                Long productId = 1L;

                // When & Then
                mockMvc.perform(post("/api/admin/{productId}/disable", productId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Product disabled successfully"));

                verify(productService).disableProduct(productId);
        }

        @Test
        void enableProduct_ReturnsOk_WhenSuccessful() throws Exception {
                Long productId = 1L;

                Product product = new Product();
                product.setId(productId);
                product.setName("Test Product");
                product.setAvailable(false);

                // When & Then
                mockMvc.perform(post("/api/admin/{productId}/enable", productId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Product enabled successfully"));

                verify(productService).enableProduct(productId);
        }

        @Test
        void testCreateCategory_Success() throws Exception {
                CategoryDTO categoryDTO = new CategoryDTO(null, " New Category ");

                mockMvc.perform(post("/api/admin/newCategory")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(categoryDTO)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Category created successfully"));
        }

        @Test
        void testEditCategory_Success() throws Exception {
                CategoryDTO categoryDTO = new CategoryDTO(1L, " Edited Category ");

                doNothing().when(productService).editCategory(any(CategoryDTO.class));

                mockMvc.perform(post("/api/admin/editCategory")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(categoryDTO)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Category edited successfully"));
        }

        @Test
        void createCollection_success() throws Exception {
                CollectionDTO dto = new CollectionDTO(null, "New Collection", false);

                // Simular que el método void no hace nada
                doNothing().when(productService).saveCollection("New-Collection");

                mockMvc.perform(post("/api/admin/newCollection")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Collection created successfully"));

                verify(productService, times(1)).saveCollection("New-Collection");
        }

        @Test
        void editCollection_success() throws Exception {
                CollectionDTO dto = new CollectionDTO(1L, "Edited Collection", true);

                doNothing().when(productService).editCollection(dto);

                mockMvc.perform(post("/api/admin/editCollection")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Collection edited successfully"));
        }

        @Test
        void getAllUserProductPending_success() throws Exception {
                User user = new User();
                user.setUsername("Test");

                UserProduct product = new UserProduct();
                product.setId(1L);
                product.setName("Test Product");
                product.setOwner(user);

                when(userProductService.findUserProductPending()).thenReturn(List.of(product));

                mockMvc.perform(get("/api/admin/userProduct/pending"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("userProducts retrieved successfully"))
                                .andExpect(jsonPath("$.data", hasSize(1)))
                                .andExpect(jsonPath("$.data[0].id").value(1))
                                .andExpect(jsonPath("$.data[0].name").value("Test Product"));
        }

        @Test
        void approveProduct_success() throws Exception {
                User user = new User();
                user.setUsername("Test");
                UserProduct product = new UserProduct();

                product.setId(1L);
                product.setName("Approved Product");
                product.setOwner(user);

                when(userProductService.approveProduct(1L)).thenReturn(product);

                mockMvc.perform(post("/api/admin/userProduct/1/approve"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("OK APPROVE"))
                                .andExpect(jsonPath("$.data.id").value(1))
                                .andExpect(jsonPath("$.data.name").value("Approved Product"));
        }

        @Test
        void rejectProduct_success() throws Exception {
                User user = new User();
                user.setUsername("Test");

                UserProduct product = new UserProduct();
                product.setId(2L);
                product.setName("Rejected Product");
                product.setOwner(user);

                when(userProductService.rejectProduct(2L)).thenReturn(product);

                mockMvc.perform(post("/api/admin/userProduct/2/reject"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("OK REJECT"))
                                .andExpect(jsonPath("$.data.id").value(2))
                                .andExpect(jsonPath("$.data.name").value("Rejected Product"));
        }

}
