package com.artists_heaven.product;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.artists_heaven.entities.user.User;
import com.artists_heaven.images.ImageServingUtil;
import com.artists_heaven.page.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    private final ProductService productService;

    private final ImageServingUtil imageServingUtil;

    public ProductController(ProductService productService, ImageServingUtil imageServingUtil) {
        this.productService = productService;
        this.imageServingUtil = imageServingUtil;
    }

    @GetMapping("/allProducts")
    @Operation(summary = "Retrieve paginated list of products", description = "Returns a paginated list of products. Supports optional search by product name or description.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated products list", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PageResponse.class)))
    })
    public PageResponse<Product> getAllProducts(
            @Parameter(description = "Page number to retrieve (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of products per page", example = "6") @RequestParam(defaultValue = "6") int size,

            @Parameter(description = "Optional search keyword to filter products by name or description", example = "laptop", required = false) @RequestParam(required = false) String search) {

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Product> result;

        if (search != null && !search.isEmpty()) {
            result = productService.searchProducts(search, pageRequest);
        } else {
            result = productService.getAllProducts(pageRequest);
        }

        return new PageResponse<>(result);
    }

    @GetMapping("/product_media/{fileName:.+}")
    @Operation(summary = "Retrieve product image by file name", description = "Returns the product image file corresponding to the given file name. Supports serving PNG images stored in the product_media directory.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image file successfully retrieved", content = @Content(mediaType = "image/png")),
            @ApiResponse(responseCode = "404", description = "Image file not found", content = @Content)
    })
    public ResponseEntity<Resource> getProductImage(
            @Parameter(description = "File name including extension", required = true) @PathVariable String fileName) {
        String basePath = System.getProperty("user.dir") + "/artists-heaven-backend/src/main/resources/product_media/";
        return imageServingUtil.serveImage(basePath, fileName);
    }

    @GetMapping("/categories")
    @Operation(summary = "Retrieve all product categories", description = "Returns a set of all available product categories.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved categories", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Category.class, type = "array")))
    })
    public Set<Category> getAllCategories() {
        return productService.getAllCategories();
    }

    @PostMapping("/new")
    @Operation(summary = "Create a new product", description = "Creates a new product with provided product details and associated images. The images are saved, their URLs are linked to the product, and the product is registered.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Product created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "400", description = "Bad request - invalid product data or images", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<Product> newProduct(
            @Parameter(description = "Product data to create", required = true) @RequestPart("product") ProductDTO productDTO,

            @Parameter(description = "List of product images", required = true, content = @Content(mediaType = "image/*")) @RequestPart("images") List<MultipartFile> images) {
        try {
            List<String> imageUrls = productService.saveImages(images);
            productDTO.setImages(imageUrls);
            Product newProduct = productService.registerProduct(productDTO);
            return new ResponseEntity<>(newProduct, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    // Endpoint to retrieve details of a specific product by its ID
    @GetMapping("/details/{id}")
    @Operation(summary = "Retrieve product details by ID", description = "Returns detailed information of a specific product identified by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product found and returned successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
    })
    public ResponseEntity<Product> productDetails(
            @Parameter(description = "ID of the product to retrieve", required = true) @PathVariable("id") Long id) {
        try {
            // Attempt to find the product by ID
            Product product = productService.findById(id);
            // Return the product details with a 200 OK status if found
            return ResponseEntity.ok(product);
        } catch (IllegalArgumentException ex) {
            // If the product is not found, return a 404 Not Found status
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null); // Return a null body with a 404 status
        }
    }

    // Endpoint to delete a product by its ID
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Delete a product by ID", description = "Deletes a product identified by its ID if it exists.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product deleted successfully", content = @Content),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
    })
    public ResponseEntity<String> deleteProduct(
            @Parameter(description = "ID of the product to delete", required = true) @PathVariable("id") Long id) {
        try {
            // Attempt to find the product by its ID
            Product product = productService.findById(id);

            // Delete the product if it is found
            productService.deleteProduct(product.getId());

            // Return a 200 OK status if the product is deleted successfully
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            // If the product is not found, return a 404 Not Found status
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    // Endpoint to update an existing product by its ID
    @PutMapping("edit/{id}")
    @Operation(summary = "Update an existing product by ID", description = "Updates the product data, optionally adding new images and removing specified images.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product updated successfully", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "400", description = "Bad request - invalid update data", content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<String> updateProduct(
            @Parameter(description = "ID of the product to update", required = true) @PathVariable("id") Long id,

            @Parameter(description = "Product data to update", required = true) @RequestPart("product") ProductDTO productDTO,

            @Parameter(description = "List of new images to add", required = false, content = @Content(mediaType = "image/*")) @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages,

            @Parameter(description = "List of images to remove", required = false, content = @Content(mediaType = "image/*")) @RequestPart(value = "removedImages", required = false) List<MultipartFile> removedImages) {

        // Find the product by its ID
        Product product = productService.findById(id);

        try {
            // Attempt to update the product with new images, removed images, and updated
            // product data
            productService.updateProduct(product, removedImages, newImages, productDTO);

            // Return a success message if the product is updated correctly
            return ResponseEntity.ok("Product updated successfully");

        } catch (IllegalArgumentException e) {
            // Return an error message if the update fails (e.g., invalid data)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error updating product");
        }
    }

    @PutMapping("promote/{id}")
    @Operation(summary = "Promote a product by applying a discount", description = "Allows an admin user to promote a product by setting a discount percentage.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product promoted successfully", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "400", description = "Bad request - invalid input data", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication is required", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "403", description = "Forbidden - only admins can promote products", content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<String> promoteProduct(
            @Parameter(description = "ID of the product to promote", required = true) @PathVariable Long id,

            @Parameter(description = "Promotion details containing product ID and discount", required = true) @RequestBody PromoteDTO promoteDTO) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        User user = (User) authentication.getPrincipal();
        try {
            if ("ADMIN".equals(user.getRole().toString())) {
                productService.promoteProduct(promoteDTO.getId(), promoteDTO.getDiscount());
                return ResponseEntity.ok("Product promoted successfully");
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only admins can promote products");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("demote/{id}")
    @Operation(summary = "Remove promotion from a product", description = "Allows an admin user to remove the promotion (discount) from a specific product.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product demoted successfully", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "400", description = "Bad request - invalid input data", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication is required", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "403", description = "Forbidden - only admins can demote products", content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<String> demoteProduct(
            @Parameter(description = "ID of the product to demote", required = true) @PathVariable Long id) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        User user = (User) authentication.getPrincipal();
        try {
            if ("ADMIN".equals(user.getRole().toString())) {
                productService.demoteProduct(id);
                return ResponseEntity.ok("Product demote successfully");
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only admins can demote products");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("allPromotedProducts")
    @Operation(summary = "Retrieve all promoted products", description = "Fetches and returns a list of all products that currently have active promotions.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of promoted products", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Product.class))))
    })
    public ResponseEntity<List<Product>> getAllPromotedProducts() {
        // Fetching and returning the list of all promoted products from the product
        // service
        List<Product> promotedProducts = productService.getAllPromotedProducts();
        return ResponseEntity.ok(promotedProducts);
    }

    @GetMapping("sorted12Product")
    public ResponseEntity<List<ProductDTO>> getSorted12Product() {
        try {
            List<Product> product12 = productService.get12ProductsSortedByName();
            List<ProductDTO> product12DTO = product12.stream()
                    .map(this::mapToProductDTO)
                    .toList();
            return ResponseEntity.ok(product12DTO);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }
    }

    private ProductDTO mapToProductDTO(Product product) {
        return new ProductDTO(product);
    }

}
