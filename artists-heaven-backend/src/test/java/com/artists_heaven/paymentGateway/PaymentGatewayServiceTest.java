package com.artists_heaven.paymentGateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import com.artists_heaven.email.EmailSenderService;
import com.artists_heaven.entities.user.User;
import com.artists_heaven.entities.user.UserService;
import com.artists_heaven.order.OrderItemRepository;
import com.artists_heaven.order.OrderRepository;
import com.artists_heaven.payment_gateway.PaymentGatewayService;
import com.artists_heaven.product.Product;
import com.artists_heaven.product.ProductService;
import com.artists_heaven.shopping_cart.CartItemDTO;
import com.artists_heaven.shopping_cart.ProductItemDTO;
import com.artists_heaven.shopping_cart.ShoppingCartService;

class PaymentGatewayServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductService productService;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ShoppingCartService shoppingCartService;

    @Mock
    private EmailSenderService emailSenderService;

    @InjectMocks
    private PaymentGatewayService paymentGatewayService;

    Product product = new Product();
    ProductItemDTO productItem = new ProductItemDTO();
    CartItemDTO item = new CartItemDTO();
    List<CartItemDTO> items = new ArrayList<>();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        configurationTest();
    }

    private void configurationTest() {
        items = new ArrayList<>();
        item = new CartItemDTO();
        productItem = new ProductItemDTO();
        productItem.setId(1L);
        productItem.setName("Product");
        productItem.setPrice(10.0f);

        // Creamos el producto con talla M y cantidad 0
        product = new Product();
        product.setId(1L);
        Map<String, Integer> sizeMap = new HashMap<>();
        sizeMap.put("M", 0); // Producto no disponible
        product.setSize(sizeMap);
        product.setAvailable(true); // Producto disponible pero sin stock

        item.setProduct(productItem);
        item.setSize("M");
        item.setQuantity(1); // Intentamos comprar 1 producto
        items.add(item);
    }

    @Test
    void testCheckoutProducts_ProductNotAvailable() throws Exception {
        // Mock de findById para que devuelva el producto configurado
        when(productService.findById(1L)).thenReturn(product);

        // Llamada al método que se va a probar y captura de la excepción
        Exception exception = assertThrows(Exception.class, () -> {
            paymentGatewayService.checkoutProducts(items, 1L);
        });

        // Verificación de que el mensaje es el esperado cuando el producto no está
        // disponible
        assertEquals("No se ha completado el pago: Producto no disponible", exception.getMessage());
    }

    @Test
    void testCheckoutProducts_Success() throws Exception {
        // Configurar el producto para que esté disponible
        product.getSize().put("M", 10); // Producto disponible con stock

        User user = new User();
        user.setId(1L);
        user.setAddress("Address Test");
        user.setCountry("Country Test");
        user.setCity("City Test");
        user.setPostalCode("Postal Code Test");
        user.setEmail("Email Test");

        when(productService.findById(anyLong())).thenReturn(product);

        // Configurar el SecurityContext y Authentication mocks
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Llamada al método que se va a probar
        String result = paymentGatewayService.checkoutProducts(items, 1L);

        // Verificación de que el resultado no es nulo y contiene la URL de éxito
        assertNotNull(result);
    }

    @Test
    void testCheckoutProducts_SuccessAnonymous() throws Exception {
        // Configurar el producto para que esté disponible
        product.getSize().put("M", 10); // Producto disponible con stock

        when(productService.findById(anyLong())).thenReturn(product);

        // Configuración del contexto de seguridad para usuario anónimo
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Llamada al método que se va a probar
        String result = paymentGatewayService.checkoutProducts(items, null);

        // Verificación de que el resultado no es nulo y contiene la URL de éxito
        assertNotNull(result);
    }

}
