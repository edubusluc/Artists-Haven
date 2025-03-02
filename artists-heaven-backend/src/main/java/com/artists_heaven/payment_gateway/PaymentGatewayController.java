package com.artists_heaven.payment_gateway;

import org.springframework.web.bind.annotation.RestController;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.shopping_cart.CartItemDTO;
import jakarta.transaction.Transactional;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/payment_process")
public class PaymentGatewayController {

    private final PaymentGatewayService paymentGatewayService;

    public PaymentGatewayController(PaymentGatewayService paymentGatewayService) {
        this.paymentGatewayService = paymentGatewayService;
    }

    // Endpoint to handle the checkout process for payment
    @PostMapping("/checkout")
    public ResponseEntity<String>  paymentCheckout(@RequestBody List<CartItemDTO> items) {
        // Get the current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principalUser = authentication.getPrincipal();
        Long id = null;

        // Check if the authenticated user is an instance of User and retrieve their ID
        if (principalUser instanceof User user) {
            id = user.getId();
        }

        try {
            // Process the payment checkout and return the result
            return ResponseEntity.ok(paymentGatewayService.checkoutProducts(items, id));
        } catch (Exception e) {
            // Return an error message if the payment processing fails
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // Endpoint to handle incoming Stripe webhook events
    @Transactional
    @PostMapping("/stripeWebhook")
    public ResponseEntity<String> handleStripeEvent(@RequestBody String payload, // The event payload sent by Stripe
            @RequestHeader("Stripe-Signature") String sigHeader) { // The Stripe signature to verify the event

        try {
            // Process the Stripe event using the provided payload and signature
            paymentGatewayService.processStripeEvent(payload, sigHeader);

            // Return a 200 OK response if the event is processed successfully
            return ResponseEntity.ok("Evento recibido");
        } catch (Exception e) {
            // Return an error response in case of an exception (e.g., invalid event or
            // processing error)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor");
        }
    }
}
