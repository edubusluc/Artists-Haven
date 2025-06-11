package com.artists_heaven.product;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @Transactional
    void registerProductTest() throws Exception {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setCategories(new HashSet<>());
        productDTO.setDescription("Description");
        productDTO.setName("Name");
        productDTO.setPrice(100.0f);
        productDTO.setSizes(new HashMap<>());
        productDTO.setImages(new ArrayList<>());

        Product product = new Product();
        product.setCategories(productDTO.getCategories());
        product.setDescription(productDTO.getDescription());
        product.setName(productDTO.getName());
        product.setPrice(productDTO.getPrice());
        product.setSize(productDTO.getSizes());
        product.setImages(productDTO.getImages());
        product.setAvailable(false);

        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product result = productService.registerProduct(productDTO);

        assertEquals(product, result);
    }

    @Test
    void testRegisterProductThrowsException() {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setCategories(new HashSet<>());
        productDTO.setDescription("Test Description");
        productDTO.setName("Test Name");
        productDTO.setPrice(100.0f);
        productDTO.setSizes(new HashMap<>());
        productDTO.setImages(new ArrayList<>());

        when(productRepository.save(any(Product.class))).thenThrow(new RuntimeException("Database error"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.registerProduct(productDTO);
        });

        assertEquals("Unable to create the product", exception.getMessage());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testGetAllCategories() {
        Set<Category> categories = new HashSet<>();
        when(productRepository.getAllCategories()).thenReturn(categories);

        Set<Category> result = productService.getAllCategories();

        assertNotNull(result);
        verify(productRepository, times(1)).getAllCategories();
    }

    @Test
    void testGetAllProducts() {
        Product product = new Product();
        product.setName("Product 1");

        Product product2 = new Product();
        product.setName("Product 2");

        List<Product> products = new ArrayList<>();
        products.add(product);
        products.add(product2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(products, pageable, products.size());

        when(productRepository.findAllProductsSortByName(pageable)).thenReturn(page);

        Page<Product> result = productService.getAllProducts(pageable);

        assertNotNull(result);
        verify(productRepository, times(1)).findAllProductsSortByName(pageable);
    }

    @Test
    void testDeleteProduct() {
        Long productId = 1L;
        doNothing().when(productRepository).deleteById(productId);

        productService.deleteProduct(productId);

        verify(productRepository, times(1)).deleteById(productId);
    }

    @Test
    void testFindById() {
        Long productId = 1L;
        Optional<Product> product = Optional.of(new Product());
        when(productRepository.findById(productId)).thenReturn(product);

        Product result = productService.findById(productId);

        assertNotNull(result);
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    void testSave() {
        Product product = new Product();
        when(productRepository.save(product)).thenReturn(product);

        productService.save(product);

        verify(productRepository, times(1)).save(product);
    }

    @Test
    void testUpdateProduct() {
        Product product = new Product();
        product.setImages(new ArrayList<>());

        ProductDTO productDTO = new ProductDTO();
        productDTO.setCategories(new HashSet<>());
        productDTO.setDescription("Updated Description");
        productDTO.setName("Updated Name");
        productDTO.setPrice((float) 200.0);
        productDTO.setSizes(new HashMap<>());
        productDTO.setImages(new ArrayList<>());

        List<MultipartFile> newImages = new ArrayList<>();
        List<MultipartFile> removedImages = new ArrayList<>();

        productService.updateProduct(product, removedImages, newImages, productDTO);

        assertEquals(productDTO.getName(), product.getName());
        assertEquals(productDTO.getDescription(), product.getDescription());
        assertEquals(productDTO.getPrice(), product.getPrice());
        assertEquals(productDTO.getSizes(), product.getSize());
        assertEquals(productDTO.getCategories(), product.getCategories());
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void testUpdateProductWithNewImages() throws IOException {
        Product product = new Product();
        product.setImages(new ArrayList<>());

        ProductDTO productDTO = new ProductDTO();
        productDTO.setCategories(new HashSet<>());
        productDTO.setDescription("Updated Description");
        productDTO.setName("Updated Name");
        productDTO.setPrice((float) 200.0);
        productDTO.setSizes(new HashMap<>());
        productDTO.setImages(new ArrayList<>());

        MultipartFile newImage = mock(MultipartFile.class);
        when(newImage.getOriginalFilename()).thenReturn("newImage.jpg");
        when(newImage.getInputStream()).thenReturn(new ByteArrayInputStream("image data".getBytes()));
        List<MultipartFile> newImages = List.of(newImage);

        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.copy(any(InputStream.class), any(Path.class))).thenAnswer(invocation -> null);

            productService.updateProduct(product, null, newImages, productDTO);

            assertEquals(productDTO.getName(), product.getName());
            assertEquals(productDTO.getDescription(), product.getDescription());
            assertEquals(productDTO.getPrice(), product.getPrice());
            assertEquals(productDTO.getSizes(), product.getSize());
            assertEquals(productDTO.getCategories(), product.getCategories());
            verify(productRepository, times(1)).save(product);
        }
    }

    @Test
    void testUpdateProductWithRemovedAndNewImages() throws IOException {
        Product product = new Product();
        product.setImages(new ArrayList<>(List.of("/product_media/image1.jpg", "/product_media/image2.jpg")));

        ProductDTO productDTO = new ProductDTO();
        productDTO.setCategories(new HashSet<>());
        productDTO.setDescription("Updated Description");
        productDTO.setName("Updated Name");
        productDTO.setPrice((float) 200.0);
        productDTO.setSizes(new HashMap<>());
        productDTO.setImages(new ArrayList<>());

        MultipartFile removedImage = mock(MultipartFile.class);
        when(removedImage.getOriginalFilename()).thenReturn("image1.jpg");
        List<MultipartFile> removedImages = List.of(removedImage);

        MultipartFile newImage = mock(MultipartFile.class);
        when(newImage.getOriginalFilename()).thenReturn("newImage.jpg");
        when(newImage.getInputStream()).thenReturn(new ByteArrayInputStream("image data".getBytes()));
        List<MultipartFile> newImages = List.of(newImage);

        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.delete(any(Path.class))).thenAnswer(invocation -> null);
            mockedFiles.when(() -> Files.copy(any(InputStream.class), any(Path.class))).thenAnswer(invocation -> null);

            productService.updateProduct(product, removedImages, newImages, productDTO);

            assertEquals(productDTO.getName(), product.getName());
            assertEquals(productDTO.getDescription(), product.getDescription());
            assertEquals(productDTO.getPrice(), product.getPrice());
            assertEquals(productDTO.getSizes(), product.getSize());
            assertEquals(productDTO.getCategories(), product.getCategories());
            verify(productRepository, times(1)).save(product);
        }
    }

    @Test
    void testDeleteImages() throws IOException {
        // Mock del archivo
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test.jpg");

        // Lista de archivos a eliminar
        List<MultipartFile> files = Collections.singletonList(file);

        // Mock del comportamiento de Files.delete
        Path mockPath = Paths.get("artists-heaven-backend/src/main/resources/product_media", "test.jpg");
        Files.createDirectories(mockPath.getParent()); // Asegura que el directorio existe en los tests
        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.delete(mockPath)).thenAnswer(invocation -> null);

            // Llamada al método a probar
            List<String> result = productService.deleteImages(files);

            // Verificaciones
            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertTrue(result.get(0).contains("test.jpg"));

            // Verifica que Files.delete fue llamado correctamente
            mockedFiles.verify(() -> Files.delete(mockPath), times(1));
        }
    }

    @Test
    void testDeleteImagesThrowsException() {
        // Mock del archivo
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("nonexistent.jpg");

        // Lista de archivos a eliminar
        List<MultipartFile> files = Collections.singletonList(file);

        // Mock del comportamiento de Files.delete para lanzar una excepción
        Path mockPath = Paths.get("artists-heaven-backend/src/main/resources/product_media", "nonexistent.jpg");
        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.delete(mockPath)).thenThrow(new IOException("File not found"));

            // Verifica que se lanza la excepción esperada
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> productService.deleteImages(files));

            // Comprueba el mensaje de la excepción
            assertEquals("Error deleting the images.", exception.getMessage());

            // Verifica que Files.delete fue llamado correctamente
            mockedFiles.verify(() -> Files.delete(mockPath), times(1));
        }
    }

    @Test
    void testDeleteImagesOutsideTargetDirectory() {
        List<MultipartFile> images = new ArrayList<>();
        images.add(new MockMultipartFile("file", "../test.jpg", "image/jpeg", new byte[] { 1, 2, 3, 4 }));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.deleteImages(images);
        });

        assertEquals("Entry is outside of the target directory", exception.getMessage());
    }

    @Test
    void testSaveImagesSuccess() throws IOException {
        // Mock del archivo
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test.jpg");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("image data".getBytes()));

        // Lista de archivos a guardar
        List<MultipartFile> files = Collections.singletonList(file);

        // Mock del comportamiento de Files.copy
        Path mockPath = Paths.get("artists-heaven-backend/src/main/resources/product_media", "test.jpg");
        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.copy(any(Path.class), eq(mockPath))).thenAnswer(invocation -> null);

            // Llamada al método a probar
            List<String> result = productService.saveImages(files);

            // Verificaciones
            assertNotNull(result);
            assertEquals(1, result.size());

        }
    }

    @Test
    void testSaveImagesIOException() {
        List<MultipartFile> images = new ArrayList<>();
        images.add(new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[] { 1, 2, 3, 4 }));

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.copy(any(InputStream.class), any(Path.class)))
                    .thenThrow(new IOException("Test IOException"));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                productService.saveImages(images);
            });

            assertEquals("Error while saving images.", exception.getMessage());
            mockedFiles.verify(() -> Files.copy(any(InputStream.class), any(Path.class)), times(1));
        }
    }

    @Test
    void testPromoteProduct_Success() {
        Long productId = 1L;
        Product product = new Product();
        Integer discount = 10;
        product.setOn_Promotion(false);
        product.setDiscount(0);
        product.setPrice(100f);
        product.setAvailable(true);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        productService.promoteProduct(productId, discount);
        assertTrue(product.getPrice() != 100f);
        assertTrue(product.getDiscount() == 10);
        assertTrue(product.getOn_Promotion());
    }

    @Test
    void testPromoteProduct_DiscountError() {
        Long productId = 1L;
        Product product = new Product();
        Integer discount = 101;
        product.setOn_Promotion(false);
        product.setDiscount(0);
        product.setPrice(100f);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.promoteProduct(productId, discount);
        });

        assertEquals("Discount must be between 0 and 100", exception.getMessage());

    }

    @Test
    void testPromoteProduct_ProductNotFound() {
        Long productId = 1L;
        Integer discount = 10;

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.promoteProduct(productId, discount);
        });

        assertEquals("Product not found", exception.getMessage());
    }

    @Test
    void testPromoteProduct_ProductNotAvailable() {
        Long productId = 1L;
        Product product = new Product();
        Integer discount = 50;
        product.setAvailable(false);
        product.setOn_Promotion(false);
        product.setDiscount(0);
        product.setPrice(100f);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.promoteProduct(productId, discount);
        });

        assertEquals("Product is not available", exception.getMessage());

    }

    @Test
    void testPromoteProduct_AlreadyOnPromotion() {
        Long productId = 1L;
        Product product = new Product();
        Integer discount = 50;
        product.setAvailable(true);
        product.setOn_Promotion(true);
        product.setDiscount(10);
        product.setPrice(100f);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.promoteProduct(productId, discount);
        });

        assertEquals("Product is already on promotion", exception.getMessage());

    }

    @Test
    void testDemoteProduct_Success() {
        Long productId = 1L;
        Product product = new Product();
        product.setOn_Promotion(true);
        product.setDiscount(10);
        product.setPrice(90f);
        product.setAvailable(true);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        productService.demoteProduct(productId);
        assertFalse(product.getOn_Promotion());
        assertEquals(0, product.getDiscount());
        assertEquals(100f, product.getPrice());
    }

    @Test
    void testDemoteProduct_NotAvailabe() {
        Long productId = 1L;
        Product product = new Product();
        product.setAvailable(false);
        product.setOn_Promotion(true);
        product.setDiscount(10);
        product.setPrice(90f);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.demoteProduct(productId);
        });

        assertEquals("Product is not available", exception.getMessage());

    }

    @Test
    void testDemoteProduct_NotInPromotion() {
        Long productId = 1L;
        Product product = new Product();
        product.setAvailable(true);
        product.setOn_Promotion(false);
        product.setDiscount(0);
        product.setPrice(100f);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.demoteProduct(productId);
        });

        assertEquals("Product is not on promotion", exception.getMessage());
    }

    @Test
    void testGetAllPromotedProduct() {
        Product product = new Product();
        product.setName("PRODUCT TEST");
        product.setOn_Promotion(true);
        product.setDiscount(10);
        product.setPrice(90f);

        List<Product> productsPromoted = new ArrayList<>();
        productsPromoted.add(product);

        when(productRepository.findAllByOn_Promotion()).thenReturn(productsPromoted);

        List<Product> finalProductsPromoted = productService.getAllPromotedProducts();

        assertEquals("PRODUCT TEST", finalProductsPromoted.get(0).getName());

    }

    @Test
    void testSearchProducts() {
        Product product = new Product();
        product.setName("Product 1");

        List<Product> products = new ArrayList<>();
        products.add(product);

        Pageable pageable = PageRequest.of(0, 10); 
        Page<Product> page = new PageImpl<>(products, pageable, products.size());

        String searchTerm = "Product";
        when(productRepository.findByName(searchTerm, pageable)).thenReturn(page);


        Page<Product> result = productService.searchProducts(searchTerm, pageable);

        assertNotNull(result);
        verify(productRepository, times(1)).findByName(searchTerm, pageable);
        assertEquals(1, result.getTotalElements());
        assertEquals("Product 1", result.getContent().get(0).getName());
    }

}
