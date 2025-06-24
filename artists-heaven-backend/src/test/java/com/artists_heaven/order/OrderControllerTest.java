package com.artists_heaven.order;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.entities.user.UserRole;
import com.artists_heaven.product.Product;
import com.artists_heaven.product.ProductService;

import jakarta.persistence.EntityNotFoundException;

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
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
    }

    @Test
    void testGetMyOrders_Success() throws Exception {
        User user = new User();
        user.setId(1L);

        OrderItem item = new OrderItem();
        item.setProductId(123L);

        Order order = new Order();
        order.setId(1L);
        order.setCreatedDate(LocalDateTime.now());
        order.setUser(user);
        order.setItems(List.of(item));

        PageRequest pageable = PageRequest.of(0, 3);
        Page<Order> orderPage = new PageImpl<>(List.of(order), pageable, 1);

        Product product = new Product();
        product.setId(123L);
        product.setImages(List.of("img.jpg"));

        // Mock Auth
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Mock services
        when(orderService.getMyOrdersPageable(eq(1L), any(Pageable.class))).thenReturn(orderPage);
        when(productService.findAllByIds(Set.of(123L))).thenReturn(List.of(product));

        mockMvc.perform(get("/api/orders/myOrders")
                .param("page", "0")
                .param("size", "3")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders[0].id").value(1));
    }

    @Test
    void testGetMyOrders_Unauthorized() throws Exception {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(null);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        mockMvc.perform(get("/api/orders/myOrders")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetOrder_Success_AdminAccess() throws Exception {
        // Datos de prueba

        User userAdmin = new User();
        userAdmin.setRole(UserRole.ADMIN);

        Long orderId = 1L;
        User user = new User();
        user.setId(2L);
        Order order = new Order();
        order.setId(orderId);
        order.setUser(user); // Usuario de la orden

        // Simula que el servicio devuelve la orden
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userAdmin);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(orderService.findOrderById(orderId)).thenReturn(order);

        // Hacemos la solicitud GET
        mockMvc.perform(get("/api/orders/{id}", orderId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Verificamos que el status sea 200 OK
                .andExpect(jsonPath("$.id").value(orderId)); // Verificamos que el ID de la orden está en la respuesta
    }

    @Test
    void testGetOrder_Success_UserAccess() throws Exception {
        // Datos de prueba
        User user = new User();
        user.setId(2L);
        user.setRole(UserRole.USER);

        Long orderId = 1L;
        Order order = new Order();
        order.setId(orderId);
        order.setUser(user); // Usuario de la orden

        // Simula que el servicio devuelve la orden
        when(orderService.findOrderById(orderId)).thenReturn(order);

        // Simulamos que el usuario autenticado es el propietario de la orden
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Hacemos la solicitud GET
        mockMvc.perform(get("/api/orders/{id}", orderId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Verificamos que el status sea 200 OK
                .andExpect(jsonPath("$.id").value(orderId)); // Verificamos que el ID de la orden está en la respuesta
    }

    @Test
    void testGetOrder_Failure_NoAccess() throws Exception {
        // Datos de prueba
        User user = new User();
        user.setId(2L);
        user.setRole(UserRole.USER);

        User user2 = new User();
        user2.setId(3L);
        user2.setRole(UserRole.USER);

        Long orderId = 1L;
        Order order = new Order();
        order.setId(orderId);
        order.setUser(user2); // Usuario de la orden

        // Simula que el servicio devuelve la orden
        when(orderService.findOrderById(orderId)).thenReturn(order);

        // Simulamos que el usuario autenticado es el propietario de la orden
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Hacemos la solicitud GET
        mockMvc.perform(get("/api/orders/{id}", orderId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden()); // Verificamos que el status sea 403
    }

    @Test
    void testGetOrder_Failure_OrderNotFound() throws Exception {
        Long orderId = 1L;

        User userAdmin = new User();
        userAdmin.setRole(UserRole.ADMIN);

        // Simula que el servicio lanza una EntityNotFoundException
        when(orderService.findOrderById(orderId))
                .thenThrow(new EntityNotFoundException("Order not found with id: " +
                        orderId));

        // Simulamos que el usuario autenticado es el propietario de la orden
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userAdmin);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Hacemos la solicitud GET
        mockMvc.perform(get("/api/orders/{id}", orderId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // Verificamos que el status sea 404 NOT

    }
}