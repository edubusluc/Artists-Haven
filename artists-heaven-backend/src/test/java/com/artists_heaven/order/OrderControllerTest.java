package com.artists_heaven.order;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.exception.AppExceptions;
import com.artists_heaven.exception.GlobalExceptionHandler;
import com.artists_heaven.product.Product;
import com.artists_heaven.product.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.data.domain.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OrderControllerTest {

        private MockMvc mockMvc;

        @Mock
        private OrderService orderService;

        @Mock
        private ProductService productService;

        @InjectMocks
        private OrderController orderController;

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);
                mockMvc = MockMvcBuilders
                                .standaloneSetup(orderController)
                                .setControllerAdvice(new GlobalExceptionHandler())
                                .build();
        }

        // --- getMyOrders ---

        // --- getOrder ---
        @Nested
        class GetOrderTests {

                @Test
                @DisplayName("✅ should return order by ID successfully")
                void shouldReturnOrder() throws Exception {
                        OrderDetailsDTO dto = new OrderDetailsDTO();
                        when(orderService.getOrderDetailsById(10L)).thenReturn(dto);

                        mockMvc.perform(get("/api/orders/10"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.message").value("Order retrieved successfully"));
                }

                @Test
                @DisplayName("❌ should return 404 if order not found")
                void shouldReturnNotFound() throws Exception {
                        when(orderService.getOrderDetailsById(10L))
                                        .thenThrow(new AppExceptions.ResourceNotFoundException("Order not found"));

                        mockMvc.perform(get("/api/orders/10"))
                                        .andExpect(status().isNotFound())
                                        .andExpect(jsonPath("$.message").value("Order not found"));
                }

                @Test
                @DisplayName("❌ should return 403 if access forbidden")
                void shouldReturnForbidden() throws Exception {
                        when(orderService.getOrderDetailsById(10L))
                                        .thenThrow(new AppExceptions.ForbiddenActionException("Forbidden"));

                        mockMvc.perform(get("/api/orders/10"))
                                        .andExpect(status().isForbidden())
                                        .andExpect(jsonPath("$.message").value("Forbidden"));
                }

        }

        // --- getOrderByIdentifier ---
        @Nested
        class GetOrderByIdentifierTests {

                @Test
                @DisplayName("✅ should return order by identifier successfully")
                void shouldReturnOrderByIdentifier() throws Exception {

                        OrderItem orderItem = new OrderItem();
                        orderItem.setId(1L);
                        orderItem.setQuantity(2);

                        User user = new User();

                        Order order = new Order();
                        order.setId(20L);
                        order.setIdentifier(1L); // importante, tu DTO hace toString()
                        order.setTotalPrice(100.0f);
                        order.setStatus(OrderStatus.IN_PREPARATION);
                        order.setUser(user);
                        order.setItems(List.of(orderItem));
                        order.setAddressLine1("Street 123");
                        order.setCity("City");
                        order.setCountry("Country");
                        order.setPostalCode("12345");
                        order.setEmail("test@example.com");
                        order.setPhone("1234567890");
                        order.setCreatedDate(LocalDateTime.now());
                        order.setPaymentIntent("pi_123");

                        when(orderService.getOrderByIdentifier(eq(123L), eq("en"))).thenReturn(order);

                        Product product = new Product();
                        product.setId(2L);
                        product.setImages(List.of("img.png"));
                        when(productService.findAllByIds(anySet())).thenReturn(List.of(product));

                        mockMvc.perform(get("/api/orders/by-identifier")
                                        .param("identifier", "123")
                                        .param("lang", "en"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.message").value("Order retrieved successfully"))
                                        .andExpect(jsonPath("$.data.orders.id").value(20))
                                        .andExpect(jsonPath("$.data.productImages['2']").value("img.png"));
                }

                @Test
                @DisplayName("❌ should return 404 if order not found by identifier")
                void shouldReturnNotFoundByIdentifier() throws Exception {
                        when(orderService.getOrderByIdentifier(anyLong(), anyString()))
                                        .thenThrow(new AppExceptions.ResourceNotFoundException("Not found"));

                        mockMvc.perform(get("/api/orders/by-identifier")
                                        .param("identifier", "123")
                                        .param("lang", "en"))
                                        .andExpect(status().isNotFound())
                                        .andExpect(jsonPath("$.message").value("Not found"));
                }

        }
}
