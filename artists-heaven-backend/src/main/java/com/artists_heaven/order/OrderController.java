package com.artists_heaven.order;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.entities.user.UserRole;

import jakarta.persistence.EntityNotFoundException;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/myOrders")
    public ResponseEntity<List<Order>> getMyOrders() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();
            List<Order> orders = orderService.getMyOrders(user.getId());
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

@GetMapping("/{id}")
public ResponseEntity<OrderDetailsDTO> getOrder(@PathVariable Long id) {
    try {
        Order order = orderService.findOrderById(id);

        // Obtener el usuario autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        boolean isAdmin = user.getRole().equals(UserRole.ADMIN);

        // Si el pedido no tiene usuario asignado (pedido de invitado)
        if (order.getUser() == null) {
            // Solo los admins pueden ver pedidos sin dueño
            if (!isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } else {
            // Si el pedido tiene un usuario, verificar si es el dueño o admin
            boolean isOwner = order.getUser().getId().equals(user.getId());

            // Permitir acceso solo si el usuario es el propietario o un admin
            if (!isOwner && !isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        OrderDetailsDTO orderDetailsDTO = new OrderDetailsDTO(order);
        return ResponseEntity.ok(orderDetailsDTO);

    } catch (EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404 si no existe
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500 si hay otro error
    }
}


}
