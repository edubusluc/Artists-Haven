package com.artists_heaven.order;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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

}
