package com.artists_heaven.order;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import jakarta.persistence.EntityNotFoundException;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

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

        orderService.saveOrder(order);

        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void testFindOrderById_NotFound() {
        // Datos de prueba
        Long orderId = 1L;

        // Mock del repositorio: simulamos que no se encuentra la orden
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Verificamos que se lance la excepción EntityNotFoundException
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
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

}
