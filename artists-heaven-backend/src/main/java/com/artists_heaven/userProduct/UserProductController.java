package com.artists_heaven.userProduct;

import io.swagger.v3.oas.annotations.Parameter;

import java.util.List;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.standardResponse.StandardResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/user-products")
public class UserProductController {

        private final UserProductService userProductService;

        private final ResourceLoader resourceLoader;

        public UserProductController(UserProductService userProductService, ResourceLoader resourceLoader) {
                this.userProductService = userProductService;
                this.resourceLoader = resourceLoader;

        }

        @PostMapping("/create")
        @Operation(summary = "Create new UserProduct", description = "Creates a new product linked to a user.")
        @ApiResponse(responseCode = "201", description = "Product created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        public ResponseEntity<StandardResponse<UserProduct>> createUserProduct(
                        @RequestPart UserProductDTO userProductDTO,
                        @Parameter(description = "List of product images", required = true, content = @Content(mediaType = "image/*")) @RequestPart("images") List<MultipartFile> images,
                        @AuthenticationPrincipal User user,
                        @RequestParam String lang) {

                List<String> imageUrls = userProductService.saveImages(images);
                userProductDTO.setImages(imageUrls);

                UserProduct created = userProductService.createUserProduct(userProductDTO, user.getId(), lang);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(new StandardResponse<>("UserProduct created successfully", created,
                                                HttpStatus.CREATED.value()));
        }

        @GetMapping("/all")
        @Operation(summary = "Retrieve list of user products", description = "Returns a list of user products. Supports optional search by product name or description. If size = -1, returns all matching products.")
        @ApiResponse(responseCode = "200", description = "Successfully retrieved user products list", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        public ResponseEntity<StandardResponse<List<UserProductDetailsDTO>>> getAllUserProducts(
                        @AuthenticationPrincipal User user) {

                Long userId = (user != null) ? user.getId() : null;
                List<UserProductDetailsDTO> details = userProductService.getAllUserProductDetails(userId);

                return ResponseEntity.ok(
                                new StandardResponse<>("UserProducts retrieved successfully", details,
                                                HttpStatus.OK.value()));
        }

        @GetMapping("/userProduct_media/{fileName:.+}")
        @Operation(summary = "Retrieve product image by file name", description = "Returns the product image file corresponding to the given file name. Supports serving images stored in the userProduct_media directory.")
        @ApiResponse(responseCode = "200", description = "Image file successfully retrieved", content = @Content(mediaType = "image/png"))
        @ApiResponse(responseCode = "404", description = "Image file not found", content = @Content)
        public ResponseEntity<Resource> getUserProductImage(@PathVariable String fileName) {
                try {
                        // 1️⃣ Intentar cargar desde classpath (recursos embebidos)
                        Resource resource = resourceLoader.getResource("classpath:userProduct_media/" + fileName);
                        if (resource.exists() && resource.isReadable()) {
                                return ResponseEntity.ok()
                                                .contentType(MediaTypeFactory.getMediaType(fileName)
                                                                .orElse(MediaType.APPLICATION_OCTET_STREAM))
                                                .body(resource);
                        }

                        // 2️⃣ Intentar cargar desde filesystem (ruta física)
                        Path filePath = Paths.get(System.getProperty("user.dir"), "userProduct_media", fileName)
                                        .normalize();
                        Resource fileResource = new UrlResource(filePath.toUri());
                        if (fileResource.exists() && fileResource.isReadable()) {
                                return ResponseEntity.ok()
                                                .contentType(MediaTypeFactory.getMediaType(fileName)
                                                                .orElse(MediaType.APPLICATION_OCTET_STREAM))
                                                .body(fileResource);
                        }

                        // 3️⃣ No se encontró en ninguna ruta
                        return ResponseEntity.notFound().build();

                } catch (Exception e) {
                        e.printStackTrace(); // Para depuración
                        return ResponseEntity.internalServerError().build();
                }
        }

        @Operation(summary = "Get products created by the authenticated user", description = "Retrieves all products that have been created or uploaded by the currently authenticated user.", security = @SecurityRequirement(name = "bearerAuth"))
        @ApiResponse(responseCode = "200", description = "User products retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @ApiResponse(responseCode = "401", description = "Unauthorized – user must be authenticated", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @GetMapping("myUserProducts")
        public ResponseEntity<StandardResponse<List<UserProductDetailsDTO>>> getMyUserProducts(
                        @AuthenticationPrincipal User user) {
                Long id = user.getId();
                List<UserProductDetailsDTO> myUserProducts = userProductService.findMyUserProducts(id);
                return ResponseEntity.ok(
                                new StandardResponse<>("UserProducts retrieved successfully", myUserProducts,
                                                HttpStatus.OK.value()));

        }

}
