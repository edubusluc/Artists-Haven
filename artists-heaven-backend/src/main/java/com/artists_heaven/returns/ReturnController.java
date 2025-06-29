package com.artists_heaven.returns;

import org.springframework.web.bind.annotation.RestController;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.order.Order;
import com.artists_heaven.order.OrderService;

import io.swagger.v3.oas.annotations.Operation;

import java.nio.charset.StandardCharsets;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/returns")
public class ReturnController {

    private final ReturnService returnService;

    private final OrderService orderService;

    public ReturnController(ReturnService returnService, OrderService orderService) {
        this.returnService = returnService;
        this.orderService = orderService;
    }

    @PostMapping("/create")
    public ResponseEntity<String> createReturnForOrder(@RequestBody ReturnRequestDTO returnRequestDTO) {
        try {
            Order order = orderService.findOrderById(returnRequestDTO.getOrderId());
            returnService.createReturnForOrder(order, returnRequestDTO.getReason(), returnRequestDTO.getEmail());
            return ResponseEntity
                    .ok("Return request created successfully for order ID: " + returnRequestDTO.getOrderId());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error while creating return: " + e.getMessage());
        }
    }

    @GetMapping("/{orderId}/label")
    @Operation(summary = "Get return label PDF")
    public ResponseEntity<byte[]> getReturnLabel(
            @PathVariable Long orderId,
            @RequestParam(required = false) String email) {

        try {
            Order order = orderService.findOrderById(orderId);

            // Verificar si el usuario está autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated()
                    && authentication.getPrincipal() instanceof User) {

                User user = (User) authentication.getPrincipal();

                if (!order.getUser().getId().equals(user.getId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }

            } else {
                // Si no está autenticado o no es un User, verificar el email
                if (email == null || !order.getUser().getEmail().equalsIgnoreCase(email)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }

            byte[] pdf = returnService.generateReturnLabelPdf(orderId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.inline()
                    .filename("ORDER-RETURN" + order.getIdentifier() + ".pdf").build());

            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(e.getMessage().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

}
