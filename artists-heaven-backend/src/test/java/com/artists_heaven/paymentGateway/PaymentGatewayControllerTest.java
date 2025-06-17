package com.artists_heaven.paymentGateway;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.payment_gateway.PaymentGatewayController;
import com.artists_heaven.payment_gateway.PaymentGatewayService;
import com.artists_heaven.shopping_cart.CartItemDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

class PaymentGatewayControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PaymentGatewayService paymentGatewayService;

    @InjectMocks
    private PaymentGatewayController paymentGatewayController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(paymentGatewayController).build();
    }

    @Test
    void testPaymentCheckout_Success() throws Exception {
        List<CartItemDTO> items = List.of(new CartItemDTO());
        User user = new User();
        user.setId(1L);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(paymentGatewayService.checkoutProducts(anyList(), anyLong())).thenReturn("Success URL");

        mockMvc.perform(post("/api/payment_process/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(items)))
                .andExpect(status().isOk());
    }

    @Test
    void testPaymentCheckout_SuccessAnonymous() throws Exception {
        List<CartItemDTO> items = List.of(new CartItemDTO());

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(null);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(paymentGatewayService.checkoutProducts(anyList(), anyLong())).thenReturn("Success URL");

        mockMvc.perform(post("/api/payment_process/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(items)))
                .andExpect(status().isOk());
    }

    @Test
    void testPaymentCheckout_Error() throws Exception {
        List<CartItemDTO> items = List.of(new CartItemDTO());
        User user = new User();
        user.setId(1L);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(paymentGatewayService.checkoutProducts(anyList(), anyLong())).thenThrow(new RuntimeException("Error"));

        mockMvc.perform(post("/api/payment_process/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(items)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testHandleStripeEvent_Success() throws Exception {
        String payload = "test_payload";
        String sigHeader = "test_sigHeader";

        doNothing().when(paymentGatewayService).processStripeEvent(anyString(), anyString());

        mockMvc.perform(post("/api/payment_process/stripeWebhook")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Stripe-Signature", sigHeader)
                .content(payload))
                .andExpect(status().isOk());
    }
}
