package com.artists_heaven.product;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.artists_heaven.entities.user.User;

import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.springframework.core.io.FileSystemResource;
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
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // Endpoint to retrieve all products
    @GetMapping("/allProducts")
    public List<Product> getAllProducts() {
        // Fetching and returning the list of all products from the product service
        return productService.getAllProducts();
    }

    // Endpoint to retrieve a product media (image) by its file name
    @GetMapping("/product_media/{fileName:.+}")
    public ResponseEntity<Resource> getProductImage(@PathVariable String fileName) {
        // Constructing the base path to the directory where product media is stored
        String basePath = System.getProperty("user.dir") + "/artists-heaven-backend/src/main/resources/product_media/";

        // Resolving the full file path using the base path and file name
        Path filePath = Paths.get(basePath, fileName);

        // Creating a Resource object to represent the file
        Resource resource = new FileSystemResource(filePath.toFile());

        // Checking if the requested file exists
        if (!resource.exists()) {
            // Returning a "not found" response if the file does not exist
            return ResponseEntity.notFound().build();
        }

        // Returning the image as a response with the correct content type header
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/png") // Setting the content type to PNG
                .body(resource); // Returning the file content
    }

    // Endpoint to retrieve all product categories
    @GetMapping("/categories")
    public Set<Category> getAllCategories() {
        // Fetching and returning the set of all categories from the product service
        return productService.getAllCategories();
    }

    // Endpoint to create a new product
    @PostMapping("/new")
    public ResponseEntity<Product> newProduct(
            @RequestPart("product") ProductDTO productDTO, // Product data to create
            @RequestPart("images") List<MultipartFile> images) { // List of product images

        try {
            // Save the images and retrieve their URLs
            List<String> imageUrls = productService.saveImages(images);

            // Set the image URLs in the product DTO
            productDTO.setImages(imageUrls);

            // Register the new product using the provided data and return the created
            // product in the response
            Product newProduct = productService.registerProduct(productDTO);
            return new ResponseEntity<>(newProduct, HttpStatus.CREATED); // Return with "Created" status
        } catch (IllegalArgumentException e) {
            // If there is an error (e.g., invalid data), return a bad request status with
            // no product
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST); // Return with "Bad Request" status
        }
    }

    // Endpoint to retrieve details of a specific product by its ID
    @GetMapping("/details/{id}")
    public ResponseEntity<Product> productDetails(@PathVariable("id") Long id) {
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
    public ResponseEntity<String> deleteProduct(@PathVariable("id") Long id) {
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
    public ResponseEntity<String> updateProduct(@PathVariable("id") Long id,
            @RequestPart("product") ProductDTO productDTO, // Product data to update
            @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages, // List of new images
            @RequestPart(value = "removedImages", required = false) List<MultipartFile> removedImages) { // List of
                                                                                                         // removed
                                                                                                         // images

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
    public ResponseEntity<String> promoteProduct(@RequestBody PromoteDTO promoteDTO) {
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
    public ResponseEntity<String> demoteProduct(@PathVariable Long id) {
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
    

}
