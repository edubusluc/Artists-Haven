package com.artists_heaven.product;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.artists_heaven.admin.CategoryDTO;
import com.artists_heaven.admin.CollectionDTO;
import com.artists_heaven.exception.AppExceptions;
import com.artists_heaven.exception.AppExceptions.BadRequestException;
import com.artists_heaven.exception.AppExceptions.InvalidInputException;
import com.artists_heaven.exception.AppExceptions.ResourceNotFoundException;
import com.artists_heaven.order.Order;
import com.artists_heaven.order.OrderItem;
import com.artists_heaven.page.PageResponse;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;

import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private MessageSource messageSource;

    @Mock
    private CollectionRepository collectionRepository;

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void cleanUp() throws Exception {
        Path uploadDir = Paths.get("artists-heaven-backend/src/main/resources/product_media/");

        if (Files.exists(uploadDir)) {
            // Borrar todos los archivos dentro de la carpeta
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(uploadDir)) {
                for (Path filePath : stream) {
                    Files.deleteIfExists(filePath);
                }
            }
        }
    }

    @Test
    void registerProductTest() throws Exception {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setCategories(new HashSet<>());
        productDTO.setDescription("Description");
        productDTO.setName("Name");
        productDTO.setPrice(100.0f);
        productDTO.setSizes(new HashMap<>());
        productDTO.setImages(new ArrayList<>());
        productDTO.setSection(Section.TSHIRT);
        productDTO.setAvailableUnits(0);

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
        productDTO.setSection(Section.TSHIRT);
        productDTO.setAvailableUnits(0);

        when(productRepository.save(any(Product.class)))
                .thenThrow(new RuntimeException("Unable to create the product"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productService.registerProduct(productDTO);
        });

        assertEquals("Unable to create the product", exception.getMessage());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void registerProductWithAvailableUnitsTest() {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setCategories(new HashSet<>());
        productDTO.setDescription("Product with units");
        productDTO.setName("Product Name");
        productDTO.setPrice(150.0f);
        productDTO.setSizes(new HashMap<>());
        productDTO.setImages(new ArrayList<>());
        productDTO.setSection(Section.TSHIRT);
        productDTO.setAvailableUnits(10);

        Product product = new Product();
        product.setCategories(productDTO.getCategories());
        product.setDescription(productDTO.getDescription());
        product.setName(productDTO.getName());
        product.setPrice(productDTO.getPrice());
        product.setAvailableUnits(productDTO.getAvailableUnits());
        product.setImages(productDTO.getImages());
        product.setSection(productDTO.getSection());

        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product result = productService.registerProduct(productDTO);

        assertEquals(product, result);
        assertEquals(10, result.getAvailableUnits());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testGetAllCategories() {
        Category category = new Category();
        Set<Category> categories = new HashSet<>(List.of(category));
        when(productRepository.getAllCategories()).thenReturn(categories);

        Set<Category> result = productService.getAllCategories();

        assertNotNull(result);
        verify(productRepository, times(1)).getAllCategories();
    }

    @Test
    void testGetAllCategoriesException() {
        Set<Category> categories = new HashSet<>();
        when(productRepository.getAllCategories()).thenReturn(categories);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> productService.getAllCategories());

        assertNotNull(exception);
        assertTrue(exception.getMessage().equals("No categories found"));
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
        product.setId(1L);
        product.setSize(new HashMap<>());
        product.setCategories(new HashSet<Category>());


        ProductDTO productDTO = new ProductDTO();
        productDTO.setCategories(new HashSet<>());
        productDTO.setDescription("Updated Description");
        productDTO.setName("Updated Name");
        productDTO.setPrice((float) 200.0);
        productDTO.setSizes(new HashMap<>());
        productDTO.setImages(new ArrayList<>());

        MultipartFile model3d = new MockMultipartFile(
                "file",                             
                "test.glb",                        
                "test/glb",                       
                "contenido-de-la-imagen".getBytes(StandardCharsets.UTF_8)  
        );

        List<MultipartFile> newImages = new ArrayList<>();
        List<MultipartFile> removedImages = new ArrayList<>();
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        productService.updateProduct(product.getId(), removedImages, newImages, productDTO, null,model3d);

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
        product.setId(1L);
        product.setSize(new HashMap<>());
        product.setCategories(new HashSet<Category>());

        ProductDTO productDTO = new ProductDTO();
        productDTO.setCategories(new HashSet<>());
        productDTO.setDescription("Updated Description");
        productDTO.setName("Updated Name");
        productDTO.setPrice((float) 200.0);
        productDTO.setSizes(new HashMap<>());
        productDTO.setImages(new ArrayList<>());

        MultipartFile newImage = mock(MultipartFile.class);
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        when(newImage.getOriginalFilename()).thenReturn("newImage.jpg");
        when(newImage.getInputStream()).thenReturn(new ByteArrayInputStream("image data".getBytes()));
        List<MultipartFile> newImages = List.of(newImage);

        MultipartFile model3d = mock(MultipartFile.class);
        when(model3d.getOriginalFilename()).thenReturn("newModel.glb");
        when(model3d.getInputStream()).thenReturn(new ByteArrayInputStream("model data".getBytes()));


        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.copy(any(InputStream.class), any(Path.class))).thenAnswer(invocation -> null);

            productService.updateProduct(product.getId(), null, newImages, productDTO, null, model3d);

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
        product.setId(1L);
        product.setSize(new HashMap<>());
        product.setCategories(new HashSet<Category>());

        ProductDTO productDTO = new ProductDTO();
        productDTO.setCategories(new HashSet<>());
        productDTO.setDescription("Updated Description");
        productDTO.setName("Updated Name");
        productDTO.setPrice((float) 200.0);
        productDTO.setSizes(new HashMap<>());
        productDTO.setImages(new ArrayList<>());

        MultipartFile removedImage = mock(MultipartFile.class);
        when(removedImage.getOriginalFilename()).thenReturn("image1.jpg");
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        List<MultipartFile> removedImages = List.of(removedImage);

        MultipartFile newImage = mock(MultipartFile.class);
        when(newImage.getOriginalFilename()).thenReturn("newImage.jpg");
        when(newImage.getInputStream()).thenReturn(new ByteArrayInputStream("image data".getBytes()));
        List<MultipartFile> newImages = List.of(newImage);

        MultipartFile model3d = mock(MultipartFile.class);
        when(model3d.getOriginalFilename()).thenReturn("newModel.glb");
        when(model3d.getInputStream()).thenReturn(new ByteArrayInputStream("model data".getBytes()));

        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.delete(any(Path.class))).thenAnswer(invocation -> null);
            mockedFiles.when(() -> Files.copy(any(InputStream.class), any(Path.class))).thenAnswer(invocation -> null);

            productService.updateProduct(product.getId(), removedImages, newImages, productDTO, null, model3d);

            assertEquals(productDTO.getName(), product.getName());
            assertEquals(productDTO.getDescription(), product.getDescription());
            assertEquals(productDTO.getPrice(), product.getPrice());
            assertEquals(productDTO.getSizes(), product.getSize());
            assertEquals(productDTO.getCategories(), product.getCategories());
            verify(productRepository, times(1)).save(product);
        }
    }

    @Test
    void testUpdateProduct_productNotFound() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        ProductDTO productDTO = new ProductDTO();
        productDTO.setName("Name");
        productDTO.setPrice(100f);

        MultipartFile model3d = mock(MultipartFile.class);

        assertThrows(AppExceptions.ResourceNotFoundException.class,
                () -> productService.updateProduct(1L, null, null, productDTO, null, model3d));
    }

    @Test
    void testUpdateProduct_emptyName() {
        Product product = new Product();
        product.setId(1L);
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));

        ProductDTO productDTO = new ProductDTO();
        productDTO.setName("   ");
        productDTO.setPrice(100f);

        assertThrows(AppExceptions.InvalidInputException.class,
                () -> productService.updateProduct(1L, null, null, productDTO, null, null));
    }

    @Test
    void testUpdateProduct_invalidPrice() {
        Product product = new Product();
        product.setId(1L);
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));

        ProductDTO productDTO = new ProductDTO();
        productDTO.setName("Valid Name");
        productDTO.setPrice(0f);

        assertThrows(AppExceptions.InvalidInputException.class,
                () -> productService.updateProduct(1L, null, null, productDTO, null, null));
    }

    @Test
    void testUpdateProduct_withReorderedImages() {
        Product product = new Product();
        product.setId(1L);
        product.setImages(new ArrayList<>(List.of("img1.jpg", "img2.jpg", "img3.jpg")));
        product.setSize(new HashMap<>());
        product.setCategories(new HashSet<Category>());
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));

        ProductDTO productDTO = new ProductDTO();
        productDTO.setName("Updated Name");
        productDTO.setPrice(100f);
        productDTO.setCategories(new HashSet<>());
        productDTO.setSizes(new HashMap<>());

        List<String> reorderedImages = List.of("img3.jpg", "img1.jpg"); // img2 se moverá al final

        productService.updateProduct(1L, null, null, productDTO, reorderedImages, null);

        List<String> expectedOrder = List.of("img3.jpg", "img1.jpg", "img2.jpg");
        assertEquals(expectedOrder, product.getImages());
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void testUpdateProduct_collectionNotFound() {
        Product product = new Product();
        product.setId(1L);
        product.setImages(List.of("Images"));
        product.setSize(new HashMap<>());
        product.setCategories(new HashSet<Category>());

        ProductDTO productDTO = new ProductDTO();
        productDTO.setName("Valid Name");
        productDTO.setPrice(100f);
        productDTO.setCollectionId(99L); // ID que no existe
        productDTO.setSizes(new HashMap<>());
        productDTO.setCategories(new HashSet<Category>());

        // Mockear collectionRepository
        when(productRepository.findById(1l)).thenReturn(Optional.of(product));
        when(collectionRepository.findById(99L)).thenReturn(Optional.empty());

        // Llamar al método y verificar que lance la excepción correcta
        assertThrows(AppExceptions.ResourceNotFoundException.class,
                () -> productService.updateProduct(1L, null, null, productDTO, null, null));
    }

    @Test
    void registerProduct_nameNull_throwsException() {
        ProductDTO dto = new ProductDTO();
        dto.setName(null);
        dto.setPrice(10f);

        AppExceptions.InvalidInputException ex = assertThrows(
                AppExceptions.InvalidInputException.class,
                () -> productService.registerProduct(dto));
        assertEquals("Product name is required.", ex.getMessage());
    }

    @Test
    void registerProduct_nameEmpty_throwsException() {
        ProductDTO dto = new ProductDTO();
        dto.setName("   ");
        dto.setPrice(10f);

        AppExceptions.InvalidInputException ex = assertThrows(
                AppExceptions.InvalidInputException.class,
                () -> productService.registerProduct(dto));
        assertEquals("Product name is required.", ex.getMessage());
    }

    @Test
    void registerProduct_priceNull_throwsException() {
        ProductDTO dto = new ProductDTO();
        dto.setName("Test");
        dto.setPrice(null);

        AppExceptions.InvalidInputException ex = assertThrows(
                AppExceptions.InvalidInputException.class,
                () -> productService.registerProduct(dto));
        assertEquals("Product price must be greater than 0.", ex.getMessage());
    }

    @Test
    void registerProduct_priceZeroOrNegative_throwsException() {
        ProductDTO dto = new ProductDTO();
        dto.setName("Test");
        dto.setPrice(0f);

        AppExceptions.InvalidInputException ex = assertThrows(
                AppExceptions.InvalidInputException.class,
                () -> productService.registerProduct(dto));
        assertEquals("Product price must be greater than 0.", ex.getMessage());
    }

    @Test
    void registerProduct_collectionNotFound_throwsException() {
        ProductDTO dto = new ProductDTO();
        dto.setName("Test");
        dto.setPrice(10f);
        dto.setCollectionId(1L);

        when(collectionRepository.findById(1L)).thenReturn(Optional.empty());

        AppExceptions.ResourceNotFoundException ex = assertThrows(
                AppExceptions.ResourceNotFoundException.class,
                () -> productService.registerProduct(dto));
        assertEquals("Collection not found with id: 1", ex.getMessage());
    }

    @Test
    void registerProduct_withAvailableUnits_setsUnits() {
        ProductDTO dto = new ProductDTO();
        dto.setName("Test");
        dto.setPrice(10f);
        dto.setAvailableUnits(5);

        when(productRepository.existsByReference(anyLong())).thenReturn(false);
        when(productRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        Product saved = productService.registerProduct(dto);
        assertEquals(5, saved.getAvailableUnits());
        assertNull(saved.getSize());
    }

    @Test
    void registerProduct_withSizes_setsSize() {
        ProductDTO dto = new ProductDTO();
        dto.setName("Test");
        dto.setPrice(10f);
        dto.setAvailableUnits(0);
        Map<String, Integer> sizes = new HashMap<>();
        sizes.put("M", 2);
        dto.setSizes(sizes);

        when(productRepository.existsByReference(anyLong())).thenReturn(false);
        when(productRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        Product saved = productService.registerProduct(dto);
        assertEquals(sizes, saved.getSize());
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

        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            productService.promoteProduct(productId, discount);
        });

        assertEquals("Discount must be between 0 and 100", exception.getMessage());

    }

    @Test
    void testPromoteProduct_ProductNotFound() {
        Long productId = 1L;
        Integer discount = 10;

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.promoteProduct(productId, discount);
        });

        assertEquals("Product not found with id: 1", exception.getMessage());
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

        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
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

        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
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

        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
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

        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
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

    @Test
    void testGet12ProductsSortedByName() {
        Product product = new Product();
        product.setName("Product 1");

        when(productRepository.find12ProductsSortedByName()).thenReturn(List.of(product));
        List<Product> products = productService.get12ProductsSortedByName();
        assertNotNull(products);
    }

    @Test
    void testFindAllByIds() {
        Product product = new Product();
        product.setName("Product 1");

        List<Product> products = new ArrayList<>();
        when(productRepository.findAllById(Set.of(1L))).thenReturn(products);

        List<Product> result = productService.findAllByIds(Set.of(1L));
        assertNotNull(result);

    }

    @Test
    void testMapProductToProductDTO() {
        Collection collection = new Collection();
        collection.setId(1L);

        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(100.0f);
        product.setAvailable(true);
        product.setOn_Promotion(false);
        product.setDiscount(0);
        product.setSize(new HashMap<>());
        product.setCategories(new HashSet<>());
        product.setImages(new ArrayList<>());
        product.setCollection(collection);

        ProductDTO productDTO = new ProductDTO(product);
        assertNotNull(productDTO);

    }

    @Test
    void testDisableProduct() {
        Product product = new Product();
        product.setId(1L);
        product.setAvailable(true);

        when(productRepository.findById(1l)).thenReturn(Optional.of(product));

        productService.disableProduct(1l);
        assertTrue(product.getAvailable() == false);
    }

    @Test
    void testEnableProduct() {
        Product product = new Product();
        product.setId(1L);
        product.setAvailable(false);

        when(productRepository.findById(1l)).thenReturn(Optional.of(product));

        productService.enableProduct(1l);
        assertTrue(product.getAvailable() == true);
    }

    @Test
    void testEnableProductException() {
        Product product = new Product();
        product.setId(1L);
        product.setAvailable(true);

        when(productRepository.findById(1l)).thenReturn(Optional.of(product));
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> productService.enableProduct(1L));

        assertEquals(exception.getMessage(), "Product is already enabled");
    }

    @Test
    void testFinAllCategories() {
        Category category = new Category();
        List<Category> categories = List.of(category);

        when(categoryRepository.findAll()).thenReturn(categories);
        List<Category> result = productService.findAllCategories();

        assertNotNull(result);
    }

    @Test
    void saveCategory_PositiveTest() {
        // Arrange
        String categoryName = "NewCategory";

        // Simular que la categoría no existe
        when(categoryRepository.existsByName(categoryName)).thenReturn(false);

        // Simular la categoría guardada
        Category savedCategory = new Category();
        savedCategory.setName(categoryName);

        // No es necesario simular el resultado de save() si no se verifica su retorno

        // Act
        productService.saveCategory(categoryName);

        // Assert
        verify(categoryRepository).existsByName(categoryName); // Se verifica existencia
        verify(categoryRepository).save(argThat(category -> category.getName().equals(categoryName))); // Se guarda
                                                                                                       // correctamente
    }

    @Test
    void saveCategoryException() {
        String categoryName = "NewCategory";

        // Simular que la categoría ya existe
        when(categoryRepository.existsByName(categoryName)).thenReturn(true);

        // Ejecutar y verificar que lanza ResponseStatusException
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> productService.saveCategory(categoryName));

        // Comprobar el mensaje de la excepción
        assertEquals("Category with the name 'NewCategory' already exists.", exception.getReason());

        // Verificar que no se haya llamado a save
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void testFindAllCollection() {
        Collection collection = new Collection();
        when(collectionRepository.findAll()).thenReturn(List.of(collection));

        List<Collection> result = productService.findAllCollections();
        assertNotNull(result);
    }

    @Test
    void testEditCategory() {
        // Arrange
        CategoryDTO categoryDTO = new CategoryDTO(1L, "Category Test");
        Category category = new Category();
        category.setId(1L);
        category.setName("OLD_NAME");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        // Act
        productService.editCategory(categoryDTO);

        // Assert
        assertEquals("CategoryTest", category.getName());
        verify(categoryRepository).save(category);
    }

    @Test
    void testEditCategory_CategoryNotFound() {
        // Arrange
        CategoryDTO categoryDTO = new CategoryDTO(1L, "Category Test");

        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            productService.editCategory(categoryDTO);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Category with the id '1' not exists."));
    }

    @Test
    void testFindProductByArtist() {
        Product product = new Product();
        List<Product> products = List.of(product);

        when(productRepository.findProductsByArtistCategory("test")).thenReturn(products);

        List<Product> result = productService.findProductsByArtist("test");
        assertNotNull(result);
    }

    @Test
    void testFindProductByArtistEmptyListWithNullName() {
        Product product = new Product();
        List<Product> products = List.of(product);

        when(productRepository.findProductsByArtistCategory("test")).thenReturn(products);

        List<Product> result = productService.findProductsByArtist(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindProductByArtistEmptyListWithEmptyName() {
        Product product = new Product();
        List<Product> products = List.of(product);

        when(productRepository.findProductsByArtistCategory("test")).thenReturn(products);

        List<Product> result = productService.findProductsByArtist("");
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindAllProducts() {
        List<Product> mockProducts = List.of(new Product(), new Product());
        when(productRepository.findAll()).thenReturn(mockProducts);

        List<Product> result = productService.findAllProducts();

        assertEquals(2, result.size());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void testFindTshirtsProduct() {
        List<Product> mockProducts = List.of(new Product());
        when(productRepository.findBySection(Section.TSHIRT)).thenReturn(mockProducts);

        List<Product> result = productService.findTshirtsProduct();

        assertEquals(1, result.size());
        verify(productRepository, times(1)).findBySection(Section.TSHIRT);
    }

    @Test
    void testFindPantsProduct() {
        List<Product> mockProducts = List.of(new Product());
        when(productRepository.findBySection(Section.PANTS)).thenReturn(mockProducts);

        List<Product> result = productService.findPantsProduct();

        assertEquals(1, result.size());
        verify(productRepository, times(1)).findBySection(Section.PANTS);
    }

    @Test
    void testFindAccessoriesProduct() {
        List<Product> mockProducts = List.of(new Product());
        when(productRepository.findBySection(Section.ACCESSORIES)).thenReturn(mockProducts);

        List<Product> result = productService.findAccessoriesProduct();

        assertEquals(1, result.size());
        verify(productRepository, times(1)).findBySection(Section.ACCESSORIES);
    }

    @Test
    void testAccessoryWithSize_ThrowsException() {
        Product product = new Product();
        product.setSection(Section.ACCESSORIES);

        Map<String, Integer> sizes = new HashMap<>();
        sizes.put("M", 1);
        product.setSize(sizes);

        product.setAvailableUnits(10);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, product::validateProduct);
        assertEquals("Los accesorios no pueden tener tallas asignadas.", exception.getMessage());
    }

    @Test
    void testAccessoryWithNullUnits_ThrowsException() {
        Product product = new Product();
        product.setSection(Section.ACCESSORIES);
        product.setSize(null);

        product.setAvailableUnits(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, product::validateProduct);
        assertEquals("La cantidad de unidades disponibles debe ser mayor o igual a 0 para los accesorios.",
                exception.getMessage());
    }

    @Test
    void validateProduct_accessoryValid_noException() {
        Product product = new Product();
        product.setSection(Section.ACCESSORIES);
        product.setSize(null);
        product.setAvailableUnits(1);

        assertDoesNotThrow(product::validateProduct);
    }

    @Test
    void validateProduct_nonAccessory_anyValues_noException() {
        Map<String, Integer> sizes = Map.of("L", 10);

        Product product = new Product();
        product.setSection(Section.HOODIES);
        product.setSize(sizes);
        product.setAvailableUnits(10);

        assertDoesNotThrow(product::validateProduct);
    }

    @Test
    void testAccessoryWithEmptySize_noException() {
        Product product = new Product();
        product.setSection(Section.ACCESSORIES);
        product.setSize(new HashMap<>()); // tamaño vacío
        product.setAvailableUnits(10);

        assertDoesNotThrow(product::validateProduct); // No debería lanzar excepción
    }

    @Test
    void testAccessoryWithNegativeUnits_ThrowsException() {
        Product product = new Product();
        product.setSection(Section.ACCESSORIES);
        product.setSize(null);
        product.setAvailableUnits(-5); // unidades negativas

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                product::validateProduct);
        assertEquals("La cantidad de unidades disponibles debe ser mayor o igual a 0 para los accesorios.",
                exception.getMessage());
    }

    @Test
    void getTopSellingProduct_HappyPath_ReturnsCorrectProductInfo() {
        // Arrange
        OrderItem item1 = new OrderItem();
        item1.setName("Camisa");
        OrderItem item2 = new OrderItem();
        item2.setName("Pantalon");
        OrderItem item3 = new OrderItem();
        item3.setName("Camiseta");

        Order order1 = new Order();
        order1.setItems(List.of(item1, item2));
        Order order2 = new Order();
        order2.setItems(List.of(item3));

        when(productRepository.getOrders()).thenReturn(List.of(order1, order2));

        Product topProduct = new Product();
        topProduct.setName("Camisa");
        topProduct.setDescription("Camisa de algodón");
        topProduct.setPrice(199.99f);

        when(productRepository.findByNameIgnoreCase("Camisa")).thenReturn(topProduct);

        // Act
        Map<String, String> result = productService.getTopSellingProduct();

        // Assert
        assertEquals("Camisa", result.get("nombre"));
        assertEquals("Camisa de algodón", result.get("descripcion"));

        verify(productRepository, times(1)).getOrders();
        verify(productRepository, times(1)).findByNameIgnoreCase("Camisa");
    }

    @Test
    void getTopSellingProduct_EmptyOrders_ThrowsException() {
        when(productRepository.getOrders()).thenReturn(Collections.emptyList());

        assertThrows(NullPointerException.class, () -> productService.getTopSellingProduct());

        verify(productRepository, times(1)).getOrders();
        verify(productRepository, never()).findByNameIgnoreCase(anyString());
    }

    @Test
    void getTopSellingProduct_ProductNotFound_ThrowsException() {
        OrderItem item1 = new OrderItem();
        item1.setName("Camisa");
        Order order1 = new Order();
        order1.setItems(List.of(item1));

        when(productRepository.getOrders()).thenReturn(List.of(order1));
        when(productRepository.findByNameIgnoreCase("Camisa")).thenReturn(null);

        assertThrows(NullPointerException.class, () -> productService.getTopSellingProduct());

        verify(productRepository, times(1)).getOrders();
        verify(productRepository, times(1)).findByNameIgnoreCase("Camisa");
    }

    @Test
    void getTopSellingProduct_MultipleItems_TakesMostFrequent() {
        // Arrange
        OrderItem item1 = new OrderItem();
        item1.setName("Camisa");
        OrderItem item2 = new OrderItem();
        item2.setName("Pantalon");
        OrderItem item3 = new OrderItem();
        item3.setName("Camiseta");
        OrderItem item4 = new OrderItem();
        item4.setName("Gorra");

        Order order1 = new Order();
        order1.setItems(List.of(item1, item2));
        Order order2 = new Order();
        order2.setItems(List.of(item3, item4));

        when(productRepository.getOrders()).thenReturn(List.of(order1, order2));

        Product topProduct = new Product();
        topProduct.setName("Camisa");
        topProduct.setDescription("Camisa premium");
        topProduct.setPrice(250.0f);

        when(productRepository.findByNameIgnoreCase("Camisa")).thenReturn(topProduct);

        // Act
        Map<String, String> result = productService.getTopSellingProduct();

        // Assert
        assertEquals("Camisa", result.get("nombre"));
        assertEquals("Camisa premium", result.get("descripcion"));

        verify(productRepository, times(1)).getOrders();
        verify(productRepository, times(1)).findByNameIgnoreCase("Camisa");
    }

    @Test
    void getRecommendedProduct_HappyPath_ReturnsTop3() {
        // Arrange
        Product p1 = new Product();
        p1.setName("Camisa");
        p1.setDescription("Camisa de algodón");
        p1.setPrice(199.99f);
        Product p2 = new Product();
        p2.setName("Pantalón");
        p2.setDescription("Pantalón de lino");
        p2.setPrice(299.99f);
        Product p3 = new Product();
        p3.setName("Zapatos");
        p3.setDescription("Zapatos de cuero");
        p3.setPrice(399.99f);
        Product p4 = new Product();
        p4.setName("Gorra");
        p4.setDescription("Gorra deportiva");
        p4.setPrice(99.99f);

        when(productRepository.findTopRatingProduct()).thenReturn(List.of(p1, p2, p3, p4));

        // Act
        Map<String, String> result = productService.getRecommendedProduct();

        // Assert
        assertEquals(3, result.size());
        assertTrue(result.containsKey("Camisa"));
        assertTrue(result.containsKey("Pantalón"));
        assertTrue(result.containsKey("Zapatos"));
        assertFalse(result.containsKey("Gorra"));
        verify(productRepository, times(1)).findTopRatingProduct();
    }

    @Test
    void getRecommendedProduct_ExactlyThreeProducts_ReturnsAll() {
        Product p1 = new Product();
        p1.setName("Camisa");
        p1.setDescription("Camisa de algodón");
        p1.setPrice(199.99f);
        Product p2 = new Product();
        p2.setName("Pantalón");
        p2.setDescription("Pantalón de lino");
        p2.setPrice(299.99f);
        Product p3 = new Product();
        p3.setName("Zapatos");
        p3.setDescription("Zapatos de cuero");
        p3.setPrice(399.99f);

        when(productRepository.findTopRatingProduct()).thenReturn(List.of(p1, p2, p3));

        Map<String, String> result = productService.getRecommendedProduct();

        assertEquals(3, result.size());
        assertTrue(result.containsKey("Zapatos"));
    }

    @Test
    void getRecommendedProduct_LessThanThreeProducts_ReturnsAllAvailable() {
        Product p1 = new Product();
        p1.setName("Camisa");
        p1.setDescription("Camisa de algodón");
        p1.setPrice(199.99f);
        Product p2 = new Product();
        p2.setName("Pantalón");
        p2.setDescription("Pantalón de lino");
        p2.setPrice(299.99f);

        when(productRepository.findTopRatingProduct()).thenReturn(List.of(p1, p2));

        Map<String, String> result = productService.getRecommendedProduct();

        assertEquals(2, result.size());
        assertTrue(result.containsKey("Camisa"));
        assertTrue(result.containsKey("Pantalón"));
    }

    @Test
    void getRelatedProducts_HappyPath_ReturnsSortedAndLimitedList() {

        // Arrange
        Product p1 = new Product();
        p1.setId(1L);
        p1.setAvailable(true);
        p1.setCreatedDate(new Date());

        Product p2 = new Product();
        p2.setId(2L);
        p2.setAvailable(true);
        p2.setCreatedDate(Date.from(LocalDate.now().minusDays(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()));

        Product p3 = new Product();
        p3.setId(3L);
        p3.setAvailable(true);
        p3.setCreatedDate(Date.from(LocalDate.now().minusDays(2)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()));

        Product p4 = new Product();
        p4.setId(4L);
        p4.setAvailable(true);
        p4.setCreatedDate(Date.from(LocalDate.now()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()));

        Product p5 = new Product();
        p5.setId(5L);
        p5.setAvailable(true);
        p5.setCreatedDate(Date.from(LocalDate.now().minusDays(4)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()));

        when(productRepository.findBySection(Section.TSHIRT)).thenReturn(List.of(p1, p2, p3, p4, p5));

        // Act
        List<Product> result = productService.getRelatedProducts("TSHIRT", 99L);

        // Assert
        assertEquals(4, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(4L, result.get(1).getId());
        assertEquals(2L, result.get(2).getId());
        assertEquals(3L, result.get(3).getId());

        verify(productRepository, times(1)).findBySection(Section.TSHIRT);
    }

    @Test
    void getRelatedProducts_InvalidSection_ThrowsException() {
        assertThrows(InvalidInputException.class, () -> productService.getRelatedProducts("INVALID", 1L));
        verify(productRepository, never()).findBySection(any());
    }

    @Test
    void findByReference_ProductExists_ReturnsProduct() {
        // Arrange
        Product product = new Product();
        product.setId(1L);
        when(productRepository.findByReference(1L)).thenReturn(Optional.of(product));

        // Act
        Product result = productService.findByReference(1L, "es");

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(productRepository, times(1)).findByReference(1L);
        verify(messageSource, never()).getMessage(anyString(), any(), any());
    }

    @Test
    void findByReference_ProductNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(productRepository.findByReference(99L)).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq("product.notReference"), isNull(), eq(new Locale("es"))))
                .thenReturn("Producto no encontrado: ");

        // Act & Assert
        AppExceptions.ResourceNotFoundException ex = assertThrows(
                AppExceptions.ResourceNotFoundException.class,
                () -> productService.findByReference(99L, "es"));

        assertTrue(ex.getMessage().contains("Producto no encontrado: 99"));
        verify(productRepository, times(1)).findByReference(99L);
        verify(messageSource, times(1)).getMessage(eq("product.notReference"), isNull(), eq(new Locale("es")));
    }

    @Test
    void saveCollection_NewCollection_SavesSuccessfully() {
        // Arrange
        String name = "Collection Test";
        when(collectionRepository.existsByName(name)).thenReturn(false);

        // Act
        productService.saveCollection(name);

        // Assert
        verify(collectionRepository, times(1))
                .save(argThat(collection -> collection.getName().equals(name.toUpperCase())));
    }

    @Test
    void saveCollection_CollectionAlreadyExists_ThrowsException() {
        // Arrange
        String name = "existente";
        when(collectionRepository.existsByName(name)).thenReturn(true);

        // Act & Assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> productService.saveCollection(name));

        assertTrue(ex.getMessage().contains("400 BAD_REQUEST"));
        verify(collectionRepository, times(1)).existsByName(name);
        verify(collectionRepository, never()).save(any());
    }

    @Test
    void editCollection_CollectionExists_UpdatesSuccessfully() {
        // Arrange
        CollectionDTO dto = new CollectionDTO(1l, "Nueva Colección", true);

        Collection existingCollection = new Collection();
        existingCollection.setId(1L);
        existingCollection.setName("OldName");
        existingCollection.setIsPromoted(false);

        when(collectionRepository.findById(1L)).thenReturn(Optional.of(existingCollection));

        // Act
        productService.editCollection(dto);

        // Assert
        verify(collectionRepository, times(1)).findById(1L);
        verify(collectionRepository, times(1)).save(
                argThat(collection -> collection.getName().equals("NuevaColección") && collection.getIsPromoted()));
    }

    @Test
    void editCollection_CollectionNotFound_ThrowsException() {
        // Arrange
        CollectionDTO dto = new CollectionDTO(99l, "Colección", false);

        when(collectionRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> productService.editCollection(dto));

        assertEquals("400 BAD_REQUEST \"Collection with the id '99' not exists.\"", ex.getMessage());
        verify(collectionRepository, times(1)).findById(99L);
        verify(collectionRepository, never()).save(any());
    }

    @Test
    void test_findByCollection() {
        Product product = new Product();
        when(productRepository.findByCollectionName("Test Collection")).thenReturn(List.of(product));
        List<Product> result = productService.findByCollection("Test Collection");
        assertNotNull(result);
    }

    @Test
    void getProducts_SizeIsMinusOne_ReturnsAllProducts() {
        // Arrange
        Product product = new Product();
        Product product2 = new Product();

        List<Product> products = List.of(product, product2);
        when(productService.findAllProducts()).thenReturn(products);

        // Act
        PageResponse<ProductDTO> response = productService.getProducts(0, -1, null);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.getContent().size());
        assertEquals(2, response.getTotalElements());
        assertTrue(response.isLast());
    }

    @Test
    void getProducts_WithSearch_UsesSearchProducts() {
        // Arrange
        Product product = new Product();
        String search = "camisa";
        PageRequest pageRequest = PageRequest.of(0, 5);
        Page<Product> pageResult = new PageImpl<>(List.of(product));

        when(productService.searchProducts(search, pageRequest)).thenReturn(pageResult);

        // Act
        PageResponse<ProductDTO> response = productService.getProducts(0, 5, search);

        // Assert
        assertEquals(1, response.getContent().size());
    }

    @Test
    void getProducts_WithoutSearch_UsesFindAll() {
        // Arrange
        Product product = new Product();
        Product product2 = new Product();
        PageRequest pageRequest = PageRequest.of(0, 5);
        Page<Product> pageResult = new PageImpl<>(List.of(product, product2));
        when(productRepository.findAll(pageRequest)).thenReturn(pageResult);

        // Act
        PageResponse<ProductDTO> response = productService.getProducts(0, 5, "");

        // Assert
        assertEquals(2, response.getContent().size());
        verify(productRepository, times(1)).findAll(pageRequest);
    }

    @Test
    void test_findHoodiesProduct() {
        Product product = new Product();
        product.setSection(Section.HOODIES);
        when(productRepository.findBySection(Section.HOODIES)).thenReturn(List.of(product));
        List<Product> result = productService.findHoodiesProduct();
        assertNotNull(result);
    }

    @Test
    void deleteImagesString_emptyList_doesNothing() {
        List<String> emptyList = List.of();
        assertDoesNotThrow(() -> productService.deleteImagesString(emptyList));
    }

    @Test
    void deleteImagesString_filesDeletedSuccessfully() throws IOException {
        List<String> images = List.of("/some/path/image1.jpg", "/some/path/image2.png");

        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
            // Simular que Files.deleteIfExists no lanza excepción
            mockedFiles.when(() -> Files.deleteIfExists(any(Path.class))).thenReturn(true);

            assertDoesNotThrow(() -> productService.deleteImagesString(images));

            // Verificar que se llamó a deleteIfExists para cada archivo
            mockedFiles.verify(() -> Files.deleteIfExists(any(Path.class)), times(2));
            mockedFiles.verify(() -> Files.deleteIfExists(any(Path.class)), times(2));
        }
    }

    @Test
    void deleteImagesString_deleteThrowsIOException_throwsRuntimeException() throws IOException {
        List<String> images = List.of("/some/path/image1.jpg");

        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
            // Simular que deleteIfExists lanza IOException
            mockedFiles.when(() -> Files.deleteIfExists(any(Path.class))).thenThrow(new IOException("Disk error"));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> productService.deleteImagesString(images));
            assertTrue(ex.getMessage().contains("Error eliminando la imagen: /some/path/image1.jpg"));
            assertTrue(ex.getCause() instanceof IOException);
        }
    }

}
