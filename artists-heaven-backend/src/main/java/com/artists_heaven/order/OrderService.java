package com.artists_heaven.order;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<Order> getMyOrders(Long userId) {
        return orderRepository.getOrdersByUserId(userId);
    }

    public Page<Order> getMyOrdersPageable(Long userId, Pageable pageable) {
        return orderRepository.getOrdersByUserIdPageable(userId, pageable);
    }

    public Integer getNumOrdersPerYear(int year) {
        return orderRepository.getNumOrdersPerYear(year);
    }

    public Double getIncomePerYear(int year) {
        return orderRepository.getIncomePerYear(year);
    }

    public void save(Order order) {
        orderRepository.save(order);
    }

    public Order findOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));
    }

    public Order getOrderByIdentifier(Long identifier) {
        return orderRepository.findOrderByIdentifier(identifier);
    }

}
