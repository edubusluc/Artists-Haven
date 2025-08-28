package com.artists_heaven.returns;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import org.springframework.http.MediaType;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.exception.GlobalExceptionHandler;
import com.artists_heaven.order.Order;
import com.artists_heaven.order.OrderService;
import com.artists_heaven.standardResponse.StandardResponse;

import org.springframework.security.core.context.SecurityContext;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

class ReturnControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReturnService returnService;

    @Mock
    private OrderService orderService;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private ReturnController returnController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(returnController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testCreateReturnForOrder_Success() {
        ReturnRequestDTO dto = new ReturnRequestDTO();
        dto.setOrderId(1L);
        dto.setReason("No me gustó");
        dto.setEmail("cliente@example.com");

        Order mockOrder = new Order();
        mockOrder.setId(1L);

        when(orderService.findOrderById(1L)).thenReturn(mockOrder);
        doNothing().when(returnService).createReturnForOrder(mockOrder, dto.getReason(), dto.getEmail());
        when(messageSource.getMessage(eq("return.message.successful"), any(), any()))
                .thenReturn("Solicitud de devolución creada correctamente. Devolución creada para el pedido con ID: ");

        ResponseEntity<StandardResponse<String>> response = returnController.createReturnForOrder(dto, "es");

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody().getMessage()
                .contains("Solicitud de devolución creada correctamente. Devolución creada para el pedido con ID: "));
    }

    @Test
    void testGetReturnLabel_AsAuthenticatedUser_Success() {
        Long orderId = 1L;
        byte[] fakePdf = new byte[] { 1, 2, 3 };

        User user = new User();
        user.setId(100L);
        user.setEmail("user@example.com");

        Order order = new Order();
        order.setUser(user);
        order.setIdentifier(123L);

        // Mock authentication
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(user);
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(orderService.findOrderById(orderId)).thenReturn(order);
        when(returnService.generateReturnLabelPdf(orderId)).thenReturn(fakePdf);

        ResponseEntity<byte[]> response = returnController.getReturnLabel(orderId, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("application/pdf", response.getHeaders().getContentType().toString());
        assertArrayEquals(fakePdf, response.getBody());
    }

    @Test
    void testGetReturnLabel_UnauthenticatedUserWithCorrectEmail() {
        Long orderId = 1L;
        byte[] fakePdf = new byte[] { 1, 2, 3 };

        User user = new User();
        user.setId(100L);
        user.setEmail("test@example.com");

        Order order = new Order();
        order.setUser(user);
        order.setIdentifier(999L);

        // No authentication
        SecurityContextHolder.clearContext();

        when(orderService.findOrderById(orderId)).thenReturn(order);
        when(returnService.generateReturnLabelPdf(orderId)).thenReturn(fakePdf);

        ResponseEntity<byte[]> response = returnController.getReturnLabel(orderId, "test@example.com");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(fakePdf, response.getBody());
    }

    @Test
    void testGetReturnLabel_UnauthenticatedUserWithWrongEmail() throws Exception {
        Long orderId = 1L;

        // Crear el usuario y la orden
        User user = new User();
        user.setEmail("real@example.com");
        user.setId(99L);

        Order order = new Order();
        order.setUser(user);

        // Simulamos la respuesta del servicio
        when(orderService.findOrderById(orderId)).thenReturn(order);

        // Limpiamos el contexto de seguridad (usuario no autenticado)
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        // Llamada al controlador
        mockMvc.perform(get("/api/returns/{orderId}/label", orderId)
                .param("email", "fake@example.com")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("You are not allowed to access this return label"))
                .andExpect(jsonPath("$.status").value(403));

        verify(orderService).findOrderById(orderId);
        verify(returnService, never()).generateReturnLabelPdf(anyLong());
    }

    // Escenario 3: Usuario autenticado pero NO dueño -> 403
    @Test
    void testGetReturnLabel_AuthenticatedUserNotOwner() throws Exception {
        Long orderId = 3L;

        User orderUser = new User();
        orderUser.setEmail("owner@example.com");
        orderUser.setId(101L);

        Order order = new Order();
        order.setUser(orderUser);

        when(orderService.findOrderById(orderId)).thenReturn(order);

        // Usuario autenticado distinto
        User loggedUser = new User();
        loggedUser.setId(202L);

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(loggedUser);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        mockMvc.perform(get("/api/returns/{orderId}/label", orderId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You are not allowed to access this return label"))
                .andExpect(jsonPath("$.status").value(403));

        verify(returnService, never()).generateReturnLabelPdf(anyLong());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }
}
