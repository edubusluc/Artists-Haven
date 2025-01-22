package com.artists_heaven.shoppingCart;

import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.AuthenticationException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.entities.user.UserService;
import com.artists_heaven.product.Product;
import com.artists_heaven.product.ProductService;

public class ShoppingCartControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ShoppingCartService shoppingCartService;

    @Mock
    private UserService userService;

    @Mock
    private ProductService productService;

    @InjectMocks
    private ShoppingCartController shoppingCartController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(shoppingCartController).build();
    }

    @Test
    void myShoppingCart_ReturnsEmptyCart_ForAnonymousUser() throws Exception {
        // Configuración del contexto de seguridad para usuario anónimo
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Ejecución y verificación
        mockMvc.perform(get("/api/myShoppingCart")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty());

        SecurityContextHolder.clearContext();
    }

    @Test
    void myShoppingCart_ReturnsShoppingCart_ForAuthenticatedUser() throws Exception {
        // Datos de prueba
        User user = new User();
        user.setId(1L);

        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice((float) 100.00);

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setSize("M");
        cartItem.setQuantity(2);

        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setId(1L);
        shoppingCart.setItems(List.of(cartItem));

        // Configuración del contexto de seguridad
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Configuración de servicios mock
        when(shoppingCartService.getShoppingCart(user.getId())).thenReturn(shoppingCart);

        // Ejecución y verificación
        mockMvc.perform(get("/api/myShoppingCart")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(shoppingCart.getId()))
                .andExpect(jsonPath("$.items[0].product.id").value(product.getId()))
                .andExpect(jsonPath("$.items[0].product.name").value(product.getName()))
                .andExpect(jsonPath("$.items[0].product.price").value(product.getPrice().doubleValue()))
                .andExpect(jsonPath("$.items[0].size").value(cartItem.getSize()))
                .andExpect(jsonPath("$.items[0].quantity").value(cartItem.getQuantity()));

        SecurityContextHolder.clearContext();
    }

    @Test
    void myShoppingCart_ReturnsBadRequest_OnException() throws Exception {
        // Configuración del contexto de seguridad
        Authentication authentication = mock(Authentication.class);
        User user = new User();
        user.setId(1L);
        when(authentication.getPrincipal()).thenReturn(user);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Configuración del servicio mock para lanzar excepción
        when(shoppingCartService.getShoppingCart(user.getId())).thenThrow(new RuntimeException("Error"));

        // Ejecución y verificación
        mockMvc.perform(get("/api/myShoppingCart")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        SecurityContextHolder.clearContext();
    }

    @Test
    void addProductsToMyShoppingCart_Returns200_WhenProductAddedSuccessfully() throws Exception {
        // Configuración de datos de prueba
        AddProductDTO request = new AddProductDTO();
        request.setProductId(1L);
        request.setSize("M");

        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(10.0f);

        User user = new User();
        user.setId(1L); // Asignar ID al usuario

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setSize("M");
        cartItem.setQuantity(1);

        List<CartItem> cartItems = new ArrayList<>();
        cartItems.add(cartItem);

        // Configuración de mocks
        when(shoppingCartService.addProducts(eq(user.getId()), eq(product), eq(request.getSize()), eq(1)))
                .thenReturn(cartItems);
        when(productService.findById(eq(request.getProductId()))).thenReturn(product);
        when(userService.getUserById(eq(user.getId()))).thenReturn(user); // Simula la obtención del usuario

        // Simular autenticación en el SecurityContext
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Ejecución y verificación
        mockMvc.perform(post("/api/myShoppingCart/addProducts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].product.id").value(product.getId()))
                .andExpect(jsonPath("$[0].product.name").value(product.getName()))
                .andExpect(jsonPath("$[0].product.price").value(product.getPrice().doubleValue()))
                .andExpect(jsonPath("$[0].size").value("M"))
                .andExpect(jsonPath("$[0].quantity").value(1));

        // Limpiar el contexto de seguridad después de la prueba
        SecurityContextHolder.clearContext();
    }

    @Test
    void addProductsToMyShoppingCart_Returns401_WhenUserNotAuthenticated() throws Exception {
        // Configuración de datos de prueba
        AddProductDTO request = new AddProductDTO();
        request.setProductId(1L);
        request.setSize("M");

        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(10.0f);

        // Simulamos que el usuario no está autenticado
        when(shoppingCartService.addProducts(anyLong(), eq(product), eq(request.getSize()), eq(1)))
                .thenThrow(new AuthenticationException("User not authenticated") {
                });

        // Ejecución y verificación
        mockMvc.perform(post("/api/myShoppingCart/addProducts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized()); // 401

        SecurityContextHolder.clearContext();
    }

    @Test
    void addProductsToMyShoppingCart_Returns500_WhenUnexpectedErrorOccurs() throws Exception {
        // Configuración de datos de prueba
        AddProductDTO request = new AddProductDTO();
        request.setProductId(null);
        request.setSize(null);

        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(10.0f);

        User user = new User();
        user.setId(1L); // Asignar ID al usuario

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setSize("M");
        cartItem.setQuantity(1);

        List<CartItem> cartItems = new ArrayList<>();
        cartItems.add(cartItem);

        // Configuración de mocks
        when(shoppingCartService.addProducts(eq(user.getId()), eq(product), eq(request.getSize()), eq(1)))
                .thenReturn(cartItems);
        when(productService.findById(eq(request.getProductId()))).thenReturn(product);
        when(userService.getUserById(eq(user.getId()))).thenReturn(user); // Simula la obtención del usuario

        // Simular autenticación en el SecurityContext
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Simulamos que ocurre un error inesperado
        when(shoppingCartService.addProducts(anyLong(), eq(product), eq(request.getSize()), eq(1)))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Ejecución y verificación
        mockMvc.perform(post("/api/myShoppingCart/addProducts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError()); // 500

        SecurityContextHolder.clearContext();
    }

    @Test
    void addProductsToMyShoppingCartNonAuthenticated_Returns404_WhenProductNotFound() throws Exception {
        // Configuración de datos de prueba
        AddProductNonAuthenticatedDTO request = new AddProductNonAuthenticatedDTO();
        request.setProductId(1L);
        request.setSize("M");
        request.setShoppingCart(new ShoppingCart());

        // Configuración de mocks
        when(productService.findById(request.getProductId())).thenReturn(null);

        // Ejecución y verificación
        mockMvc.perform(post("/api/myShoppingCart/addProductsNonAuthenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void addProductsToMyShoppingCartNonAuthenticated_Returns200_WhenProductAddedSuccessfully() throws Exception {
        // Configuración de datos de prueba
        AddProductNonAuthenticatedDTO request = new AddProductNonAuthenticatedDTO();
        request.setProductId(1L);
        request.setSize("M");

        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(10.0f);

        ShoppingCart shoppingCart = new ShoppingCart();
        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setSize("M");
        cartItem.setQuantity(1);

        List<CartItem> cartItems = new ArrayList<>();
        cartItems.add(cartItem);
        shoppingCart.setItems(cartItems);

        request.setShoppingCart(shoppingCart);

        // Configuración de mocks
        when(productService.findById(request.getProductId())).thenReturn(product);
        when(shoppingCartService.addProductsNonAuthenticated(
                any(), any(), any(), anyInt())).thenReturn(cartItems);

        // Ejecución y verificación
        mockMvc.perform(post("/api/myShoppingCart/addProductsNonAuthenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].product.id").value(product.getId()))
                .andExpect(jsonPath("$[0].product.name").value(product.getName()))
                .andExpect(jsonPath("$[0].product.price").value((double) product.getPrice()))
                .andExpect(jsonPath("$[0].size").value("M"))
                .andExpect(jsonPath("$[0].quantity").value(1));
    }

    @Test
    void addProductsToMyShoppingCartNonAuthenticated_Returns500_OnException() throws Exception {
        // Configuración de datos de prueba
        AddProductNonAuthenticatedDTO request = new AddProductNonAuthenticatedDTO();
        request.setProductId(1L);
        request.setSize("M");
        request.setShoppingCart(new ShoppingCart());

        // Configuración de mocks
        when(productService.findById(request.getProductId())).thenThrow(new RuntimeException("Error"));

        // Ejecución y verificación
        mockMvc.perform(post("/api/myShoppingCart/addProductsNonAuthenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void deleteProductsFromShoppingCart_Returns500_WhenErrorOccursInService() throws Exception {
        // Configuración de datos de prueba
        Long itemId = 1L;

        User user = new User();
        user.setId(1L);

        // Simulación de fallo en el servicio
        when(shoppingCartService.removeProduct(user.getId(), itemId))
                .thenThrow(new RuntimeException("Internal server error"));

        // Simular el contexto de autenticación
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Map<String, Object> payload = new HashMap<>();
        payload.put("itemId", itemId);

        mockMvc.perform(post("/api/myShoppingCart/deleteProducts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isInternalServerError()); // 500
    }

    @Test
    void deleteProductsFromShoppingCartNonAuthenticated_Returns500_WhenErrorOccurs() throws Exception {
        // Configuración de datos de prueba
        AddProductNonAuthenticatedDTO request = new AddProductNonAuthenticatedDTO();
        request.setProductId(null);
        request.setShoppingCart(null);

        // Configuración de mocks para simular un error
        when(shoppingCartService.removeProductNonAuthenticate(
                request.getShoppingCart(), request.getProductId())).thenThrow(new RuntimeException("Error inesperado"));

        // Ejecución y verificación
        mockMvc.perform(post("/api/myShoppingCart/deleteProductsNonAuthenticated")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError()); // 500
    }

}
