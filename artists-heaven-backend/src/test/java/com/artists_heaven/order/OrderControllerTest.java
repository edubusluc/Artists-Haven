package com.artists_heaven.order;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.entities.user.UserRole;

import jakarta.persistence.EntityNotFoundException;

class OrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
    }

    @Test
    void testGetMyOrders_Success() throws Exception {
        Order order = new Order();
        order.setId(1L);
        List<Order> orders = List.of(order);

        User user = new User();
        user.setId(1L);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(orderService.getMyOrders(1L)).thenReturn(orders);

        mockMvc.perform(get("/api/orders/myOrders")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
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
    public void testGetOrder_Success_AdminAccess() throws Exception {
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
    public void testGetOrder_Success_UserAccess() throws Exception {
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
    public void testGetOrder_Failure_NoAccess() throws Exception {
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
    public void testGetOrder_Failure_OrderNotFound() throws Exception {
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