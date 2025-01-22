package com.artists_heaven.shoppingCart;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import com.artists_heaven.entities.user.User;
import com.artists_heaven.entities.user.UserService;
import com.artists_heaven.product.Product;

@Service
public class ShoppingCartService {

    private final ShoppingCartRepository shoppingCartRepository;

    private final UserService userService;

    private final CartItemRepository cartItemRepository;

    public ShoppingCartService(ShoppingCartRepository shoppingCartRepository, UserService userService,
            CartItemRepository cartItemRepository) {
        this.shoppingCartRepository = shoppingCartRepository;
        this.userService = userService;
        this.cartItemRepository = cartItemRepository;
    }

    public ShoppingCart getShoppingCart(Long id) {
        User user = userService.getUserById(id);
        return shoppingCartRepository.findShoppingCartByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Error al encontrar el carrito"));
    }

    public List<CartItem> addProducts(Long id, Product product, String size, int quantity) {
        User user = userService.getUserById(id);

        ShoppingCart shoppingCart = getOrCreateShoppingCart(user.getId());

        Optional<CartItem> existingItem = shoppingCart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()) && item.getSize().equals(size))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem();
            newItem.setProduct(product);
            newItem.setSize(size);
            newItem.setQuantity(quantity);
            newItem.setShoppingCart(shoppingCart);
            shoppingCart.getItems().add(newItem);
            cartItemRepository.save(newItem);
        }

        return shoppingCart.getItems();
    }

    private ShoppingCart getOrCreateShoppingCart(Long userId) {
        User user = userService.getUserById(userId);

        return shoppingCartRepository.findShoppingCartByUserId(user.getId())
                .orElseGet(() -> {
                    ShoppingCart newCart = new ShoppingCart();
                    newCart.setUser(user);
                    newCart.setItems(new ArrayList<>());
                    shoppingCartRepository.save(newCart);
                    return newCart;
                });
    }

    public List<CartItem> addProductsNonAuthenticated(ShoppingCart shoppingCart, Product product, String size,
            int quantity) {
        if (shoppingCart.getItems() == null) {
            shoppingCart.setItems(new ArrayList<>());
        }

        // Buscar si el item ya existe en el carrito
        Optional<CartItem> existingItem = shoppingCart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()) && item.getSize().equals(size))
                .findFirst();

        if (existingItem.isPresent()) {
            // Actualizar la cantidad si el item ya existe
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepository.save(item);
        } else {
            // Crear un nuevo item si no existe
            CartItem newItem = new CartItem();
            newItem.setProduct(product);
            newItem.setSize(size);
            newItem.setQuantity(quantity);
            shoppingCart.getItems().add(newItem);
        }

        return shoppingCart.getItems();
    }

    public List<CartItem> removeProduct(Long userId, Long itemId) {
        // Obtener el carrito de compras del usuario
        ShoppingCart shoppingCart = getShoppingCart(userId);
        
        // Buscar el CartItem a modificar o eliminar
        CartItem toModify = shoppingCart.getItems().get(itemId.intValue());    
        if (toModify.getQuantity() == 1) {
            // Si la cantidad es 1, elimina el CartItem completamente
            shoppingCart.getItems().remove(toModify); // Eliminar de la lista
            cartItemRepository.delete(toModify);     // Eliminar de la base de datos
        } else {
            // Si hay más de una cantidad, reducir la cantidad
            toModify.setQuantity(toModify.getQuantity() - 1);
            cartItemRepository.save(toModify);       // Actualizar en la base de datos
        }
    
        // Guardar el carrito de compras actualizado
        shoppingCartRepository.save(shoppingCart);
    
        // Retornar la lista actualizada de items en el carrito
        return shoppingCart.getItems();
    }

    public List<CartItem> removeProductNonAuthenticate(ShoppingCart shoppingCart, Long itemId) {
        List<CartItem> shoppingCartItems = shoppingCart.getItems();

        CartItem toModify = shoppingCart.getItems().get(itemId.intValue());
        if (toModify.getQuantity() == 1) {
            shoppingCart.getItems().remove(toModify);
        }else{
            toModify.setQuantity(toModify.getQuantity() - 1);
        }  
        return shoppingCartItems;
    }
}
