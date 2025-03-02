package com.artists_heaven.order;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<Order> getMyOrders(Long userId) {
        return orderRepository.getOrdersByUserId(userId);
    }

}
