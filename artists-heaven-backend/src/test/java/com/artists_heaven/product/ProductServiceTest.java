package com.artists_heaven.product;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

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
    public void registerProductTest() throws Exception {
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

        assertEquals("No se ha podido crear el producto", exception.getMessage());
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
        List<Product> products = new ArrayList<>();
        when(productRepository.findAll()).thenReturn(products);

        List<Product> result = productService.getAllProducts();

        assertNotNull(result);
        verify(productRepository, times(1)).findAll();
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

        Optional<Product> result = productService.findById(productId);

        assertTrue(result.isPresent());
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
            assertEquals("Error al eliminar las imágenes.", exception.getMessage());

            // Verifica que Files.delete fue llamado correctamente
            mockedFiles.verify(() -> Files.delete(mockPath), times(1));
        }
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
            assertTrue(result.get(0).contains("/product_media/test.jpg"));

        }
    }

}
