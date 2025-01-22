package com.artists_heaven.shoppingCart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.entities.user.UserService;
import com.artists_heaven.product.Product;
import com.artists_heaven.shopping_cart.CartItem;
import com.artists_heaven.shopping_cart.CartItemRepository;
import com.artists_heaven.shopping_cart.ShoppingCart;
import com.artists_heaven.shopping_cart.ShoppingCartRepository;
import com.artists_heaven.shopping_cart.ShoppingCartService;

public class ShoppingCartServiceTest {

    @Mock
    private ShoppingCartRepository shoppingCartRepository;

    @Mock
    private UserService userService;

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private ShoppingCartService shoppingCartService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getShoppingCartTest() {
        Long userId = 1L;
        User mockUser = new User();
        mockUser.setId(userId);

        ShoppingCart mockCart = new ShoppingCart();
        mockCart.setId(1L);

        when(userService.getUserById(userId)).thenReturn(mockUser);
        when(shoppingCartRepository.findShoppingCartByUserId(userId)).thenReturn(Optional.of(mockCart));

        ShoppingCart result = shoppingCartService.getShoppingCart(userId);

        assertNotNull(result);
        assertEquals(mockCart.getId(), result.getId());
        verify(userService, times(1)).getUserById(userId);
        verify(shoppingCartRepository, times(1)).findShoppingCartByUserId(userId);
    }

    @Test
    void getShoppingCartThrowsExceptionWhenCartNotFound() {
        Long userId = 1L;
        User mockUser = new User();
        mockUser.setId(userId);

        when(userService.getUserById(userId)).thenReturn(mockUser);
        when(shoppingCartRepository.findShoppingCartByUserId(userId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            shoppingCartService.getShoppingCart(userId);
        });

        assertEquals("Error al encontrar el carrito", exception.getMessage());
        verify(userService, times(1)).getUserById(userId);
        verify(shoppingCartRepository, times(1)).findShoppingCartByUserId(userId);
    }

    @Test
    void addProductsAddsNewItemToCart() {
        // Datos de prueba
        Long userId = 1L;
        User mockUser = new User();
        mockUser.setId(userId);

        Product mockProduct = new Product();
        mockProduct.setId(1L);

        ShoppingCart mockCart = new ShoppingCart();
        mockCart.setUser(mockUser);
        mockCart.setItems(new ArrayList<>());

        String size = "M";
        int quantity = 2;

        // Simulaciones
        when(userService.getUserById(userId)).thenReturn(mockUser);
        when(shoppingCartRepository.findShoppingCartByUserId(userId)).thenReturn(Optional.of(mockCart));

        // Ejecución
        List<CartItem> result = shoppingCartService.addProducts(userId, mockProduct, size, quantity);

        // Verificaciones
        assertNotNull(result);
        assertEquals(1, result.size());
        CartItem addedItem = result.get(0);
        assertEquals(mockProduct.getId(), addedItem.getProduct().getId());
        assertEquals(size, addedItem.getSize());
        assertEquals(quantity, addedItem.getQuantity());
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    void addProductsUpdatesExistingItemQuantity() {
        // Datos de prueba
        Long userId = 1L;
        User mockUser = new User();
        mockUser.setId(userId);

        Product mockProduct = new Product();
        mockProduct.setId(1L);

        CartItem existingItem = new CartItem();
        existingItem.setProduct(mockProduct);
        existingItem.setSize("M");
        existingItem.setQuantity(2);

        ShoppingCart mockCart = new ShoppingCart();
        mockCart.setUser(mockUser);
        mockCart.setItems(new ArrayList<>(List.of(existingItem)));

        String size = "M";
        int quantity = 3;

        // Simulaciones
        when(userService.getUserById(userId)).thenReturn(mockUser);
        when(shoppingCartRepository.findShoppingCartByUserId(userId)).thenReturn(Optional.of(mockCart));

        // Ejecución
        List<CartItem> result = shoppingCartService.addProducts(userId, mockProduct, size, quantity);

        // Verificaciones
        assertNotNull(result);
        assertEquals(1, result.size());
        CartItem updatedItem = result.get(0);
        assertEquals(mockProduct.getId(), updatedItem.getProduct().getId());
        assertEquals(size, updatedItem.getSize());
        assertEquals(5, updatedItem.getQuantity());
        verify(cartItemRepository, times(1)).save(existingItem);
    }

    @Test
    void addProductsCreatesNewShoppingCartIfNoneExists() {
        // Datos de prueba
        Long userId = 1L;
        User mockUser = new User();
        mockUser.setId(userId);

        Product mockProduct = new Product();
        mockProduct.setId(1L);

        ShoppingCart newCart = new ShoppingCart();
        newCart.setUser(mockUser);
        newCart.setItems(new ArrayList<>());

        String size = "M";
        int quantity = 2;

        // Simulaciones
        when(userService.getUserById(userId)).thenReturn(mockUser);
        when(shoppingCartRepository.findShoppingCartByUserId(userId)).thenReturn(Optional.empty());
        when(shoppingCartRepository.save(any(ShoppingCart.class))).thenReturn(newCart);

        // Ejecución
        List<CartItem> result = shoppingCartService.addProducts(userId, mockProduct, size, quantity);

        // Verificaciones
        assertNotNull(result);
        assertEquals(1, result.size());
        CartItem addedItem = result.get(0);
        assertEquals(mockProduct.getId(), addedItem.getProduct().getId());
        assertEquals(size, addedItem.getSize());
        assertEquals(quantity, addedItem.getQuantity());
        verify(shoppingCartRepository, times(1)).save(any(ShoppingCart.class));
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    void addProductsNonAuthenticatedAddsNewItemToEmptyCart() {
        // Datos de prueba
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setItems(new ArrayList<>());

        Product mockProduct = new Product();
        mockProduct.setId(1L);

        String size = "M";
        int quantity = 2;

        // Ejecución
        List<CartItem> result = shoppingCartService.addProductsNonAuthenticated(shoppingCart, mockProduct, size,
                quantity);

        // Verificaciones
        assertNotNull(result);
        assertEquals(1, result.size());
        CartItem addedItem = result.get(0);
        assertEquals(mockProduct.getId(), addedItem.getProduct().getId());
        assertEquals(size, addedItem.getSize());
        assertEquals(quantity, addedItem.getQuantity());
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void addProductsNonAuthenticatedAddsNewItemToExistingCart() {
        // Datos de prueba
        ShoppingCart shoppingCart = new ShoppingCart();
        CartItem existingItem = new CartItem();
        existingItem.setProduct(new Product());
        existingItem.getProduct().setId(2L);
        shoppingCart.setItems(new ArrayList<>(List.of(existingItem)));

        Product mockProduct = new Product();
        mockProduct.setId(1L);

        String size = "M";
        int quantity = 2;

        // Ejecución
        List<CartItem> result = shoppingCartService.addProductsNonAuthenticated(shoppingCart, mockProduct, size,
                quantity);

        // Verificaciones
        assertNotNull(result);
        assertEquals(2, result.size());
        CartItem addedItem = result.stream().filter(item -> item.getProduct().getId().equals(mockProduct.getId()))
                .findFirst().orElse(null);
        assertNotNull(addedItem);
        assertEquals(mockProduct.getId(), addedItem.getProduct().getId());
        assertEquals(size, addedItem.getSize());
        assertEquals(quantity, addedItem.getQuantity());
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void addProductsNonAuthenticatedUpdatesQuantityOfExistingItem() {
        // Datos de prueba
        ShoppingCart shoppingCart = new ShoppingCart();
        Product mockProduct = new Product();
        mockProduct.setId(1L);

        CartItem existingItem = new CartItem();
        existingItem.setProduct(mockProduct);
        existingItem.setSize("M");
        existingItem.setQuantity(3);
        shoppingCart.setItems(new ArrayList<>(List.of(existingItem)));

        String size = "M";
        int quantity = 2;

        // Ejecución
        List<CartItem> result = shoppingCartService.addProductsNonAuthenticated(shoppingCart, mockProduct, size,
                quantity);

        // Verificaciones
        assertNotNull(result);
        assertEquals(1, result.size());
        CartItem updatedItem = result.get(0);
        assertEquals(5, updatedItem.getQuantity());
        verify(cartItemRepository, times(1)).save(existingItem);
    }

    @Test
    void addProductsNonAuthenticatedHandlesNullItemsList() {
        // Datos de prueba
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setItems(null);

        Product mockProduct = new Product();
        mockProduct.setId(1L);

        String size = "M";
        int quantity = 2;

        // Ejecución
        List<CartItem> result = shoppingCartService.addProductsNonAuthenticated(shoppingCart, mockProduct, size,
                quantity);

        // Verificaciones
        assertNotNull(result);
        assertEquals(1, result.size());
        CartItem addedItem = result.get(0);
        assertEquals(mockProduct.getId(), addedItem.getProduct().getId());
        assertEquals(size, addedItem.getSize());
        assertEquals(quantity, addedItem.getQuantity());
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void addProductsNonAuthenticatedPreservesOtherItemsInCart() {
        // Datos de prueba
        ShoppingCart shoppingCart = new ShoppingCart();
        Product existingProduct = new Product();
        existingProduct.setId(2L);

        CartItem existingItem = new CartItem();
        existingItem.setProduct(existingProduct);
        existingItem.setSize("L");
        existingItem.setQuantity(1);

        shoppingCart.setItems(new ArrayList<>(List.of(existingItem)));

        Product newProduct = new Product();
        newProduct.setId(1L);

        String size = "M";
        int quantity = 2;

        // Ejecución
        List<CartItem> result = shoppingCartService.addProductsNonAuthenticated(shoppingCart, newProduct, size,
                quantity);

        // Verificaciones
        assertNotNull(result);
        assertEquals(2, result.size());
        CartItem addedItem = result.stream().filter(item -> item.getProduct().getId().equals(newProduct.getId()))
                .findFirst().orElse(null);
        assertNotNull(addedItem);
        assertEquals(newProduct.getId(), addedItem.getProduct().getId());
        assertEquals(size, addedItem.getSize());
        assertEquals(quantity, addedItem.getQuantity());

        CartItem preservedItem = result.stream()
                .filter(item -> item.getProduct().getId().equals(existingProduct.getId())).findFirst().orElse(null);
        assertNotNull(preservedItem);
        assertEquals(existingProduct.getId(), preservedItem.getProduct().getId());
        assertEquals("L", preservedItem.getSize());
        assertEquals(1, preservedItem.getQuantity());
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void removeProductRemovesItemWhenQuantityIsOne() {
        // Datos de prueba
        Long userId = 1L;
        Long itemId = 0L;

        // Crear un usuario simulado
        User user = new User();
        user.setId(userId);

        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUser(user);
        CartItem item = new CartItem();
        item.setId(1L);
        item.setQuantity(1);
        shoppingCart.setItems(new ArrayList<>(List.of(item)));

        when(userService.getUserById(userId)).thenReturn(user);
        when(shoppingCartRepository.findShoppingCartByUserId(userId)).thenReturn(java.util.Optional.of(shoppingCart));

        // Ejecución
        List<CartItem> result = shoppingCartService.removeProduct(userId, itemId);

        // Verificaciones
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(cartItemRepository, times(1)).delete(item);
        verify(shoppingCartRepository, times(1)).save(shoppingCart);
    }

    @Test
    void removeProductReducesQuantityWhenGreaterThanOne() {
        // Datos de prueba
        Long userId = 1L;
        Long itemId = 0L;

        // Crear un usuario simulado
        User user = new User();
        user.setId(userId);

        // Crear un carrito de compras con un ítem
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUser(user);
        CartItem item = new CartItem();
        item.setId(1L);
        item.setQuantity(3);
        shoppingCart.setItems(new ArrayList<>(List.of(item)));

        // Configurar los mocks
        when(userService.getUserById(userId)).thenReturn(user);
        when(shoppingCartRepository.findShoppingCartByUserId(userId)).thenReturn(java.util.Optional.of(shoppingCart));

        // Ejecución
        List<CartItem> result = shoppingCartService.removeProduct(userId, itemId);

        // Verificaciones
        assertNotNull(result);
        assertEquals(1, result.size());
        CartItem modifiedItem = result.get(0);
        assertEquals(2, modifiedItem.getQuantity());
        verify(cartItemRepository, times(1)).save(item);
        verify(shoppingCartRepository, times(1)).save(shoppingCart);
    }

    @Test
    void removeProductHandlesInvalidItemId() {
        // Datos de prueba
        Long userId = 1L;
        Long itemId = 10L; // Índice fuera de rango

        User user = new User();
        user.setId(userId);

        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUser(user);
        CartItem item = new CartItem();
        item.setId(1L);
        item.setQuantity(1);
        shoppingCart.setItems(new ArrayList<>(List.of(item)));

        when(userService.getUserById(userId)).thenReturn(user);
        when(shoppingCartRepository.findShoppingCartByUserId(userId)).thenReturn(java.util.Optional.of(shoppingCart));

        // Ejecución y verificación
        assertThrows(IndexOutOfBoundsException.class, () -> shoppingCartService.removeProduct(userId, itemId));
        verify(cartItemRepository, never()).delete(any());
        verify(cartItemRepository, never()).save(any());
        verify(shoppingCartRepository, never()).save(any());
    }

    @Test
    void removeProductHandlesEmptyShoppingCart() {
        // Datos de prueba
        Long userId = 1L;
        Long itemId = 0L;

        User user = new User();
        user.setId(userId);

        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUser(user);
        shoppingCart.setItems(new ArrayList<>());

        when(userService.getUserById(userId)).thenReturn(user);
        when(shoppingCartRepository.findShoppingCartByUserId(userId)).thenReturn(java.util.Optional.of(shoppingCart));

        // Ejecución y verificación
        assertThrows(IndexOutOfBoundsException.class, () -> shoppingCartService.removeProduct(userId, itemId));
        verify(cartItemRepository, never()).delete(any());
        verify(cartItemRepository, never()).save(any());
        verify(shoppingCartRepository, never()).save(any());
    }

    @Test
    void removeProductNonAuthenticateReducesQuantityWhenGreaterThanOne() {
        // Datos de prueba
        Long itemId = 0L;

        ShoppingCart shoppingCart = new ShoppingCart();
        CartItem item = new CartItem();
        item.setId(1L);
        item.setQuantity(3);

        shoppingCart.setItems(new ArrayList<>(List.of(item)));

        // Ejecución
        List<CartItem> result = shoppingCartService.removeProductNonAuthenticate(shoppingCart, itemId);

        // Verificaciones
        assertNotNull(result);
        assertEquals(1, result.size());
        CartItem modifiedItem = result.get(0);
        assertEquals(2, modifiedItem.getQuantity()); // La cantidad se reduce a 2
    }

    @Test
    void removeProductNonAuthenticateRemovesItemWhenQuantityIsOne() {
        // Datos de prueba
        Long itemId = 0L;

        ShoppingCart shoppingCart = new ShoppingCart();
        CartItem item = new CartItem();
        item.setId(1L);
        item.setQuantity(1);

        shoppingCart.setItems(new ArrayList<>(List.of(item)));

        // Ejecución
        List<CartItem> result = shoppingCartService.removeProductNonAuthenticate(shoppingCart, itemId);

        // Verificaciones
        assertNotNull(result);
        assertEquals(0, result.size()); // El item se elimina completamente
    }

}
