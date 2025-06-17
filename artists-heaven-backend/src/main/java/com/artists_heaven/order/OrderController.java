package com.artists_heaven.order;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.entities.user.UserRole;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
    @Operation(summary = "Get current user's orders", description = "Returns a list of orders placed by the authenticated user. Requires authentication.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of orders", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Order.class))),
            @ApiResponse(responseCode = "400", description = "Bad request, unable to retrieve orders", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required", content = @Content)
    })
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
    @Operation(summary = "Get order details by ID", description = "Retrieves detailed information of an order by its ID. Access is restricted to the order owner or administrators. "
            +
            "Orders without an assigned user can only be accessed by administrators.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved order details", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderDetailsDTO.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden: Access denied to this order", content = @Content),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<OrderDetailsDTO> getOrder(
            @Parameter(description = "ID of the order to retrieve", required = true) @PathVariable Long id) {

        try {
            Order order = orderService.findOrderById(id);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            boolean isAdmin = user.getRole().equals(UserRole.ADMIN);

            if (order.getUser() == null) {
                if (!isAdmin) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            } else {
                boolean isOwner = order.getUser().getId().equals(user.getId());
                if (!isOwner && !isAdmin) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }

            OrderDetailsDTO orderDetailsDTO = new OrderDetailsDTO(order);
            return ResponseEntity.ok(orderDetailsDTO);

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
