package com.artists_heaven.product;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.entities.user.UserRole;
import com.artists_heaven.page.PageResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class ProductControllerTest {

        private MockMvc mockMvc;

        @Mock
        private ProductService productService;

        @InjectMocks
        private ProductController productController;

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);
                mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
                SecurityContextHolder.clearContext();
        }

        // @Test
        // @Transactional
        // void testGetAllProducts() throws Exception {
        // List<Product> products = new ArrayList<>();
        // Product product1 = new Product();
        // product1.setCategories(new HashSet<>());
        // product1.setDescription("Description");
        // product1.setName("Product1");
        // product1.setPrice(100.0f);
        // product1.setSize(new HashMap<>());
        // product1.setImages(new ArrayList<>());
        // product1.setAvailable(true);
        // products.add(product1);

        // Product product2 = new Product();
        // product2.setCategories(new HashSet<>());
        // product2.setDescription("Description");
        // product2.setName("Product2");
        // product2.setPrice(100.0f);
        // product2.setSize(new HashMap<>());
        // product2.setImages(new ArrayList<>());
        // product2.setAvailable(true);

        // when(productService.getAllProducts()).thenReturn(products);

        // mockMvc.perform(get("/api/product/allProducts"))
        // .andExpect(status().isOk())
        // .andExpect(jsonPath("$.length()").value(1))
        // .andExpect(jsonPath("$[0].name").value("Product1"));
        // }

        @Test
        @Transactional
        void testGetProductImage() throws Exception {
                String fileName = "test.png";
                String basePath = System.getProperty("user.dir")
                                + "/artists-heaven-backend/src/main/resources/product_media/";
                Path filePath = Paths.get(basePath, fileName);

                // Create a dummy file for testing
                Files.createDirectories(filePath.getParent());
                Files.createFile(filePath);

                mockMvc.perform(get("/api/product/product_media/{fileName}", fileName))
                                .andExpect(status().isOk())
                                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "image/png"));

                // Clean up the dummy file
                Files.deleteIfExists(filePath);
        }

        @Test
        void testGetProductImageNotFound() throws Exception {
                String fileName = "nonexistent.png";

                mockMvc.perform(get("/api/product/product_media/{fileName}", fileName))
                                .andExpect(status().isNotFound());
        }

        @Test
        @Transactional
        void testGetAllCategories() throws Exception {
                Set<Category> categories = new HashSet<>();
                Category category1 = new Category();
                category1.setId(1L);
                category1.setName("Category1");
                categories.add(category1);

                Category category2 = new Category();
                category2.setId(2L);
                category2.setName("Category2");
                categories.add(category2);

                Set<Category> sortedCategories = categories.stream()
                                .sorted(Comparator.comparing(Category::getName)) // Ordena por nombre o por ID
                                .collect(Collectors.toCollection(LinkedHashSet::new));

                when(productService.getAllCategories()).thenReturn(sortedCategories);

                mockMvc.perform(get("/api/product/categories"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(2))
                                .andExpect(jsonPath("$[0].name").value("Category1"))
                                .andExpect(jsonPath("$[1].name").value("Category2"));
        }

        @Test
        @Transactional
        void testProductDetails() throws Exception {
                Long productId = 1L;
                Product product = new Product();
                product.setId(productId);
                product.setName("Test Product");
                product.setDescription("Test Description");
                product.setPrice(100.0f);

                when(productService.findById(productId)).thenReturn((product));

                mockMvc.perform(get("/api/product/details/{id}", productId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id", is(productId.intValue())))
                                .andExpect(jsonPath("$.name", is("Test Product")))
                                .andExpect(jsonPath("$.description", is("Test Description")))
                                .andExpect(jsonPath("$.price", is(100.0)));
        }

        @Test
        void testProductDetailsNotFound() throws Exception {
                when(productService.findById(1L)).thenThrow(new IllegalArgumentException("Product not found"));

                mockMvc.perform(get("/api/product/details/1"))
                                .andExpect(status().isNotFound())
                                .andExpect(content().string(""));

                verify(productService, times(1)).findById(1L);
        }

        @Test
        @Transactional
        void testDeleteProduct() throws Exception {
                Long productId = 1L;
                Product product = new Product();
                product.setId(productId);

                when(productService.findById(productId)).thenReturn((product));
                doNothing().when(productService).deleteProduct(productId);

                mockMvc.perform(delete("/api/product/delete/{id}", productId))
                                .andExpect(status().isOk());

                verify(productService, times(1)).findById(productId);
                verify(productService, times(1)).deleteProduct(productId);
        }

        @Test
        void testDeleteProductNotFound() throws Exception {
                when(productService.findById(1L)).thenThrow(new IllegalArgumentException("Product not found"));

                mockMvc.perform(delete("/api/product/delete/1"))
                                .andExpect(status().isNotFound())
                                .andExpect(content().string(""));

                verify(productService, times(1)).findById(1L);
                verify(productService, times(0)).deleteProduct(anyLong());
        }

        @Test
        @Transactional
        void testNewProduct() throws Exception {
                ProductDTO productDTO = new ProductDTO();
                productDTO.setName("Test Product");
                productDTO.setDescription("Test Description");
                productDTO.setPrice(100.0f);

                List<MultipartFile> images = new ArrayList<>();
                MockMultipartFile image = new MockMultipartFile("images", "test.jpg", MediaType.IMAGE_JPEG_VALUE,
                                "test image content".getBytes());
                images.add(image);

                List<String> imageUrls = new ArrayList<>();
                imageUrls.add("/product_media/test.jpg");

                Product product = new Product();
                product.setName("Test Product");
                product.setDescription("Test Description");
                product.setPrice(100.0f);
                product.setImages(imageUrls);

                when(productService.saveImages(images)).thenReturn(imageUrls);
                when(productService.registerProduct(any(ProductDTO.class))).thenReturn(product);

                MockMultipartFile productJson = new MockMultipartFile("product", "", "application/json",
                                "{\"name\": \"Test Product\", \"description\": \"Test Description\", \"price\": 100.0}"
                                                .getBytes());

                mockMvc.perform(multipart("/api/product/new")
                                .file(productJson)
                                .file(image))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.name", is("Test Product")))
                                .andExpect(jsonPath("$.description", is("Test Description")))
                                .andExpect(jsonPath("$.price", is(100.0)))
                                .andExpect(jsonPath("$.images[0]", is("/product_media/test.jpg")));

                verify(productService, times(1)).saveImages(images);
                verify(productService, times(1)).registerProduct(any(ProductDTO.class));
        }

        @Test
        @Transactional
        void testNewProductBadRequest() throws Exception {
                List<MultipartFile> images = new ArrayList<>();
                MockMultipartFile image = new MockMultipartFile("images", "test.jpg", MediaType.IMAGE_JPEG_VALUE,
                                "test image content".getBytes());
                images.add(image);

                when(productService.saveImages(images))
                                .thenThrow(new IllegalArgumentException("Error al guardar las imágenes."));

                MockMultipartFile productJson = new MockMultipartFile("product", "", "application/json",
                                "{\"name\": \"Test Product\", \"description\": \"Test Description\", \"price\": 100.0}"
                                                .getBytes());

                mockMvc.perform(multipart("/api/product/new")
                                .file(productJson)
                                .file(image))
                                .andExpect(status().isBadRequest());

                verify(productService, times(1)).saveImages(images);
                verify(productService, times(0)).registerProduct(any(ProductDTO.class));
        }

        @Test
        void testPromoteProduct_Success() throws Exception {
                PromoteDTO promoteDTO = new PromoteDTO();
                promoteDTO.setId(1L);
                promoteDTO.setDiscount(10);

                User user = new User();
                user.setRole(UserRole.ADMIN);

                Authentication authentication = mock(Authentication.class);
                when(authentication.getPrincipal()).thenReturn(user);
                when(authentication.isAuthenticated()).thenReturn(true);

                SecurityContext securityContext = mock(SecurityContext.class);
                when(securityContext.getAuthentication()).thenReturn(authentication);
                SecurityContextHolder.setContext(securityContext);

                doNothing().when(productService).promoteProduct(promoteDTO.getId(), promoteDTO.getDiscount());

                mockMvc.perform(put("/api/product/promote/{id}", promoteDTO.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(new ObjectMapper().writeValueAsString(promoteDTO)))
                                .andExpect(status().isOk())
                                .andExpect(content().string("Product promoted successfully"));

                SecurityContextHolder.clearContext();
        }

        @Test
        void testPromoteProduct_Unauthorized() throws Exception {
                PromoteDTO promoteDTO = new PromoteDTO();
                promoteDTO.setId(1L);
                promoteDTO.setDiscount(10);

                Authentication authentication = mock(Authentication.class);
                when(authentication.getPrincipal()).thenReturn(null);
                SecurityContext securityContext = mock(SecurityContext.class);
                when(securityContext.getAuthentication()).thenReturn(authentication);
                SecurityContextHolder.setContext(securityContext);

                mockMvc.perform(put("/api/product/promote/{id}", promoteDTO.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(new ObjectMapper().writeValueAsString(promoteDTO)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(content().string("Unauthorized"));

                SecurityContextHolder.clearContext();
        }

        @Test
        void testPromoteProduct_Forbidden() throws Exception {
                PromoteDTO promoteDTO = new PromoteDTO();
                promoteDTO.setId(1L);
                promoteDTO.setDiscount(10);

                User user = new User();
                user.setRole(UserRole.USER);

                Authentication authentication = mock(Authentication.class);
                when(authentication.getPrincipal()).thenReturn(user);
                when(authentication.isAuthenticated()).thenReturn(true);
                SecurityContext securityContext = mock(SecurityContext.class);
                when(securityContext.getAuthentication()).thenReturn(authentication);
                SecurityContextHolder.setContext(securityContext);

                mockMvc.perform(put("/api/product/promote/{id}", promoteDTO.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(new ObjectMapper().writeValueAsString(promoteDTO)))
                                .andExpect(status().isForbidden())
                                .andExpect(content().string("Only admins can promote products"));

                SecurityContextHolder.clearContext();
        }

        @Test
        void testPromoteProduct_BadRequest() throws Exception {
                PromoteDTO promoteDTO = new PromoteDTO();
                promoteDTO.setId(1L);
                promoteDTO.setDiscount(10);

                User user = new User();
                user.setRole(UserRole.ADMIN);

                Authentication authentication = mock(Authentication.class);
                when(authentication.getPrincipal()).thenReturn(user);
                when(authentication.isAuthenticated()).thenReturn(true);
                SecurityContext securityContext = mock(SecurityContext.class);
                when(securityContext.getAuthentication()).thenReturn(authentication);
                SecurityContextHolder.setContext(securityContext);

                doThrow(new IllegalArgumentException("Invalid discount")).when(productService)
                                .promoteProduct(promoteDTO.getId(), promoteDTO.getDiscount());

                mockMvc.perform(put("/api/product/promote/{id}", promoteDTO.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(new ObjectMapper().writeValueAsString(promoteDTO)))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().string("Invalid discount"));

                SecurityContextHolder.clearContext();
        }

        @Test
        void testDemoteProduct_Success() throws Exception {
                Long productId = 1L;

                User user = new User();
                user.setRole(UserRole.ADMIN);

                Authentication authentication = mock(Authentication.class);
                when(authentication.getPrincipal()).thenReturn(user);
                when(authentication.isAuthenticated()).thenReturn(true);
                SecurityContext securityContext = mock(SecurityContext.class);
                when(securityContext.getAuthentication()).thenReturn(authentication);
                SecurityContextHolder.setContext(securityContext);

                doNothing().when(productService).demoteProduct(productId);

                mockMvc.perform(put("/api/product/demote/{id}", productId)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(content().string("Product demote successfully"));

                SecurityContextHolder.clearContext();
        }

        @Test
        void testDemoteProduct_Unauthorized() throws Exception {
                Long productId = 1L;

                Authentication authentication = mock(Authentication.class);
                when(authentication.getPrincipal()).thenReturn(null);
                SecurityContext securityContext = mock(SecurityContext.class);
                when(securityContext.getAuthentication()).thenReturn(authentication);
                SecurityContextHolder.setContext(securityContext);

                mockMvc.perform(put("/api/product/demote/{id}", productId)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isUnauthorized())
                                .andExpect(content().string("Unauthorized"));

                SecurityContextHolder.clearContext();
        }

        @Test
        void testDemoteProduct_Forbidden() throws Exception {
                Long productId = 1L;

                User user = new User();
                user.setRole(UserRole.USER);

                Authentication authentication = mock(Authentication.class);
                when(authentication.getPrincipal()).thenReturn(user);
                when(authentication.isAuthenticated()).thenReturn(true);
                SecurityContext securityContext = mock(SecurityContext.class);
                when(securityContext.getAuthentication()).thenReturn(authentication);
                SecurityContextHolder.setContext(securityContext);

                mockMvc.perform(put("/api/product/demote/{id}", productId)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isForbidden())
                                .andExpect(content().string("Only admins can demote products"));

                SecurityContextHolder.clearContext();
        }

        @Test
        void testDemoteProduct_BadRequest() throws Exception {
                Long productId = 1L;

                User user = new User();
                user.setRole(UserRole.ADMIN);

                Authentication authentication = mock(Authentication.class);
                when(authentication.getPrincipal()).thenReturn(user);
                when(authentication.isAuthenticated()).thenReturn(true);
                SecurityContext securityContext = mock(SecurityContext.class);
                when(securityContext.getAuthentication()).thenReturn(authentication);
                SecurityContextHolder.setContext(securityContext);

                doThrow(new IllegalArgumentException("Invalid product ID")).when(productService)
                                .demoteProduct(productId);

                mockMvc.perform(put("/api/product/demote/{id}", productId)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().string("Invalid product ID"));

                SecurityContextHolder.clearContext();
        }

        @Test
        void testGetAllPromotedProduct() throws Exception {
                Product product = new Product();
                product.setName("PRODUCT TEST");
                product.setOn_Promotion(true);
                product.setDiscount(10);
                product.setPrice(90f);

                List<Product> productsPromoted = new ArrayList<>();
                productsPromoted.add(product);

                when(productService.getAllPromotedProducts()).thenReturn(productsPromoted);

                mockMvc.perform(get("/api/product/allPromotedProducts"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.size()").value(1))
                                .andExpect(jsonPath("$[0].name").value("PRODUCT TEST"))
                                .andExpect(jsonPath("$[0].on_Promotion").value(true))
                                .andExpect(jsonPath("$[0].discount").value(10))
                                .andExpect(jsonPath("$[0].price").value(90.0));

        }

        @Test
        void testGetAllProducts_WithSearch() {
                // Arrange
                String search = "Product";
                int page = 0;
                int size = 6;
                Pageable pageable = PageRequest.of(page, size);

                Product product = new Product();
                product.setName("Product 1");

                List<Product> products = List.of(product);
                Page<Product> productPage = new PageImpl<>(products, pageable, products.size());

                when(productService.searchProducts(search, pageable)).thenReturn(productPage);

                // Act
                PageResponse<Product> result = productController.getAllProducts(page, size, search);

                // Assert
                assertNotNull(result);
                assertEquals(1, result.getTotalElements());
                assertEquals("Product 1", result.getContent().get(0).getName());
                verify(productService, times(1)).searchProducts(search, pageable);
                verify(productService, never()).getAllProducts(any(Pageable.class));
        }

        @Test
        void testGetAllProducts_WithoutSearch() {
                // Arrange
                int page = 0;
                int size = 6;
                Pageable pageable = PageRequest.of(page, size);

                Product product = new Product();
                product.setName("Product 1");

                List<Product> products = List.of(product);
                Page<Product> productPage = new PageImpl<>(products, pageable, products.size());

                when(productService.getAllProducts(pageable)).thenReturn(productPage);

                // Act
                PageResponse<Product> result = productController.getAllProducts(page, size, null);

                // Assert
                assertNotNull(result);
                assertEquals(1, result.getTotalElements());
                assertEquals("Product 1", result.getContent().get(0).getName());
                verify(productService, times(1)).getAllProducts(pageable);
                verify(productService, never()).searchProducts(anyString(), any(Pageable.class));
        }

}
