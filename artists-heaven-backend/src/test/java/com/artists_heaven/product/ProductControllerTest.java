package com.artists_heaven.product;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.artists_heaven.exception.AppExceptions;
import com.artists_heaven.exception.GlobalExceptionHandler;
import com.artists_heaven.images.ImageServingUtil;
import com.artists_heaven.page.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.util.*;

class ProductControllerTest {

        private MockMvc mockMvc;

        @Mock
        private ProductService productService;

        @Mock
        private ImageServingUtil imageServingUtil;

        @Mock
        private MessageSource messageSource;

        @InjectMocks
        private ProductController productController;

        private Product product;
        private ProductDTO productDTO;

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);
                mockMvc = MockMvcBuilders.standaloneSetup(productController)
                                .setControllerAdvice(new GlobalExceptionHandler())
                                .build();

                product = new Product();
                product.setId(1L);
                product.setName("Test Product");

                productDTO = new ProductDTO(product);
        }

        @Test
        void testGetAllProducts_success() throws Exception {
                
                PageResponse<ProductDTO> pageResponse = new PageResponse<>(List.of(productDTO), 1, 0, 1, 1, true);
                when(productService.getProducts(0, 6, null)).thenReturn(pageResponse);

                mockMvc.perform(get("/api/product/allProducts")
                                .param("page", "0")
                                .param("size", "6"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Products retrieved successfully"));
        }

        @Test
        void testGetAllProducts_error() throws Exception {
                when(productService.getProducts(anyInt(), anyInt(), any()))
                                .thenThrow(new RuntimeException("Unexpected error"));

                mockMvc.perform(get("/api/product/allProducts"))
                                .andExpect(status().isInternalServerError());
        }

        @Test
        void testGetProductImage_success() throws Exception {
                Resource image = new ByteArrayResource("fake-image".getBytes(StandardCharsets.UTF_8));
                when(imageServingUtil.serveImage(anyString(), eq("test.png"))).thenReturn(ResponseEntity.ok(image));

                mockMvc.perform(get("/api/product/product_media/test.png"))
                                .andExpect(status().isOk());
        }

        @Test
        void testGetAllCategories_success() throws Exception {
                Category category = new Category();
                category.setName("T-SHIRTS");
                when(productService.getAllCategories()).thenReturn(Set.of(category));

                mockMvc.perform(get("/api/product/categories"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Categories retrieved successfully"));
        }

        @Test
        void testNewProduct_success() throws Exception {
                // Simulamos la parte del producto como JSON con todos los campos obligatorios
                MockMultipartFile productPart = new MockMultipartFile(
                                "product",
                                "",
                                "application/json",
                                ("{" +
                                                "\"name\":\"Product\"," +
                                                "\"price\":129.99," +
                                                "\"section\":\"TSHIRT\"," + 
                                                "\"composition\":\"Cotton\"," +
                                                "\"shippingDetails\":\"Ships in 5 days\"," +
                                                "\"images\":[\"img.png\"]" +
                                                "}").getBytes());

                // Simulamos una imagen
                MockMultipartFile image = new MockMultipartFile(
                                "images", // Debe coincidir con @RequestPart("images")
                                "img.png",
                                "image/png",
                                "fake".getBytes());

                // Mocks de servicio
                when(productService.saveImages(anyList())).thenReturn(List.of("img.png"));
                when(productService.registerProduct(any(ProductDTO.class))).thenReturn(product);

                mockMvc.perform(multipart("/api/product/new")
                                .file(productPart)
                                .file(image)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .characterEncoding("UTF-8"))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.message").value("Product created successfully"));
        }

        @Test
        void testProductDetails_success() throws Exception {
                when(productService.findById(1L)).thenReturn(product);

                mockMvc.perform(get("/api/product/details/1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.id").value(1));
        }

        @Test
        void testProductDetails_notFound() throws Exception {
                when(productService.findById(99L))
                                .thenThrow(new AppExceptions.ResourceNotFoundException("Product not found"));

                mockMvc.perform(get("/api/product/details/99"))
                                .andExpect(status().isNotFound());
        }

        @Test
        void testUpdateProduct_success() throws Exception {
                MockMultipartFile productPart = new MockMultipartFile(
                                "product",
                                "",
                                "application/json",
                                ("{" +
                                                "\"name\":\"Updated\"," +
                                                "\"price\":129.99," +
                                                "\"section\":\"TSHIRT\"," +
                                                "\"composition\":\"Canvas\"," +
                                                "\"shippingDetails\":\"Ships in 5 days\"," +
                                                "\"images\":[\"image1.jpg\"]" +
                                                "}").getBytes());

                mockMvc.perform(multipart("/api/product/edit/1")
                                .file(productPart)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .with(request -> {
                                        request.setMethod("PUT");
                                        return request;
                                }))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Product updated successfully"));

                verify(productService).updateProduct(eq(1L), any(), any(), any(), any());
        }

        @Test
        void testPromoteProduct_success() throws Exception {
                mockMvc.perform(put("/api/product/promote/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"discount\":10}"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Product promoted successfully"));

                verify(productService).promoteProduct(1L, 10);
        }

        @Test
        void testDemoteProduct_success() throws Exception {
                when(messageSource.getMessage(eq("promoted.message.successful"), any(), any()))
                                .thenReturn("Demote successful");

                mockMvc.perform(put("/api/product/demote/1").param("lang", "en"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Demote successful"));
        }

        @Test
        void testGetSorted12Products() throws Exception {
                when(productService.get12ProductsSortedByName()).thenReturn(List.of(product));

                mockMvc.perform(get("/api/product/sorted12Product"))
                                .andExpect(status().isOk());
        }

        @Test
        void testGetTshirts() throws Exception {
                when(productService.findTshirtsProduct()).thenReturn(List.of(product));

                mockMvc.perform(get("/api/product/tshirt"))
                                .andExpect(status().isOk());
        }

        @Test
        void testGetPants() throws Exception {
                when(productService.findPantsProduct()).thenReturn(List.of(product));

                mockMvc.perform(get("/api/product/pants"))
                                .andExpect(status().isOk());
        }

        @Test
        void testGetHoodies() throws Exception {
                when(productService.findHoodiesProduct()).thenReturn(List.of(product));

                mockMvc.perform(get("/api/product/hoodies"))
                                .andExpect(status().isOk());
        }

        @Test
        void testGetAccessories() throws Exception {
                when(productService.findAccessoriesProduct()).thenReturn(List.of(product));

                mockMvc.perform(get("/api/product/accessories"))
                                .andExpect(status().isOk());
        }

        @Test
        void testGetRelatedProducts_success() throws Exception {
                when(productService.getRelatedProducts("TSHIRT", 1L)).thenReturn(List.of(product));

                mockMvc.perform(get("/api/product/related")
                                .param("section", "TSHIRT")
                                .param("id", "1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data[0].id").value(1));
        }

        @Test
        void testGetProductByReference_success() throws Exception {
                when(productService.findByReference(123L, "en")).thenReturn(product);

                mockMvc.perform(get("/api/product/by-reference")
                                .param("reference", "123")
                                .param("lang", "en"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data").value(1));
        }

        @Test
        void testGetPromotedCollections_success() throws Exception {
                Collection collection = new Collection();
                collection.setId(1L);
                collection.setName("Summer");
                collection.setIsPromoted(true);

                when(productService.findAllCollections()).thenReturn(List.of(collection));

                mockMvc.perform(get("/api/product/promoted-collections"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data[0].name").value("Summer"));
        }

        @Test
        void testGetProductByCollection_notFound() throws Exception {
                when(productService.findByCollection("Summer")).thenReturn(Collections.emptyList());

                mockMvc.perform(get("/api/product/collection/Summer"))
                                .andExpect(status().isNotFound());
        }

        @Test
        void testGetProductByCollection_success() throws Exception {
                when(productService.findByCollection("Summer")).thenReturn(List.of(product));

                mockMvc.perform(get("/api/product/collection/Summer"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data[0].id").value(1));
        }
}
