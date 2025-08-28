package com.artists_heaven.order;

import java.util.List;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.entities.user.UserRole;
import com.artists_heaven.exception.AppExceptions;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    private final MessageSource messageSource;

    public OrderService(OrderRepository orderRepository, MessageSource messageSource) {
        this.orderRepository = orderRepository;
        this.messageSource = messageSource;
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
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Order not found with id: " + id));
    }

    public Order getOrderByIdentifier(Long identifier, String lang) {
        Locale locale = new Locale(lang);
        Order order = orderRepository.findOrderByIdentifier(identifier);
        if (order == null) {
            String msg = messageSource.getMessage("order.notIdentifier", null, locale);
            throw new AppExceptions.ResourceNotFoundException(msg + identifier);
        }
        return order;
    }

    public OrderDetailsDTO getOrderDetailsById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Order not found with id: " + id));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        boolean isAdmin = user.getRole().equals(UserRole.ADMIN);

        if (order.getUser() == null && !isAdmin) {
            throw new AppExceptions.ForbiddenActionException("Only administrators can access unassigned orders.");
        }

        if (order.getUser() != null && !order.getUser().getId().equals(user.getId()) && !isAdmin) {
            throw new AppExceptions.ForbiddenActionException("You do not have permission to access this order.");
        }

        return new OrderDetailsDTO(order);
    }

}
