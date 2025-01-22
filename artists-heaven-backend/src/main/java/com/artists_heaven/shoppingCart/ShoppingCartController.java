package com.artists_heaven.shoppingCart;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.AuthenticationException;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.product.Product;
import com.artists_heaven.product.ProductService;

import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/myShoppingCart")
public class ShoppingCartController {

    private final ShoppingCartService shoppingCartService;

    private final ProductService productService;

    public ShoppingCartController(ShoppingCartService shoppingCartService, ProductService productService) {
        this.shoppingCartService = shoppingCartService;
        this.productService = productService;
    }

    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            throw new BadCredentialsException("User not authenticated");
        }
        return (User) authentication.getPrincipal();
    }

    private CartItemDTO mapToCartItemDTO(CartItem cartItem) {
        CartItemDTO dto = new CartItemDTO();

        // Mapea Product a ProductDTO
        ProductItemDTO productDTO = new ProductItemDTO();
        productDTO.setId(cartItem.getProduct().getId());
        productDTO.setName(cartItem.getProduct().getName());
        productDTO.setPrice(cartItem.getProduct().getPrice());

        // Completa el DTO
        dto.setProduct(productDTO);
        dto.setSize(cartItem.getSize());
        dto.setQuantity(cartItem.getQuantity());

        return dto;
    }

    @GetMapping("")
    public ResponseEntity<ShoppingCartDTO> myShoppingCart() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Object principalUser = authentication.getPrincipal();
            if (principalUser == "anonymousUser") {
                ShoppingCartDTO shoppingCartDTO = new ShoppingCartDTO();
                return ResponseEntity.ok(shoppingCartDTO);
            }
            User user = (User) principalUser;
            ShoppingCart shoppingCart = shoppingCartService.getShoppingCart(user.getId());

            List<CartItem> cartItems = shoppingCart.getItems();
            List<CartItemDTO> cartItemDTOs = cartItems.stream()
                    .map(this::mapToCartItemDTO)
                    .collect(Collectors.toList());

            ShoppingCartDTO shoppingCartDTO = new ShoppingCartDTO();
            shoppingCartDTO.setId(shoppingCart.getId());
            shoppingCartDTO.setItems(cartItemDTOs);
            
            return ResponseEntity.ok(shoppingCartDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/addProducts")
    public ResponseEntity<List<CartItemDTO>> addProductsToMyShoppingCart(@RequestBody AddProductDTO request) {
        try {
            // Obtener el usuario autenticado
            User user = getAuthenticatedUser();

            // Verificar si el producto existe
            Product product = productService.findById(request.getProductId());
            if (product == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404
            }

            // Añadir el producto al carrito
            List<CartItem> cartItems = shoppingCartService.addProducts(user.getId(), product, request.getSize(), 1);

            // Mapear los elementos del carrito a DTO
            List<CartItemDTO> cartItemDTOs = cartItems.stream()
                    .map(this::mapToCartItemDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(cartItemDTOs); // 200
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500
        }
    }

    @PostMapping("/addProductsNonAuthenticate")
    public ResponseEntity<List<CartItemDTO>> addProductsToMyShoppingCartNonAuthenticated(
            @RequestBody AddProductNonAuthenticatedDTO request) {
        try {
            // Validar que el producto existe
            Product product = productService.findById(request.getProductId());
            if (product == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404
            }
    
            // Añadir el producto al carrito
            List<CartItem> cartItems = shoppingCartService.addProductsNonAuthenticated(
                    request.getShoppingCart(), product, request.getSize(), 1);
    
            // Mapear los elementos del carrito a DTO
            List<CartItemDTO> cartItemDTOs = cartItems.stream()
                    .map(this::mapToCartItemDTO)
                    .collect(Collectors.toList());
    
            return ResponseEntity.ok(cartItemDTOs); // 200
        } catch (Exception e) {
            // Log del error para depuración
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500
        }
    }

    @PostMapping("/deleteProducts")
    public ResponseEntity<List<CartItemDTO>> deleteProductsFromShoppingCart(@RequestBody Map<String, Object> payload) {
        try {
            Long itemId = Long.valueOf(payload.get("itemId").toString());
    
            // Obtener usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || authentication.getPrincipal() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401
            }
    
            User user = (User) authentication.getPrincipal();    
            // Eliminar el producto del carrito
            List<CartItem> cartItems = shoppingCartService.removeProduct(user.getId(), itemId);
    
            // Mapear los elementos del carrito a DTO
            List<CartItemDTO> cartItemDTOs = cartItems.stream()
                    .map(this::mapToCartItemDTO)
                    .collect(Collectors.toList());
    
            return ResponseEntity.ok(cartItemDTOs); // 200
        } catch (Exception e) {
            // Log del error para depuración
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500
        }
    }
    

    @PostMapping("/deleteProductsNonAuthenticated")
    public ResponseEntity<List<CartItemDTO>> deleteProductsFromShoppingCartNonAuthenticated(
            @RequestBody AddProductNonAuthenticatedDTO request) {
        try {
            // Eliminar el producto del carrito no autenticado
            List<CartItem> cartItems = shoppingCartService.removeProductNonAuthenticate(
                    request.getShoppingCart(), 
                    request.getProductId()
            );
    
            // Mapear los elementos del carrito a DTO
            List<CartItemDTO> cartItemDTOs = cartItems.stream()
                    .map(this::mapToCartItemDTO)
                    .collect(Collectors.toList());
    
            return ResponseEntity.ok(cartItemDTOs); // 200
        } catch (Exception e) {
            // Log del error para depuración
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500
        }
    }

}
