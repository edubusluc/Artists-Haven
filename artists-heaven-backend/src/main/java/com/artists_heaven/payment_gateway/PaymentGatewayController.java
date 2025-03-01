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

    @PostMapping("/checkout")
    public String paymentCheckout(@RequestBody List<CartItemDTO> items) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principalUser = authentication.getPrincipal();
        Long id = null;
        if (principalUser instanceof User) {
            User user = (User) principalUser;
            id = user.getId();
        }
        try {
            return paymentGatewayService.checkoutProducts(items, id);
        } catch (Exception e) {
            return "Error en el procesamiento del pago: " + e.getMessage();
        }
    }

    @Transactional
    @PostMapping("/stripeWebhook")
    public ResponseEntity<String> handleStripeEvent(@RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            paymentGatewayService.processStripeEvent(payload, sigHeader);
            return ResponseEntity.ok("Evento recibido");
        } catch (Exception e) {
            System.err.println("Error procesando el evento de Stripe: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor");
        }
    }
}
