package com.artists_heaven.payment_gateway;

import org.springframework.web.bind.annotation.RestController;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.shopping_cart.CartItemDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/payment_process")
public class PaymentGatewayController {

    private final PaymentGatewayService paymentGatewayService;

    public PaymentGatewayController(PaymentGatewayService paymentGatewayService) {
        this.paymentGatewayService = paymentGatewayService;
    }

    @PostMapping("/checkout")
    @Operation(summary = "Process payment checkout", description = "Processes the payment for the list of cart items associated with the authenticated user.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment processed successfully", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "500", description = "Internal server error during payment processing", content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<String> paymentCheckout(
            @RequestBody(description = "List of items to checkout", required = true, content = @Content(array = @ArraySchema(schema = @Schema(implementation = CartItemDTO.class)))) @org.springframework.web.bind.annotation.RequestBody List<CartItemDTO> items) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principalUser = authentication.getPrincipal();
        Long id = null;

        if (principalUser instanceof User user) {
            id = user.getId();
        }

        try {
            return ResponseEntity.ok(paymentGatewayService.checkoutProducts(items, id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/stripeWebhook")
    public ResponseEntity<String> handleStripeEvent(HttpServletRequest request,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            // Leer el payload como bytes directamente
            byte[] payloadBytes = request.getInputStream().readAllBytes();

            // Convertir a String usando UTF-8, sin modificar nada
            String payload = new String(payloadBytes, StandardCharsets.UTF_8);

            // Procesar evento
            paymentGatewayService.processStripeEvent(payload, sigHeader);

            return ResponseEntity.ok("Evento recibido");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor");
        }
    }

}
