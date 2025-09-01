package com.artists_heaven.product;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.artists_heaven.admin.CategoryDTO;
import com.artists_heaven.admin.CollectionDTO;
import com.artists_heaven.exception.AppExceptions;
import com.artists_heaven.images.ImageServingUtil;
import com.artists_heaven.page.PageResponse;
import com.artists_heaven.standardResponse.StandardResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.context.MessageSource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/product")
public class ProductController {

        private final ProductService productService;

        private final ImageServingUtil imageServingUtil;

        private final MessageSource messageSource;

        public ProductController(ProductService productService, ImageServingUtil imageServingUtil,
                        MessageSource messageSource) {
                this.productService = productService;
                this.imageServingUtil = imageServingUtil;
                this.messageSource = messageSource;
        }

        @GetMapping("/allProducts")
        @Operation(summary = "Retrieve list of products", description = "Returns a list of products. Supports optional search by product name or description. "
                        +
                        "If size = -1, returns all matching products.")
        @ApiResponse(responseCode = "200", description = "Successfully retrieved products list", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        public ResponseEntity<StandardResponse<PageResponse<ProductDTO>>> getAllProducts(
                        @Parameter(description = "Page number to retrieve (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,

                        @Parameter(description = "Number of products per page. Use -1 to retrieve all products", example = "6") @RequestParam(defaultValue = "6") int size,

                        @Parameter(description = "Optional search keyword to filter products by name or description", example = "laptop", required = false) @RequestParam(required = false) String search) {

                PageResponse<ProductDTO> products = productService.getProducts(page, size, search);

                return ResponseEntity.ok(
                                new StandardResponse<>("Products retrieved successfully", products,
                                                HttpStatus.OK.value()));
        }

        @GetMapping("/product_media/{fileName:.+}")
        @Operation(summary = "Retrieve product image by file name", description = "Returns the product image file corresponding to the given file name. Supports serving PNG images stored in the product_media directory.")
        @ApiResponse(responseCode = "200", description = "Image file successfully retrieved", content = @Content(mediaType = "image/png"))
        @ApiResponse(responseCode = "404", description = "Image file not found", content = @Content)
        public ResponseEntity<Resource> getProductImage(
                        @Parameter(description = "File name including extension", required = true) @PathVariable String fileName) {
                String basePath = System.getProperty("user.dir")
                                + "/artists-heaven-backend/src/main/resources/product_media/";
                return imageServingUtil.serveImage(basePath, fileName);
        }

        @GetMapping("/categories")
        @Operation(summary = "Retrieve all product categories", description = "Returns a set of all available product categories.")
        @ApiResponse(responseCode = "200", description = "Successfully retrieved categories", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        public ResponseEntity<StandardResponse<Set<CategoryDTO>>> getAllCategories() {
                Set<Category> categories = productService.getAllCategories();
                Set<CategoryDTO> categoriesDTO = categories.stream()
                                .map(c -> new CategoryDTO(c.getId(), c.getName()))
                                .collect(Collectors.toSet());
                return ResponseEntity.ok(
                                new StandardResponse<>("Categories retrieved successfully", categoriesDTO,
                                                HttpStatus.OK.value()));
        }

        @PostMapping("/new")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Create a new product", description = "Creates a new product with provided product details and associated images. "
                        +
                        "The images are saved, their URLs are linked to the product, and the product is registered.")

        @ApiResponse(responseCode = "201", description = "Product created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @ApiResponse(responseCode = "400", description = "Bad request - invalid product data or images", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))

        public ResponseEntity<StandardResponse<Product>> newProduct(
                        @Parameter(description = "Product data to create", required = true) @RequestPart("product") @Valid ProductDTO productDTO,

                        @Parameter(description = "List of product images", required = true, content = @Content(mediaType = "image/*")) @RequestPart("images") List<MultipartFile> images,
                        @Parameter(description = "3D model file", required = false, content = @Content(mediaType = "model/*")) @RequestPart(value = "model", required = false) MultipartFile modelFile) {

                if (images == null || images.isEmpty()) {
                        throw new AppExceptions.InvalidInputException("At least one image is required.");
                }

                List<String> imageUrls = productService.saveImages(images);
                productDTO.setImages(imageUrls);

                if (modelFile != null && !modelFile.isEmpty()) {
                        String modelUrl = productService.saveModel(modelFile); // método que debes implementar
                        productDTO.setModelReference(modelUrl);
                }

                Product newProduct = productService.registerProduct(productDTO);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(new StandardResponse<>("Product created successfully", newProduct,
                                                HttpStatus.CREATED.value()));
        }

        // Endpoint to retrieve details of a specific product by its ID
        @GetMapping("/details/{id}")
        @Operation(summary = "Retrieve product details by ID", description = "Returns detailed information of a specific product identified by its ID.")

        @ApiResponse(responseCode = "200", description = "Product found and returned successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))

        public ResponseEntity<StandardResponse<ProductDTO>> productDetails(
                        @Parameter(description = "ID of the product to retrieve", required = true) @PathVariable("id") Long id) {

                Product product = productService.findById(id);
                ProductDTO productDTO = new ProductDTO(product);

                return ResponseEntity.ok(
                                new StandardResponse<>("Product retrieved successfully", productDTO,
                                                HttpStatus.OK.value()));
        }

        @PutMapping("/edit/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Update an existing product by ID", description = "Updates the product data, optionally adding new images, removing specified images, and reordering images.")

        @ApiResponse(responseCode = "200", description = "Product updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @ApiResponse(responseCode = "400", description = "Bad request - invalid update data", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        public ResponseEntity<StandardResponse<ProductDTO>> updateProduct(
                        @Parameter(description = "ID of the product to update", required = true) @PathVariable("id") Long id,

                        @Parameter(description = "Product data to update", required = true) @RequestPart("product") @Valid ProductDTO productDTO,

                        @Parameter(description = "List of new images to add", required = false, content = @Content(mediaType = "image/*")) @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages,

                        @Parameter(description = "List of images to remove", required = false, content = @Content(mediaType = "image/*")) @RequestPart(value = "removedImages", required = false) List<MultipartFile> removedImages,

                        @Parameter(description = "Ordered list of image URLs/names", required = false, content = @Content(mediaType = "application/json")) @RequestPart(value = "reorderedImages", required = false) List<String> reorderedImages,
                        @Parameter(description = "3D model file", required = false, content = @Content(mediaType = "model/*")) @RequestPart(value = "model", required = false) MultipartFile modelFile) {

                productService.updateProduct(id, removedImages, newImages, productDTO,
                                reorderedImages, modelFile);

                return ResponseEntity.ok(
                                new StandardResponse<>("Product updated successfully", null, HttpStatus.OK.value()));
        }

        @PutMapping("/promote/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Promote a product by applying a discount", description = "Allows an admin user to promote a product by setting a discount percentage.")

        @ApiResponse(responseCode = "200", description = "Product promoted successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @ApiResponse(responseCode = "400", description = "Bad request - invalid input data", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @ApiResponse(responseCode = "401", description = "Unauthorized - authentication is required", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @ApiResponse(responseCode = "403", description = "Forbidden - only admins can promote products", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))

        public ResponseEntity<StandardResponse<ProductDTO>> promoteProduct(
                        @Parameter(description = "ID of the product to promote", required = true) @PathVariable Long id,

                        @Parameter(description = "Promotion details containing discount", required = true) @RequestBody PromoteDTO promoteDTO) {

                productService.promoteProduct(id, promoteDTO.getDiscount());

                return ResponseEntity.ok(
                                new StandardResponse<>("Product promoted successfully", null, HttpStatus.OK.value()));
        }

        @PutMapping("/demote/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Remove promotion from a product", description = "Allows an admin user to remove the promotion (discount) from a specific product.")

        @ApiResponse(responseCode = "200", description = "Product demoted successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @ApiResponse(responseCode = "400", description = "Bad request - invalid input data", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @ApiResponse(responseCode = "401", description = "Unauthorized - authentication is required", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @ApiResponse(responseCode = "403", description = "Forbidden - only admins can demote products", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))

        public ResponseEntity<StandardResponse<ProductDTO>> demoteProduct(
                        @Parameter(description = "ID of the product to demote", required = true) @PathVariable Long id,
                        String lang) { // <- importante: inyectar la Locale actual

                productService.demoteProduct(id);
                Locale locale = new Locale(lang);
                String message = messageSource.getMessage("promoted.message.successful", null, locale);

                return ResponseEntity.ok(
                                new StandardResponse<>(message, null, HttpStatus.OK.value()));
        }

        @GetMapping("sorted12Product")
        @Operation(summary = "Get 12 products sorted by name")
        public ResponseEntity<StandardResponse<List<ProductDTO>>> getSorted12Products() {
                List<Product> product12 = productService.get12ProductsSortedByName();
                return buildProductResponse(product12, "Retrieved 12 sorted products successfully");
        }

        @GetMapping("tshirt")
        @Operation(summary = "Get all t-shirts")
        public ResponseEntity<StandardResponse<List<ProductDTO>>> getTshirts() {
                List<Product> products = productService.findTshirtsProduct();
                return buildProductResponse(products, "Retrieved t-shirts successfully");
        }

        @GetMapping("pants")
        @Operation(summary = "Get all pants")
        public ResponseEntity<StandardResponse<List<ProductDTO>>> getPants() {
                List<Product> products = productService.findPantsProduct();
                return buildProductResponse(products, "Retrieved pants successfully");
        }

        @GetMapping("hoodies")
        @Operation(summary = "Get all hoodies")
        public ResponseEntity<StandardResponse<List<ProductDTO>>> getHoodies() {
                List<Product> products = productService.findHoodiesProduct();
                return buildProductResponse(products, "Retrieved hoodies successfully");
        }

        @GetMapping("accessories")
        @Operation(summary = "Get all accessories")
        public ResponseEntity<StandardResponse<List<ProductDTO>>> getAccessories() {
                List<Product> products = productService.findAccessoriesProduct();
                return buildProductResponse(products, "Retrieved accessories successfully");
        }

        private ResponseEntity<StandardResponse<List<ProductDTO>>> buildProductResponse(List<Product> products,
                        String message) {
                List<ProductDTO> productDTOs = products.stream()
                                .map(ProductDTO::new)
                                .toList();

                return ResponseEntity.ok(
                                new StandardResponse<>(message, productDTOs, HttpStatus.OK.value()));
        }

        @GetMapping("/related")
        @Operation(summary = "Retrieve related products", description = "Fetches up to 4 related products from the same section, excluding the current product ID.")
        @ApiResponse(responseCode = "200", description = "Related products retrieved successfully", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ProductDTO.class))))
        @ApiResponse(responseCode = "400", description = "Invalid section or ID parameter", content = @Content)
        @ApiResponse(responseCode = "404", description = "No related products found", content = @Content)
        @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
        public ResponseEntity<StandardResponse<List<ProductDTO>>> getRelatedProduct(
                        @Parameter(description = "Section name (e.g. TSHIRT, PANTS)", required = true) @RequestParam String section,
                        @Parameter(description = "ID of the current product to exclude", required = true) @RequestParam Long id) {

                List<Product> relatedProducts = productService.getRelatedProducts(section, id);

                List<ProductDTO> relatedProductDTOs = relatedProducts.stream()
                                .map(ProductDTO::new)
                                .toList();

                return ResponseEntity.ok(
                                new StandardResponse<>("Related products retrieved successfully",
                                                relatedProductDTOs,
                                                HttpStatus.OK.value()));
        }

        @GetMapping("/by-reference")
        @Operation(summary = "Find product by reference", description = "Searches for a product using its unique reference number and returns its ID.")
        @ApiResponse(responseCode = "200", description = "Product found successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Long.class)))
        @ApiResponse(responseCode = "400", description = "Invalid reference parameter", content = @Content)
        @ApiResponse(responseCode = "404", description = "No product found with the given reference", content = @Content)
        @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
        public ResponseEntity<StandardResponse<Long>> getProductByReference(
                        @Parameter(description = "Reference number of the product", required = true) @RequestParam Long reference,
                        @RequestParam String lang) {

                Product product = productService.findByReference(reference, lang);

                return ResponseEntity.ok(
                                new StandardResponse<>(
                                                "Product found successfully",
                                                product.getId(),
                                                HttpStatus.OK.value()));
        }

        @GetMapping("/promoted-collections")
        @Operation(summary = "Get promoted collections", description = "Fetches all collections that are marked as promoted.")
        @ApiResponse(responseCode = "200", description = "Successfully retrieved promoted collections", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CollectionDTO.class))))
        @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content)
        @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
        public ResponseEntity<StandardResponse<List<CollectionDTO>>> getPromotedCollections() {

                List<CollectionDTO> promotedCollections = productService.findAllCollections().stream()
                                .filter(Collection::getIsPromoted)
                                .map(coll -> new CollectionDTO(coll.getId(), coll.getName(), coll.getIsPromoted()))
                                .toList();

                return ResponseEntity.ok(
                                new StandardResponse<>("Promoted collections retrieved successfully",
                                                promotedCollections,
                                                HttpStatus.OK.value()));
        }

        @GetMapping("/collection/{collectionName}")
        @Operation(summary = "Retrieve products by collection", description = "Fetches all products belonging to a specific collection.")
        @ApiResponse(responseCode = "200", description = "Products retrieved successfully", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ProductDTO.class))))
        @ApiResponse(responseCode = "404", description = "No products found for the given collection", content = @Content)
        public ResponseEntity<StandardResponse<List<ProductDTO>>> getProductByCollection(
                        @Parameter(description = "Name of the collection", required = true) @PathVariable String collectionName) {

                List<Product> products = productService.findByCollection(collectionName);

                if (products == null || products.isEmpty()) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(new StandardResponse<>(
                                                        "No products found in collection: " + collectionName,
                                                        null,
                                                        HttpStatus.NOT_FOUND.value()));
                }

                List<ProductDTO> productDTOs = products.stream()
                                .map(ProductDTO::new)
                                .toList();

                return ResponseEntity.ok(
                                new StandardResponse<>(
                                                "Products retrieved successfully from collection: " + collectionName,
                                                productDTOs,
                                                HttpStatus.OK.value()));
        }

}
