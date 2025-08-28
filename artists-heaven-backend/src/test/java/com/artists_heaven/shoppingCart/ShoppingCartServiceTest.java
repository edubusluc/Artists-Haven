package com.artists_heaven.shoppingCart;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
import com.artists_heaven.product.Section;
import com.artists_heaven.shopping_cart.CartItem;
import com.artists_heaven.shopping_cart.CartItemRepository;
import com.artists_heaven.shopping_cart.ShoppingCart;
import com.artists_heaven.shopping_cart.ShoppingCartRepository;
import com.artists_heaven.shopping_cart.ShoppingCartService;

class ShoppingCartServiceTest {

    @Mock
    private ShoppingCartRepository shoppingCartRepository;

    @Mock
    private UserService userService;

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private ShoppingCartService shoppingCartService;

    @BeforeEach
    void setUp() {
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
    void addProductsAccessory_newItemAddedWithNullSize() {
        Long userId = 1L;
        User mockUser = new User();
        mockUser.setId(userId);

        Product accessory = new Product();
        accessory.setId(10L);
        accessory.setSection(Section.ACCESSORIES);

        ShoppingCart cart = new ShoppingCart();
        cart.setUser(mockUser);
        cart.setItems(new ArrayList<>());

        when(userService.getUserById(userId)).thenReturn(mockUser);
        when(shoppingCartRepository.findShoppingCartByUserId(userId)).thenReturn(Optional.of(cart));

        List<CartItem> result = shoppingCartService.addProducts(userId, accessory, null, 2);

        assertEquals(1, result.size());
        CartItem item = result.get(0);
        assertEquals(accessory.getId(), item.getProduct().getId());
        assertNull(item.getSize()); // Tamaño debe ser null para accesorios
        assertEquals(2, item.getQuantity());
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    void addProductsAccessory_existingItemIncreasesQuantity() {
        Long userId = 1L;
        User mockUser = new User();
        mockUser.setId(userId);

        Product accessory = new Product();
        accessory.setId(10L);
        accessory.setSection(Section.ACCESSORIES);

        CartItem existing = new CartItem();
        existing.setProduct(accessory);
        existing.setQuantity(3);

        ShoppingCart cart = new ShoppingCart();
        cart.setUser(mockUser);
        cart.setItems(new ArrayList<>(List.of(existing)));

        when(userService.getUserById(userId)).thenReturn(mockUser);
        when(shoppingCartRepository.findShoppingCartByUserId(userId)).thenReturn(Optional.of(cart));

        List<CartItem> result = shoppingCartService.addProducts(userId, accessory, null, 2);

        assertEquals(1, result.size());
        assertEquals(5, result.get(0).getQuantity()); // Se suma la cantidad
        verify(cartItemRepository, times(1)).save(existing);
    }

    @Test
    void addProductsClothing_existingItemDifferentSize_addsNewItem() {
        Long userId = 1L;
        User mockUser = new User();
        mockUser.setId(userId);

        Product shirt = new Product();
        shirt.setId(20L);
        shirt.setSection(Section.TSHIRT);

        CartItem existing = new CartItem();
        existing.setProduct(shirt);
        existing.setSize("M");
        existing.setQuantity(2);

        ShoppingCart cart = new ShoppingCart();
        cart.setUser(mockUser);
        cart.setItems(new ArrayList<>(List.of(existing)));

        when(userService.getUserById(userId)).thenReturn(mockUser);
        when(shoppingCartRepository.findShoppingCartByUserId(userId)).thenReturn(Optional.of(cart));

        // Agregar misma ropa pero talla diferente
        List<CartItem> result = shoppingCartService.addProducts(userId, shirt, "L", 1);

        assertEquals(2, result.size());
        CartItem newItem = result.stream().filter(i -> "L".equals(i.getSize())).findFirst().orElseThrow();
        assertEquals(1, newItem.getQuantity());
        verify(cartItemRepository, times(1)).save(newItem);
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
    void addProductsNonAuthenticated_cartItemsNull_initializesList() {
        ShoppingCart cart = new ShoppingCart();
        cart.setItems(null);

        Product product = new Product();
        product.setId(1L);
        product.setSection(Section.TSHIRT);

        List<CartItem> items = shoppingCartService.addProductsNonAuthenticated(cart, product, "M", 2);

        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals("M", items.get(0).getSize());
        assertEquals(2, items.get(0).getQuantity());
    }

    @Test
    void addProductsNonAuthenticated_existingAccessory_increasesQuantity() {
        Product product = new Product();
        product.setId(1L);
        product.setSection(Section.ACCESSORIES);

        CartItem existing = new CartItem();
        existing.setProduct(product);
        existing.setQuantity(3);

        ShoppingCart cart = new ShoppingCart();
        cart.setItems(new ArrayList<>(List.of(existing)));

        List<CartItem> items = shoppingCartService.addProductsNonAuthenticated(cart, product, null, 2);

        assertEquals(1, items.size());
        assertEquals(5, items.get(0).getQuantity());
        verify(cartItemRepository, times(1)).save(existing);
    }

    @Test
    void addProductsNonAuthenticated_existingClothing_sameSize_increasesQuantity() {
        Product product = new Product();
        product.setId(1L);
        product.setSection(Section.TSHIRT);

        CartItem existing = new CartItem();
        existing.setProduct(product);
        existing.setSize("M");
        existing.setQuantity(1);

        ShoppingCart cart = new ShoppingCart();
        cart.setItems(new ArrayList<>(List.of(existing)));

        List<CartItem> items = shoppingCartService.addProductsNonAuthenticated(cart, product, "M", 2);

        assertEquals(1, items.size());
        assertEquals(3, items.get(0).getQuantity());
        verify(cartItemRepository, times(1)).save(existing);
    }

    @Test
    void addProductsNonAuthenticated_newAccessory_addedToCart() {
        Product product = new Product();
        product.setId(1L);
        product.setSection(Section.ACCESSORIES);

        ShoppingCart cart = new ShoppingCart();
        cart.setItems(new ArrayList<>());

        List<CartItem> items = shoppingCartService.addProductsNonAuthenticated(cart, product, null, 2);

        assertEquals(1, items.size());
        assertNull(items.get(0).getSize());
        assertEquals(2, items.get(0).getQuantity());
    }

    @Test
    void addProductsNonAuthenticated_newClothing_addedToCart() {
        Product product = new Product();
        product.setId(1L);
        product.setSection(Section.TSHIRT);

        ShoppingCart cart = new ShoppingCart();
        cart.setItems(new ArrayList<>());

        List<CartItem> items = shoppingCartService.addProductsNonAuthenticated(cart, product, "L", 3);

        assertEquals(1, items.size());
        assertEquals("L", items.get(0).getSize());
        assertEquals(3, items.get(0).getQuantity());
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

    @Test
    void testDeleteShoppingCartUserItems() {
        User user = new User();
        user.setId(1L);

        // Crear un carrito con algunos items
        ShoppingCart cart = new ShoppingCart();
        cart.setUser(user);
        CartItem item1 = new CartItem();
        CartItem item2 = new CartItem();
        List<CartItem> items = new ArrayList<>(List.of(item1, item2));
        cart.setItems(items);

        // Mockear getShoppingCart para que devuelva nuestro carrito
        when(userService.getUserById(1l)).thenReturn(user);
        when(shoppingCartRepository.findShoppingCartByUserId(anyLong())).thenReturn(Optional.of(cart));

        // Llamar al método a testear
        shoppingCartService.deleteShoppingCartUserItems(user.getId());

        // Verificar que cada item se eliminó
        verify(cartItemRepository, times(1)).delete(item1);
        verify(cartItemRepository, times(1)).delete(item2);

        // Verificar que el carrito se actualizó con lista vacía
        assertTrue(cart.getItems().isEmpty());

        // Verificar que se guardó el carrito
        verify(shoppingCartRepository, times(1)).save(cart);
    }

}
