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
import com.artists_heaven.exception.AppExceptions.ForbiddenActionException;
import com.artists_heaven.exception.AppExceptions.ResourceNotFoundException;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    private final MessageSource messageSource;

    public OrderService(OrderRepository orderRepository, MessageSource messageSource) {
        this.orderRepository = orderRepository;
        this.messageSource = messageSource;
    }

    /**
     * Retrieves all orders for a specific user.
     *
     * @param userId the ID of the user
     * @return a list of {@link Order} objects associated with the user
     */
    public List<Order> getMyOrders(Long userId) {
        return orderRepository.getOrdersByUserId(userId);
    }

    /**
     * Retrieves paginated orders for a specific user.
     *
     * @param userId   the ID of the user
     * @param pageable pagination information
     * @return a page of {@link Order} objects associated with the user
     */
    public Page<Order> getMyOrdersPageable(Long userId, Pageable pageable) {
        return orderRepository.getOrdersByUserIdPageable(userId, pageable);
    }

    /**
     * Returns the number of orders placed in a given year.
     *
     * @param year the year to filter orders
     * @return the number of orders in that year
     */
    public Integer getNumOrdersPerYear(int year) {
        return orderRepository.getNumOrdersPerYear(year);
    }

    /**
     * Returns the total income from orders in a given year.
     *
     * @param year the year to filter orders
     * @return the total income as a {@link Double}
     */
    public Double getIncomePerYear(int year) {
        return orderRepository.getIncomePerYear(year);
    }

    /**
     * Saves an order to the repository.
     *
     * @param order the {@link Order} object to save
     */
    public void save(Order order) {
        orderRepository.save(order);
    }

    /**
     * Finds an order by its database ID.
     *
     * @param id the ID of the order
     * @return the {@link Order} object
     * @throws ResourceNotFoundException if no order exists with the given ID
     */
    public Order findOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Order not found with id: " + id));
    }

    /**
     * Retrieves an order by its unique identifier.
     *
     * @param identifier the unique order identifier
     * @param lang       the language code for localized error messages
     * @return the {@link Order} object
     * @throws ResourceNotFoundException if no order exists with the given
     *                                   identifier
     */
    public Order getOrderByIdentifier(Long identifier, String lang) {
        Locale locale = new Locale(lang);
        Order order = orderRepository.findOrderByIdentifier(identifier);
        if (order == null) {
            String msg = messageSource.getMessage("order.notIdentifier", null, locale);
            throw new AppExceptions.ResourceNotFoundException(msg + identifier);
        }
        return order;
    }

    /**
     * Retrieves detailed order information by ID with permission checks.
     * Users can access their own orders, while administrators can access any order.
     *
     * @param id the ID of the order
     * @return an {@link OrderDetailsDTO} containing detailed order information
     * @throws ForbiddenActionException  if the user is not authorized to access the
     *                                   order
     * @throws ResourceNotFoundException if no order exists with the given ID
     */
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
