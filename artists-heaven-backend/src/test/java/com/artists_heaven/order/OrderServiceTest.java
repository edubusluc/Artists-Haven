package com.artists_heaven.order;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.entities.user.UserRole;
import com.artists_heaven.exception.AppExceptions;
import com.artists_heaven.exception.AppExceptions.ResourceNotFoundException;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Mock
    private MessageSource messageSource;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetMyOrders() {
        Order order = new Order();
        order.setId(1L);

        when(orderRepository.getOrdersByUserId(1L)).thenReturn(List.of(order));

        List<Order> orders = orderService.getMyOrders(1L);
        assertNotNull(orders);
    }

    @Test
    void testFindOrderById_Exists() {
        // Datos de prueba
        Long orderId = 1L;
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.PAID);

        // Mock del repositorio
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Llamamos al método findOrderById
        Order result = orderService.findOrderById(orderId);

        // Verificamos que el resultado es el que esperamos
        assertEquals(order, result);
    }

    @Test
    void testSaveOrder() {
        // Crear un objeto Order de prueba
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.PAID);

        orderService.save(order);

        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void testFindOrderById_NotFound() {
        // Datos de prueba
        Long orderId = 1L;

        // Mock del repositorio: simulamos que no se encuentra la orden
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Verificamos que se lance la excepción EntityNotFoundException
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            orderService.findOrderById(orderId);
        });

        // Verificamos el mensaje de la excepción
        assertEquals("Order not found with id: " + orderId, exception.getMessage());
    }

    @Test
    void testGetMyOrdersPageable() {
        Order order = new Order();
        List<Order> orders = List.of(order);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> page = new PageImpl<>(orders, pageable, orders.size());

        when(orderRepository.getOrdersByUserIdPageable(1L, pageable)).thenReturn(page);
        Page<Order> result = orderService.getMyOrdersPageable(1l, pageable);
        assertNotNull(result);

    }

    @Test
    void testGetIrderByIdentifier() {
        Order order = new Order();
        order.setId(1L);
        order.setIdentifier(10L);

        when(orderRepository.findOrderByIdentifier(10L)).thenReturn(order);

        Order result = orderService.getOrderByIdentifier(10L, "es");

        assertNotNull(result);
        assertTrue(result.getIdentifier() == 10L);
    }

    @Test
    void testGetOrderByIdentifier_NotFound() {
        // Preparación: no existe el pedido
        when(orderRepository.findOrderByIdentifier(99L)).thenReturn(null);
        when(messageSource.getMessage(eq("order.notIdentifier"), any(), any(Locale.class)))
                .thenReturn("Pedido no encontrado con identificador: ");

        // Ejecución y verificación de la excepción
        AppExceptions.ResourceNotFoundException exception = assertThrows(
                AppExceptions.ResourceNotFoundException.class,
                () -> orderService.getOrderByIdentifier(99L, "es"));

        // Verificación del mensaje
        assertTrue(exception.getMessage().contains("Pedido no encontrado con identificador: 99"));

        // Verificar interacción con el repositorio
        verify(orderRepository, times(1)).findOrderByIdentifier(99L);
        verify(messageSource, times(1))
                .getMessage(eq("order.notIdentifier"), any(), any(Locale.class));
    }

    @Test
    void testGetNumOrderPerYear() {
        int year = 2025;
        Order order = new Order();
        List<Order> orders = List.of(order);

        when(orderRepository.getNumOrdersPerYear(year)).thenReturn(orders.size());
        Integer result = orderService.getNumOrdersPerYear(year);

        assertNotNull(result);
    }

    @Test
    void testGgetIncomePerYear() {
        int year = 2025;

        when(orderRepository.getIncomePerYear(year)).thenReturn(10.0);
        Double result = orderService.getIncomePerYear(year);

        assertNotNull(result);
    }

    @Test
    void getOrderDetailsById_OrderNotFound_ThrowsException() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        AppExceptions.ResourceNotFoundException ex = assertThrows(
                AppExceptions.ResourceNotFoundException.class,
                () -> orderService.getOrderDetailsById(1L));

        assertEquals("Order not found with id: 1", ex.getMessage());
        verify(orderRepository).findById(1L);
    }

    @Test
    void getOrderDetailsById_AdminAccess_AlwaysAllowed() {
        // Arrange
        Long orderId = 1L;
        User adminUser = new User();
        adminUser.setId(99L);
        adminUser.setRole(UserRole.ADMIN);

        Order order = new Order();
        order.setId(orderId);
        order.setUser(null);
        order.setIdentifier(10L);

        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(adminUser);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act
        OrderDetailsDTO result = orderService.getOrderDetailsById(orderId);

        // Assert
        assertNotNull(result);
    }

    @Test
    void getOrderDetailsById_OrderWithDifferentUser_NotAdmin_ThrowsForbidden() {
        // Arrange
        Long orderId = 1L;
        User loggedUser = new User();
        loggedUser.setId(10L);
        loggedUser.setRole(UserRole.USER);

        User orderOwner = new User();
        orderOwner.setId(20L);

        Order order = new Order();
        order.setId(orderId);
        order.setUser(orderOwner);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(loggedUser);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Act & Assert
        AppExceptions.ForbiddenActionException ex = assertThrows(
                AppExceptions.ForbiddenActionException.class,
                () -> orderService.getOrderDetailsById(orderId));

        assertEquals("You do not have permission to access this order.", ex.getMessage());
    }

    @Test
    void getOrderDetailsById_OrderWithoutUser_NotAdmin_ThrowsForbidden() {
        // Arrange
        Long orderId = 1L;
        User normalUser = new User();
        normalUser.setId(10L);
        normalUser.setRole(UserRole.USER);

        Order order = new Order();
        order.setId(orderId);
        order.setUser(null);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(normalUser);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Act & Assert
        AppExceptions.ForbiddenActionException ex = assertThrows(
                AppExceptions.ForbiddenActionException.class,
                () -> orderService.getOrderDetailsById(orderId));

        assertEquals("Only administrators can access unassigned orders.", ex.getMessage());
    }

    @Test
    void getOrderDetailsById_OrderWithSameUser_NotAdmin_ReturnsDTO() {
        // Arrange
        Long orderId = 1L;
        User loggedUser = new User();
        loggedUser.setId(10L);
        loggedUser.setRole(UserRole.USER);

        Order order = new Order();
        order.setId(orderId);
        order.setUser(loggedUser); // El pedido pertenece al usuario logueado
        order.setIdentifier(10L);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(loggedUser);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Act
        OrderDetailsDTO result = orderService.getOrderDetailsById(orderId);

        // Assert
        assertNotNull(result);
        assertEquals(order.getId(), result.getId());
    }

}
