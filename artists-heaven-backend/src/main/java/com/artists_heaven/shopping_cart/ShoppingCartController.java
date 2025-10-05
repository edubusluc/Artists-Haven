package com.artists_heaven.shopping_cart;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.AuthenticationException;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.product.Product;
import com.artists_heaven.product.ProductColor;
import com.artists_heaven.product.ProductService;
import com.artists_heaven.product.Section;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

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

        Long productCartItem = cartItem.getProduct().getId();
        Product product = productService.findById(productCartItem);

        ProductItemDTO productDTO = new ProductItemDTO();
        productDTO.setId(product.getId());
        productDTO.setName(product.getName());
        productDTO.setPrice(product.getPrice());
        productDTO.setSection(product.getSection());

        dto.setProduct(productDTO);
        dto.setSize(cartItem.getSize());
        dto.setQuantity(cartItem.getQuantity());
        dto.setColor(cartItem.getColor());

        String imageUrl = null;
        if (product.getColors() != null && !product.getColors().isEmpty()) {
            ProductColor selectedColor = product.getColors().stream()
                    .filter(c -> c.getColorName().equalsIgnoreCase(cartItem.getColor()))
                    .findFirst()
                    .orElse(null);

            if (selectedColor != null && selectedColor.getImages() != null && !selectedColor.getImages().isEmpty()) {
                imageUrl = selectedColor.getImages().get(0);
            }
        }
        dto.setImageUrl(imageUrl);

        return dto;
    }

    @GetMapping("")
    @Operation(summary = "Get the authenticated user's shopping cart", description = "Retrieves the shopping cart of the currently authenticated user. If the user is anonymous, returns an empty shopping cart.")
    @ApiResponse(responseCode = "200", description = "Shopping cart retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ShoppingCartDTO.class)))
    @ApiResponse(responseCode = "400", description = "Bad request - unable to retrieve shopping cart", content = @Content)
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
                    .toList();

            ShoppingCartDTO shoppingCartDTO = new ShoppingCartDTO();
            shoppingCartDTO.setId(shoppingCart.getId());
            shoppingCartDTO.setItems(cartItemDTOs);

            return ResponseEntity.ok(shoppingCartDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/addProducts")
    @Operation(summary = "Add products to the authenticated user's shopping cart", description = "Adds a specified product in a given size and quantity (default 1) to the authenticated user's shopping cart.")
    @ApiResponse(responseCode = "200", description = "Products added to shopping cart successfully", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CartItemDTO.class))))
    @ApiResponse(responseCode = "401", description = "Unauthorized - user must be authenticated", content = @Content)
    @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    public ResponseEntity<List<CartItemDTO>> addProductsToMyShoppingCart(
            @RequestBody AddProductDTO request) {
        try {
            User user = getAuthenticatedUser();

            Product product = productService.findById(request.getProductId());
            if (product == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            String sizeToUse = Section.ACCESSORIES.equals(product.getSection()) ? null : request.getSize();

            List<CartItem> cartItems = shoppingCartService.addProducts(
                    user.getId(),
                    product,
                    sizeToUse,
                    1,
                    request.getColor());

            List<CartItemDTO> cartItemDTOs = cartItems.stream()
                    .map(this::mapToCartItemDTO)
                    .toList();

            return ResponseEntity.ok(cartItemDTOs);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/addProductsNonAuthenticate")
    @Operation(summary = "Add products to a non-authenticated user's shopping cart", description = "Adds a specified product in a given size and quantity (default 1) to the shopping cart of a non-authenticated user.")
    @ApiResponse(responseCode = "200", description = "Products added to shopping cart successfully", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CartItemDTO.class))))
    @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    public ResponseEntity<List<CartItemDTO>> addProductsToMyShoppingCartNonAuthenticated(
            @Parameter(description = "Request body containing shopping cart data, product ID, and size", required = true) @RequestBody AddProductNonAuthenticatedDTO request) {
        try {
            // Validate that the product exists
            Product product = productService.findById(request.getProductId());
            if (product == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404
            }

            // Add the product to the shopping cart
            List<CartItem> cartItems = shoppingCartService.addProductsNonAuthenticated(
                    request.getShoppingCart(), product, request.getSize(), 1, request.getColor());

            // Map cart items to DTOs
            List<CartItemDTO> cartItemDTOs = cartItems.stream()
                    .map(this::mapToCartItemDTO)
                    .toList();

            return ResponseEntity.ok(cartItemDTOs); // 200
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500
        }
    }

    @PostMapping("/deleteProducts")
    @Operation(summary = "Delete a product from the authenticated user's shopping cart", description = "Removes a product from the authenticated user's shopping cart by item ID and returns the updated cart items.")
    @ApiResponse(responseCode = "200", description = "Product removed from shopping cart successfully", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CartItemDTO.class))))
    @ApiResponse(responseCode = "401", description = "Unauthorized - user must be authenticated", content = @Content)
    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    public ResponseEntity<List<CartItemDTO>> deleteProductsFromShoppingCart(
            @Parameter(description = "Payload containing the itemId to be removed", required = true) @RequestBody Map<String, Object> payload) {
        try {
            Long itemId = Long.valueOf(payload.get("itemId").toString());

            // Get authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || authentication.getPrincipal() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401
            }

            User user = (User) authentication.getPrincipal();
            // Remove the product from the shopping cart
            List<CartItem> cartItems = shoppingCartService.removeProduct(user.getId(), itemId);

            // Map cart items to DTOs
            List<CartItemDTO> cartItemDTOs = cartItems.stream()
                    .map(this::mapToCartItemDTO)
                    .toList();

            return ResponseEntity.ok(cartItemDTOs); // 200
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500
        }
    }

    @PostMapping("/deleteProductsNonAuthenticated")
    @Operation(summary = "Delete a product from a non-authenticated user's shopping cart", description = "Removes a product from the shopping cart of a non-authenticated user and returns the updated cart items.")
    @ApiResponse(responseCode = "200", description = "Product removed from shopping cart successfully", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CartItemDTO.class))))
    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    public ResponseEntity<List<CartItemDTO>> deleteProductsFromShoppingCartNonAuthenticated(
            @Parameter(description = "Request body containing shopping cart data and product ID", required = true) @RequestBody AddProductNonAuthenticatedDTO request) {
        try {
            // Remove the product from the non-authenticated shopping cart
            List<CartItem> cartItems = shoppingCartService.removeProductNonAuthenticate(
                    request.getShoppingCart(),
                    request.getProductId());

            // Map cart items to DTOs
            List<CartItemDTO> cartItemDTOs = cartItems.stream()
                    .map(this::mapToCartItemDTO)
                    .toList();

            return ResponseEntity.ok(cartItemDTOs); // 200
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500
        }
    }

}
