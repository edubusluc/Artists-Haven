package com.artists_heaven.product;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
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

import jakarta.transaction.Transactional;

import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;

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
        }

        @Test
        @Transactional
        void testGetAllProducts() throws Exception {
                List<Product> products = new ArrayList<>();
                Product product1 = new Product();
                product1.setCategories(new HashSet<>());
                product1.setDescription("Description");
                product1.setName("Product1");
                product1.setPrice(100.0f);
                product1.setSize(new HashMap<>());
                product1.setImages(new ArrayList<>());
                product1.setAvailable(true);
                products.add(product1);

                Product product2 = new Product();
                product2.setCategories(new HashSet<>());
                product2.setDescription("Description");
                product2.setName("Product2");
                product2.setPrice(100.0f);
                product2.setSize(new HashMap<>());
                product2.setImages(new ArrayList<>());
                product2.setAvailable(true);

                when(productService.getAllProducts()).thenReturn(products);

                mockMvc.perform(get("/api/product/allProducts"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(1))
                                .andExpect(jsonPath("$[0].name").value("Product1"));
        }

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
                                .andDo(print()) // Esto imprimirá el cuerpo de la respuesta en la consola
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

}
