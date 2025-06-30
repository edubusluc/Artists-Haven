package com.artists_heaven.returns;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.order.Order;
import com.artists_heaven.order.OrderService;

class ReturnControllerTest {
    @Mock
    private ReturnService returnService;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private ReturnController returnController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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

        ResponseEntity<String> response = returnController.createReturnForOrder(dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Return request created successfully"));
    }

    @Test
    void testCreateReturnForOrder_Invalid() {
        ReturnRequestDTO dto = new ReturnRequestDTO();
        dto.setOrderId(1L);
        dto.setReason("Motivo");
        dto.setEmail("mal@example.com");

        Order order = new Order();
        order.setId(1L);

        when(orderService.findOrderById(1L)).thenReturn(order);
        doThrow(new IllegalStateException("Email inválido"))
                .when(returnService).createReturnForOrder(order, dto.getReason(), dto.getEmail());

        ResponseEntity<String> response = returnController.createReturnForOrder(dto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Email inválido"));
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
    void testGetReturnLabel_UnauthenticatedUserWithWrongEmail() {
        Long orderId = 1L;

        User user = new User();
        user.setEmail("real@example.com");
        user.setId(99L);

        Order order = new Order();
        order.setUser(user);

        SecurityContextHolder.clearContext();

        when(orderService.findOrderById(orderId)).thenReturn(order);

        ResponseEntity<byte[]> response = returnController.getReturnLabel(orderId, "fake@example.com");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void testGetReturnLabel_AuthenticatedWrongUser() {
        Long orderId = 1L;

        User userInOrder = new User();
        userInOrder.setId(1L);
        userInOrder.setEmail("cliente@example.com");

        User loggedInUser = new User();
        loggedInUser.setId(2L);

        Order order = new Order();
        order.setUser(userInOrder);

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(loggedInUser);
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(orderService.findOrderById(orderId)).thenReturn(order);

        ResponseEntity<byte[]> response = returnController.getReturnLabel(orderId, null);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }
}
