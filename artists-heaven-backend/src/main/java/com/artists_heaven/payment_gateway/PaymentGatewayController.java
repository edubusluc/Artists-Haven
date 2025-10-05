package com.artists_heaven.payment_gateway;

import org.springframework.web.bind.annotation.RestController;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.shopping_cart.CartItemDTO;
import com.stripe.Stripe;
import com.stripe.model.checkout.Session;

import io.github.cdimascio.dotenv.Dotenv;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/payment_process")
public class PaymentGatewayController {

    private final PaymentGatewayService paymentGatewayService;

    Dotenv dotenv = Dotenv.load();

    public PaymentGatewayController(PaymentGatewayService paymentGatewayService) {
        this.paymentGatewayService = paymentGatewayService;
    }

    @PostMapping("/checkout")
    @Operation(summary = "Process payment checkout", description = "Processes the payment for the list of cart items associated with the authenticated user.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Payment processed successfully", content = @Content(mediaType = "text/plain"))
    @ApiResponse(responseCode = "500", description = "Internal server error during payment processing", content = @Content(mediaType = "text/plain"))
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

    @Operation(summary = "Handle Stripe webhook event", description = "Receives and processes webhook events sent by Stripe after a payment event (e.g., successful payment).", requestBody = @RequestBody(description = "Raw Stripe event payload sent as webhook (read from request body)", required = true))
    @ApiResponse(responseCode = "200", description = "Event received and processed successfully", content = @Content(mediaType = "text/plain"))
    @ApiResponse(responseCode = "500", description = "Internal server error while processing Stripe event", content = @Content(mediaType = "text/plain"))
    @PostMapping("/stripeWebhook")
    public ResponseEntity<String> handleStripeEvent(HttpServletRequest request,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            byte[] payloadBytes = request.getInputStream().readAllBytes();

            String payload = new String(payloadBytes, StandardCharsets.UTF_8);

            paymentGatewayService.processStripeEvent(payload, sigHeader);

            return ResponseEntity.ok("Evento recibido");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor");
        }
    }

    @Operation(summary = "Confirm payment status", description = "Confirms the payment status using the Stripe session ID after a user completes the payment process.")
    @ApiResponse(responseCode = "200", description = "Payment confirmed successfully", content = @Content(mediaType = "application/json", schema = @Schema(example = "{ \"status\": \"success\", \"amount_total\": 1999, \"currency\": \"usd\", \"email\": \"customer@example.com\" }")))
    @ApiResponse(responseCode = "400", description = "Payment not completed yet (pending)", content = @Content(mediaType = "application/json", schema = @Schema(example = "{ \"status\": \"pending\" }")))
    @ApiResponse(responseCode = "500", description = "Internal server error during confirmation", content = @Content(mediaType = "application/json", schema = @Schema(example = "{ \"error\": \"Stripe API key missing or invalid session ID\" }")))
    @GetMapping("/confirm")
    public ResponseEntity<?> confirmPayment(@RequestParam String session_id) {
        try {
            Stripe.apiKey = dotenv.get("STRIPE_KEY");
            Session session = Session.retrieve(session_id);
            if ("paid".equals(session.getPaymentStatus())) {
                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "amount_total", session.getAmountTotal() / 100,
                        "currency", session.getCurrency(),
                        "email", session.getCustomerDetails().getEmail()));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("status", "pending"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

}
