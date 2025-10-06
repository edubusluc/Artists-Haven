package com.artists_heaven.shopping_cart;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import com.artists_heaven.entities.user.User;
import com.artists_heaven.entities.user.UserService;
import com.artists_heaven.product.Product;
import com.artists_heaven.product.Section;

import jakarta.transaction.Transactional;

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

    /**
     * Retrieves the shopping cart for a user by their ID.
     *
     * @param id the ID of the user
     * @return the shopping cart associated with the user
     * @throws IllegalArgumentException if the cart cannot be found
     */
    public ShoppingCart getShoppingCart(Long id) {
        User user = userService.getUserById(id);
        return shoppingCartRepository.findShoppingCartByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Error al encontrar el carrito"));
    }

    /**
     * Adds a product to an authenticated user's shopping cart.
     * If the product already exists in the cart (considering size and color where
     * applicable),
     * its quantity is increased; otherwise, a new CartItem is created.
     *
     * @param id       the ID of the user
     * @param product  the product to add
     * @param size     the size of the product (if applicable)
     * @param quantity the quantity to add
     * @param color    the color of the product
     * @return the updated list of items in the shopping cart
     */
    public List<CartItem> addProducts(Long id, Product product, String size, int quantity, String color) {
        User user = userService.getUserById(id);

        ShoppingCart shoppingCart = getOrCreateShoppingCart(user.getId());

        Optional<CartItem> existingItem;

        if (Section.ACCESSORIES.equals(product.getSection())) {
            existingItem = shoppingCart.getItems().stream()
                    .filter(item -> item.getProduct().getId().equals(product.getId()) && item.getColor().equals(color))
                    .findFirst();
        } else {
            existingItem = shoppingCart.getItems().stream()
                    .filter(item -> item.getProduct().getId().equals(product.getId()) && item.getSize().equals(size)
                            && item.getColor().equals(color))
                    .findFirst();
        }

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem();
            newItem.setProduct(product);
            newItem.setSize(Section.ACCESSORIES.equals(product.getSection()) ? null : size);
            newItem.setQuantity(quantity);
            newItem.setShoppingCart(shoppingCart);
            shoppingCart.getItems().add(newItem);
            newItem.setColor(color);
            cartItemRepository.save(newItem);
        }

        return shoppingCart.getItems();
    }

    /**
     * Retrieves an existing shopping cart for a user or creates a new one if it
     * doesn't exist.
     *
     * @param userId the ID of the user
     * @return the existing or newly created ShoppingCart
     */
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

    /**
     * Adds a product to a non-authenticated user's shopping cart (e.g., session
     * cart).
     * Handles quantity increment if the product already exists.
     *
     * @param shoppingCart the shopping cart to modify
     * @param product      the product to add
     * @param size         the size of the product (if applicable)
     * @param quantity     the quantity to add
     * @param color        the color of the product
     * @return the updated list of items in the shopping cart
     */
    public List<CartItem> addProductsNonAuthenticated(ShoppingCart shoppingCart, Product product, String size,
            int quantity, String color) {
        if (shoppingCart.getItems() == null) {
            shoppingCart.setItems(new ArrayList<>());
        }

        Optional<CartItem> existingItem;

        if (Section.ACCESSORIES.equals(product.getSection())) {
            existingItem = shoppingCart.getItems().stream()
                    .filter(item -> item.getProduct().getId().equals(product.getId()) && item.getColor().equals(color))
                    .findFirst();
        } else {
            existingItem = shoppingCart.getItems().stream()
                    .filter(item -> item.getProduct().getId().equals(product.getId()) && item.getSize().equals(size)
                            && item.getColor().equals(color))
                    .findFirst();
        }

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem();
            newItem.setProduct(product);
            newItem.setSize(Section.ACCESSORIES.equals(product.getSection()) ? null : size);
            newItem.setQuantity(quantity);
            newItem.setColor(color);
            shoppingCart.getItems().add(newItem);
        }

        return shoppingCart.getItems();
    }

    /**
     * Removes a product or reduces its quantity by 1 in an authenticated user's
     * shopping cart.
     * If the quantity reaches 0, the item is completely removed from the cart.
     *
     * @param userId the ID of the user
     * @param itemId the ID of the CartItem to remove
     * @return the updated list of items in the shopping cart
     */
    public List<CartItem> removeProduct(Long userId, Long itemId) {
        ShoppingCart shoppingCart = getShoppingCart(userId);

        CartItem toModify = shoppingCart.getItems().get(itemId.intValue());
        if (toModify.getQuantity() == 1) {
            shoppingCart.getItems().remove(toModify);
            cartItemRepository.delete(toModify);
        } else {
            toModify.setQuantity(toModify.getQuantity() - 1);
            cartItemRepository.save(toModify);
        }

        shoppingCartRepository.save(shoppingCart);

        return shoppingCart.getItems();
    }

    /**
     * Removes a product or reduces its quantity by 1 in a non-authenticated user's
     * shopping cart.
     *
     * @param shoppingCart the shopping cart to modify
     * @param itemId       the ID of the CartItem to remove
     * @return the updated list of items in the shopping cart
     */
    public List<CartItem> removeProductNonAuthenticate(ShoppingCart shoppingCart, Long itemId) {
        List<CartItem> shoppingCartItems = shoppingCart.getItems();

        CartItem toModify = shoppingCart.getItems().get(itemId.intValue());
        if (toModify.getQuantity() == 1) {
            shoppingCart.getItems().remove(toModify);
        } else {
            toModify.setQuantity(toModify.getQuantity() - 1);
        }
        return shoppingCartItems;
    }

    /**
     * Deletes all items in a user's shopping cart.
     *
     * @param userId the ID of the user whose cart should be cleared
     */
    @Transactional
    public void deleteShoppingCartUserItems(Long userId) {
        ShoppingCart shoppingCart = getShoppingCart(userId);
        List<CartItem> items = shoppingCart.getItems();
        for (CartItem item : items) {
            cartItemRepository.delete(item);
        }
        shoppingCart.setItems(new ArrayList<>());
        shoppingCartRepository.save(shoppingCart);

    }
}
