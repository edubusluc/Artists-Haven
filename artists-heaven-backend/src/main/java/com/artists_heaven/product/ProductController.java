package com.artists_heaven.product;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/allProducts")
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/product_media/{fileName:.+}")
    public ResponseEntity<Resource> getProductImage(@PathVariable String fileName) {
        // Obtén el directorio base del proyecto de forma dinámica
        String basePath = System.getProperty("user.dir") + "/artists-heaven-backend/src/main/resources/product_media/";
        Path filePath = Paths.get(basePath, fileName);
        Resource resource = new FileSystemResource(filePath.toFile());
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/png") // Cambia a image/jpeg si tus imágenes son jpeg
                .body(resource);
    }

    @GetMapping("/categories")
    public Set<Category> getAllCategories() {
        return productService.getAllCategories();
    }

    @PostMapping("/new")
    public ResponseEntity<Product> newProduct(
            @RequestPart("product") ProductDTO productDTO,
            @RequestPart("images") List<MultipartFile> images) {
        try {
            // Aquí puedes procesar las imágenes y obtener las URLs o nombres de archivo
            List<String> imageUrls = productService.saveImages(images);

            // Asignar las URLs de las imágenes al DTO
            productDTO.setImages(imageUrls);

            Product newProduct = productService.registerProduct(productDTO);
            return new ResponseEntity<>(newProduct, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/details/{id}")
    public ResponseEntity<Product> productDetails(@PathVariable("id") Long id) {
        Optional<Product> optionalProduct = productService.findById(id);
        if (optionalProduct.isPresent()) {
            return ResponseEntity.ok(optionalProduct.get());
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable("id") Long id) {
        Optional<Product> product = productService.findById(id);
        if (product.isPresent()) {
            productService.deleteProduct(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("edit/{id}")
    public ResponseEntity<String> updateProduct(@PathVariable("id") Long id,
            @RequestPart("product") ProductDTO productDTO,
            @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages,
            @RequestPart(value = "removedImages", required = false) List<MultipartFile> removedImages) {

        Optional<Product> optionalProduct = productService.findById(id);

        if (optionalProduct.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Producto no encontrado");
        }
        try {
            Product product = optionalProduct.get();
            productService.updateProduct(product, removedImages, newImages, productDTO);
            return ResponseEntity.ok("Producto actualizado correctamente");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error al actualizar el producto");
        }
    }
}
