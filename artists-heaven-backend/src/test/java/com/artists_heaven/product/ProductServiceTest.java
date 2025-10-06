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
import org.mockito.ArgumentMatchers;
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;

import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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

    @Mock
    private MultipartFile mockFile;

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

    private Product createProduct(String name, String desc, boolean available, boolean onPromotion) {
        Product p = new Product();
        p.setName(name);
        p.setDescription(desc);
        p.setAvailable(available);
        p.setOn_Promotion(onPromotion);
        return p;
    }

    @Test
    void registerProductTest() throws Exception {
        // Arrange: crear DTO válido
        ProductColorDTO productColorDTO = new ProductColorDTO();
        productColorDTO.setColorName("colorTest");
        productColorDTO.setHexCode("#ffffff");
        productColorDTO.setImages(List.of("newImages"));
        Map<String, Integer> sizes = Map.of("M", 1);
        productColorDTO.setSizes(sizes);

        ProductDTO productDTO = new ProductDTO();
        productDTO.setName("Name");
        productDTO.setDescription("Description");
        productDTO.setPrice(100.0f);
        productDTO.setSection(Section.TSHIRT);
        productDTO.setComposition("Cotton 100%");
        productDTO.setShippingDetails("Ships in 3-5 days");
        productDTO.setAvailable(true);
        productDTO.setReference(12345L);
        productDTO.setColors(List.of(productColorDTO));

        Product product = new Product();
        product.setId(1L);
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setSection(productDTO.getSection());
        product.setComposition(productDTO.getComposition());
        product.setShippingDetails(productDTO.getShippingDetails());
        product.setAvailable(productDTO.getAvailable());
        product.setReference(productDTO.getReference());

        when(productRepository.save(any(Product.class))).thenReturn(product);

        // Act
        Product result = productService.registerProduct(productDTO);

        // Assert
        assertNotNull(result);
        assertEquals(productDTO.getName(), result.getName());
        assertEquals(productDTO.getDescription(), result.getDescription());
        assertEquals(productDTO.getPrice(), result.getPrice(), 0.001f);
        assertEquals(productDTO.getSection(), result.getSection());
        assertEquals(productDTO.getComposition(), result.getComposition());
        assertEquals(productDTO.getShippingDetails(), result.getShippingDetails());
        assertEquals(productDTO.getReference(), result.getReference());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testRegisterProductThrowsException() {
        // Arrange: crear un DTO válido mínimo
        ProductColorDTO productColorDTO = new ProductColorDTO();
        productColorDTO.setColorName("colorTest");
        productColorDTO.setHexCode("#ffffff");
        productColorDTO.setImages(List.of("newImages"));
        Map<String, Integer> sizes = Map.of("M", 1);
        productColorDTO.setSizes(sizes);

        ProductDTO productDTO = new ProductDTO();
        productDTO.setName("Test Name");
        productDTO.setDescription("Test Description");
        productDTO.setPrice(100.0f);
        productDTO.setSection(Section.TSHIRT);
        productDTO.setComposition("Cotton 100%");
        productDTO.setShippingDetails("Ships in 3-5 days");
        productDTO.setAvailable(true);
        productDTO.setReference(12345L);
        productDTO.setColors(List.of(productColorDTO));

        when(productRepository.save(any(Product.class)))
                .thenThrow(new RuntimeException("Unable to create the product"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> productService.registerProduct(productDTO));

        assertEquals("Unable to create the product", exception.getMessage());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void registerProductWithAvailableUnitsTest() {
        ProductColorDTO colorDTO = new ProductColorDTO();
        colorDTO.setColorName("Red");
        colorDTO.setHexCode("#FF0000");
        colorDTO.setAvailableUnits(10);
        colorDTO.setImages(List.of("red_front.jpg", "red_back.jpg"));
        colorDTO.setSizes(Map.of("M", 5, "L", 5));

        ProductDTO productDTO = new ProductDTO();
        productDTO.setName("Product Name");
        productDTO.setDescription("Product with units");
        productDTO.setPrice(150.0f);
        productDTO.setSection(Section.TSHIRT);
        productDTO.setComposition("100% Cotton");
        productDTO.setShippingDetails("Ships in 3-5 days");
        productDTO.setAvailable(true);
        productDTO.setReference(123456L);
        productDTO.setColors(List.of(colorDTO));

        // Entidad esperada
        Product product = new Product();
        product.setId(1L);
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setSection(productDTO.getSection());
        product.setComposition(productDTO.getComposition());
        product.setShippingDetails(productDTO.getShippingDetails());
        product.setAvailable(productDTO.getAvailable());
        product.setReference(productDTO.getReference());

        ProductColor color = new ProductColor();
        color.setColorName("Red");
        color.setHexCode("#FF0000");
        color.setAvailableUnits(10);
        color.setImages(List.of("red_front.jpg", "red_back.jpg"));
        color.setSizes(Map.of("M", 5, "L", 5));
        color.setProduct(product);

        product.setColors(List.of(color));

        when(productRepository.save(any(Product.class))).thenReturn(product);

        // Act
        Product result = productService.registerProduct(productDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Product Name", result.getName());
        assertEquals(1, result.getColors().size());
        assertEquals(10, result.getColors().get(0).getAvailableUnits());
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
        // Arrange: producto existente en BD
        Product product = new Product();
        product.setId(1L);
        product.setName("Old Name");
        product.setDescription("Old Description");
        product.setPrice(100.0f);
        product.setCategories(new HashSet<>());
        product.setComposition("Old Composition");
        product.setShippingDetails("Old Shipping Details");
        product.setAvailable(true);
        product.setReference(111L);
        product.setColors(new ArrayList<>());

        // DTO con los nuevos valores
        ProductDTO productDTO = new ProductDTO();
        productDTO.setName("Updated Name");
        productDTO.setDescription("Updated Description");
        productDTO.setPrice(200.0f);
        productDTO.setCategories(new HashSet<>());
        productDTO.setComposition("New Composition");
        productDTO.setShippingDetails("New Shipping Details");
        productDTO.setAvailable(false);
        productDTO.setReference(222L);
        productDTO.setColors(new ArrayList<>());

        // Mock del archivo 3D
        MultipartFile model3d = new MockMultipartFile(
                "file",
                "test.glb",
                "model/gltf-binary",
                "contenido-del-modelo".getBytes(StandardCharsets.UTF_8));

        // Map vacío de imágenes por color
        Map<Integer, List<MultipartFile>> colorImages = new HashMap<>();

        // Map con un solo modelo 3D en la posición 0
        Map<Integer, MultipartFile> colorModels = new HashMap<>();
        colorModels.put(0, model3d);

        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        productService.updateProduct(product.getId(), productDTO, colorImages, colorModels);

        // Assert
        assertEquals("Updated Name", product.getName());
        assertEquals("Updated Description", product.getDescription());
        assertEquals(200.0f, product.getPrice());
        assertEquals("New Composition", product.getComposition());
        assertEquals("New Shipping Details", product.getShippingDetails());
        assertFalse(product.getAvailable());
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void testUpdateProductWithNewImages() throws IOException {
        // Arrange: producto existente
        Product product = new Product();
        product.setId(1L);
        product.setName("Old Name");
        product.setDescription("Old Description");
        product.setPrice(100.0f);
        product.setCategories(new HashSet<>());
        product.setComposition("Old Composition");
        product.setShippingDetails("Old Shipping");
        product.setAvailable(true);
        product.setReference(111L);
        product.setColors(new ArrayList<>());

        // DTO con valores nuevos
        ProductDTO productDTO = new ProductDTO();
        productDTO.setName("Updated Name");
        productDTO.setDescription("Updated Description");
        productDTO.setPrice(200.0f);
        productDTO.setCategories(new HashSet<>());
        productDTO.setComposition("New Composition");
        productDTO.setShippingDetails("New Shipping");
        productDTO.setAvailable(true);
        productDTO.setReference(222L);
        productDTO.setColors(new ArrayList<>());

        // Mock de nueva imagen y modelo 3D
        MultipartFile newImage = mock(MultipartFile.class);
        when(newImage.getOriginalFilename()).thenReturn("newImage.jpg");
        when(newImage.getInputStream()).thenReturn(new ByteArrayInputStream("image data".getBytes()));

        Map<Integer, List<MultipartFile>> colorImages = new HashMap<>();

        // Map con un solo modelo 3D en la posición 0
        MultipartFile model3d = new MockMultipartFile(
                "file",
                "test.glb",
                "model/gltf-binary",
                "contenido-del-modelo".getBytes(StandardCharsets.UTF_8));
        Map<Integer, MultipartFile> colorModels = new HashMap<>();
        colorModels.put(0, model3d);

        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Mock estático de Files.copy
        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.copy(any(InputStream.class), any(Path.class))).thenAnswer(invocation -> null);

            // Act
            productService.updateProduct(product.getId(), productDTO, colorImages, colorModels);

            // Assert
            assertEquals("Updated Name", product.getName());
            assertEquals("Updated Description", product.getDescription());
            assertEquals(200.0f, product.getPrice());
            assertEquals("New Composition", product.getComposition());
            assertEquals("New Shipping", product.getShippingDetails());
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
                () -> productService.updateProduct(1L, productDTO, null, Map.of(0, model3d)));
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
                () -> productService.updateProduct(1L, productDTO, null, null));
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
                () -> productService.updateProduct(1L, productDTO, null, null));
    }

    @Test
    void testUpdateProduct_collectionNotFound() {
        // Arrange: producto existente
        Product product = new Product();
        product.setId(1L);
        product.setName("Valid Name");
        product.setPrice(100f);
        product.setCategories(new HashSet<>());
        product.setComposition("Some Composition");
        product.setShippingDetails("Some Shipping");
        product.setAvailable(true);
        product.setReference(123L);

        // DTO con collectionId que no existe
        ProductDTO productDTO = new ProductDTO();
        productDTO.setName("Valid Name");
        productDTO.setPrice(100f);
        productDTO.setCollectionId(99L); // ID inexistente
        productDTO.setCategories(new HashSet<>());

        // Mocks
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(collectionRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AppExceptions.ResourceNotFoundException.class,
                () -> productService.updateProduct(1L, productDTO, null, null));
    }

    @Test
    void testUpdateProduct_keepExistingImages() {
        // Arrange
        Product product = new Product();
        product.setId(1L);
        product.setName("Old");
        product.setPrice(100f);
        product.setColors(new ArrayList<>());
        Category category = new Category();
        Set<Category> categorySet = new HashSet<>();
        categorySet.add(category);
        product.setCategories(categorySet);

        // DTO con un color con imágenes ya existentes
        ProductColorDTO colorDTO = new ProductColorDTO();
        colorDTO.setColorName("Red");
        colorDTO.setHexCode("#FF0000");
        colorDTO.setAvailableUnits(10);
        colorDTO.setSizes(Map.of("S", 1));
        colorDTO.setImages(List.of("oldImage1.jpg", "oldImage2.jpg"));

        ProductDTO productDTO = new ProductDTO();
        productDTO.setName("Updated");
        productDTO.setPrice(200f);
        productDTO.setColors(List.of(colorDTO));
        productDTO.setCategories(new HashSet<>());

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        productService.updateProduct(1L, productDTO, null, null);

        // Assert
        assertEquals(1, product.getColors().size());
        ProductColor updatedColor = product.getColors().get(0);
        assertEquals("Red", updatedColor.getColorName());
        assertEquals(List.of("oldImage1.jpg", "oldImage2.jpg"), updatedColor.getImages());
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void testUpdateProduct_addNewColor() {
        Category category = new Category();
        category.setId(1L);

        Set<Category> categories = new HashSet<>();
        categories.add(category);

        Product product = new Product();
        product.setId(1L);
        product.setColors(new ArrayList<>());
        product.setCategories(categories);

        ProductColorDTO newColorDTO = new ProductColorDTO();
        newColorDTO.setColorName("Blue");
        newColorDTO.setHexCode("#0000FF");
        newColorDTO.setAvailableUnits(5);
        newColorDTO.setSizes(Map.of("M", 2));
        newColorDTO.setImages(new ArrayList<>());
        newColorDTO.setModelReference("existingModel.glb");

        ProductDTO productDTO = new ProductDTO();
        productDTO.setName("Product with new color");
        productDTO.setPrice(150f);
        productDTO.setColors(List.of(newColorDTO));
        productDTO.setCategories(new HashSet<>());

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        productService.updateProduct(1L, productDTO, null, null);

        assertEquals(1, product.getColors().size());
        ProductColor addedColor = product.getColors().get(0);
        assertEquals("Blue", addedColor.getColorName());
        assertEquals("#0000FF", addedColor.getHexCode());
        assertEquals(5, addedColor.getAvailableUnits());
        assertEquals("existingModel.glb", addedColor.getModelReference());
    }

    @Test
    void testUpdateProduct_updateExistingColorAndRemoveOld() {
        Category category = new Category();
        category.setId(1L);

        Set<Category> categories = new HashSet<>();
        categories.add(category);

        ProductColor existingColor = new ProductColor();
        existingColor.setId(1L);
        existingColor.setColorName("OldRed");
        existingColor.setImages(List.of("oldRed.jpg"));

        Product product = new Product();
        product.setId(1L);
        product.setColors(new ArrayList<>(List.of(existingColor)));
        product.setCategories(categories);

        ProductColorDTO colorDTO = new ProductColorDTO();
        colorDTO.setColorId(1L); // actualizar color existente
        colorDTO.setColorName("UpdatedRed");
        colorDTO.setHexCode("#FF1111");
        colorDTO.setAvailableUnits(20);
        colorDTO.setSizes(Map.of("L", 3));
        colorDTO.setImages(List.of("updatedRed.jpg"));

        ProductColorDTO newColorDTO = new ProductColorDTO();
        newColorDTO.setColorName("Green"); // nuevo color
        newColorDTO.setHexCode("#00FF00");
        newColorDTO.setAvailableUnits(10);
        newColorDTO.setSizes(Map.of("S", 1));
        newColorDTO.setImages(List.of("green.jpg"));

        ProductDTO productDTO = new ProductDTO();
        productDTO.setName("Updated Product");
        productDTO.setPrice(200f);
        productDTO.setColors(List.of(colorDTO, newColorDTO));
        productDTO.setCategories(new HashSet<>());

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        productService.updateProduct(1L, productDTO, null, null);

        assertEquals(2, product.getColors().size());
        ProductColor updatedColor = product.getColors().stream()
                .filter(c -> c.getId() != null && c.getId().equals(1L))
                .findFirst().get();
        assertEquals("UpdatedRed", updatedColor.getColorName());
        assertEquals(List.of("updatedRed.jpg"), updatedColor.getImages());

        ProductColor addedColor = product.getColors().stream()
                .filter(c -> c.getId() == null)
                .findFirst().get();
        assertEquals("Green", addedColor.getColorName());
        assertEquals(List.of("green.jpg"), addedColor.getImages());
    }

    @Test
    void testUpdateProduct_keepExistingModelIfNoNewFile() {
        Category category = new Category();
        category.setId(1L);

        Set<Category> categories = new HashSet<>();
        categories.add(category);

        ProductColor existingColor = new ProductColor();
        existingColor.setId(1L);
        existingColor.setModelReference("oldModel.glb");

        Product product = new Product();
        product.setId(1L);
        product.setColors(new ArrayList<>(List.of(existingColor)));
        product.setCategories(categories);

        ProductColorDTO colorDTO = new ProductColorDTO();
        colorDTO.setColorId(1L);
        colorDTO.setColorName("Red");
        colorDTO.setModelReference("oldModel.glb");

        ProductDTO productDTO = new ProductDTO();
        productDTO.setName("Product");
        productDTO.setPrice(100f);
        productDTO.setColors(List.of(colorDTO));
        productDTO.setCategories(new HashSet<>());

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        productService.updateProduct(1L, productDTO, null, null);

        assertEquals("oldModel.glb", product.getColors().get(0).getModelReference());
    }

    @Test
    void testUpdateProduct_addColorImagesAndModel() throws IOException {
        Category category = new Category();
        category.setId(1L);

        Set<Category> categories = new HashSet<>();
        categories.add(category);

        Product product = new Product();
        product.setId(1L);
        product.setColors(new ArrayList<>());
        product.setCategories(categories);

        ProductColorDTO colorDTO = new ProductColorDTO();
        colorDTO.setColorName("Yellow");
        colorDTO.setHexCode("#FFFF00");
        colorDTO.setAvailableUnits(5);
        colorDTO.setSizes(Map.of("M", 2));
        colorDTO.setImages(List.of("oldImage.jpg"));

        ProductDTO productDTO = new ProductDTO();
        productDTO.setName("Product with images and model");
        productDTO.setPrice(100f);
        productDTO.setColors(List.of(colorDTO));
        productDTO.setCategories(new HashSet<>());

        // Mock de nuevas imágenes
        MultipartFile newImage1 = mock(MultipartFile.class);
        when(newImage1.getOriginalFilename()).thenReturn("new1.jpg");
        when(newImage1.getInputStream()).thenReturn(new ByteArrayInputStream("data1".getBytes()));

        MultipartFile newImage2 = mock(MultipartFile.class);
        when(newImage2.getOriginalFilename()).thenReturn("new2.jpg");
        when(newImage2.getInputStream()).thenReturn(new ByteArrayInputStream("data2".getBytes()));

        Map<Integer, List<MultipartFile>> colorImages = Map.of(0, List.of(newImage1, newImage2));

        // Mock del modelo 3D
        MultipartFile model3d = new MockMultipartFile(
                "file", "model.glb", "model/gltf-binary", "3d content".getBytes());

        Map<Integer, MultipartFile> colorModels = Map.of(0, model3d);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        // Mock de métodos internos saveImages y saveModel
        ProductService spyService = spy(productService);
        doReturn(List.of("saved1.jpg", "saved2.jpg")).when(spyService).saveImages(anyList());
        doReturn("savedModel.glb").when(spyService).saveModel(any(MultipartFile.class));

        spyService.updateProduct(1L, productDTO, colorImages, colorModels);

        assertEquals(1, product.getColors().size());
        ProductColor updatedColor = product.getColors().get(0);

        // Verificar que se agregaron las imágenes nuevas
        assertEquals(List.of("oldImage.jpg", "saved1.jpg", "saved2.jpg"), updatedColor.getImages());

        // Verificar que se guardó el modelo 3D
        assertEquals("savedModel.glb", updatedColor.getModelReference());
    }

    @Test
    void registerProduct_nameNull_throwsException() {
        ProductColorDTO productColorDTO = new ProductColorDTO();
        productColorDTO.setColorName("colorTest");
        productColorDTO.setHexCode("#ffffff");
        productColorDTO.setImages(List.of("newImages"));
        Map<String, Integer> sizes = Map.of("M", 1);
        productColorDTO.setSizes(sizes);

        ProductDTO dto = new ProductDTO();
        dto.setName(null); // nombre nulo
        dto.setPrice(10f);
        dto.setSection(Section.TSHIRT);
        dto.setComposition("Cotton");
        dto.setShippingDetails("Ships in 3 days");
        dto.setColors(List.of(productColorDTO));

        AppExceptions.InvalidInputException ex = assertThrows(
                AppExceptions.InvalidInputException.class,
                () -> productService.registerProduct(dto));

        System.out.println(ex.getMessage());
        assertTrue(ex.getMessage().contains("product.name.required") || ex.getMessage().toLowerCase().contains("name"));
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
        // Arrange
        ProductDTO dto = new ProductDTO();
        dto.setName("Test Product");
        dto.setPrice(10f);
        dto.setComposition("Canvas and paint");
        dto.setShippingDetails("Ships in 2 days");
        dto.setSection(Section.ACCESSORIES);

        ProductColorDTO color = new ProductColorDTO();
        color.setColorName("Red");
        color.setHexCode("#FF0000");
        color.setAvailableUnits(5);

        dto.setColors(List.of(color));

        when(productRepository.existsByReference(anyLong())).thenReturn(false);
        when(productRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Product saved = productService.registerProduct(dto);

        // Assert
        assertNotNull(saved.getColors());
        assertEquals(1, saved.getColors().size());
        assertEquals(5, saved.getColors().get(0).getAvailableUnits());
    }

    @Test
    void registerProduct_withSizes_setsSize() {
        // Arrange: crear un ProductColorDTO con tallas
        ProductColorDTO colorDTO = new ProductColorDTO();
        colorDTO.setColorName("Red");
        colorDTO.setHexCode("#FF0000");
        Map<String, Integer> sizes = new HashMap<>();
        sizes.put("M", 2);
        colorDTO.setSizes(sizes);
        colorDTO.setAvailableUnits(0);
        colorDTO.setImages(List.of("image1.jpg"));

        // Crear DTO del producto
        ProductDTO dto = new ProductDTO();
        dto.setName("Test");
        dto.setPrice(10f);
        dto.setSection(Section.TSHIRT);
        dto.setComposition("Cotton 100%");
        dto.setShippingDetails("Ships in 3-5 days");
        dto.setAvailable(true);
        dto.setReference(123L);
        dto.setColors(List.of(colorDTO));

        // Mocks
        when(productRepository.existsByReference(anyLong())).thenReturn(false);
        when(productRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Product saved = productService.registerProduct(dto);

        // Assert
        assertEquals(1, saved.getColors().size());
        ProductColor savedColor = saved.getColors().get(0);
        assertEquals(sizes, savedColor.getSizes());
    }

    @Test
    void registerProduct_colorsNull_throwsException() {
        ProductDTO dto = new ProductDTO();
        dto.setName("Valid Name");
        dto.setPrice(10f);
        dto.setSection(Section.TSHIRT);
        dto.setColors(null); // <- nulo

        AppExceptions.InvalidInputException ex = assertThrows(
                AppExceptions.InvalidInputException.class,
                () -> productService.registerProduct(dto));

        assertEquals("Debe haber al menos un color con inventario e imágenes.", ex.getMessage());
    }

    @Test
    void registerProduct_colorsEmpty_throwsException() {
        ProductDTO dto = new ProductDTO();
        dto.setName("Valid Name");
        dto.setPrice(10f);
        dto.setSection(Section.TSHIRT);
        dto.setColors(List.of()); // <- vacío

        AppExceptions.InvalidInputException ex = assertThrows(
                AppExceptions.InvalidInputException.class,
                () -> productService.registerProduct(dto));

        assertEquals("Debe haber al menos un color con inventario e imágenes.", ex.getMessage());
    }

    @Test
    void registerProduct_accessoryWithNullUnits_throwsException() {
        ProductColorDTO color = new ProductColorDTO();
        color.setColorName("Blue");
        color.setHexCode("#0000FF");
        color.setAvailableUnits(null); // <- nulo
        color.setImages(List.of("img.jpg"));

        ProductDTO dto = new ProductDTO();
        dto.setName("Accessory");
        dto.setPrice(10f);
        dto.setSection(Section.ACCESSORIES);
        dto.setColors(List.of(color));

        AppExceptions.InvalidInputException ex = assertThrows(
                AppExceptions.InvalidInputException.class,
                () -> productService.registerProduct(dto));

        assertTrue(ex.getMessage().contains("availableUnits"));
    }

    @Test
    void registerProduct_accessoryWithNegativeUnits_throwsException() {
        ProductColorDTO color = new ProductColorDTO();
        color.setColorName("Blue");
        color.setHexCode("#0000FF");
        color.setAvailableUnits(-1); // <- negativo
        color.setImages(List.of("img.jpg"));

        ProductDTO dto = new ProductDTO();
        dto.setName("Accessory");
        dto.setPrice(10f);
        dto.setSection(Section.ACCESSORIES);
        dto.setColors(List.of(color));

        AppExceptions.InvalidInputException ex = assertThrows(
                AppExceptions.InvalidInputException.class,
                () -> productService.registerProduct(dto));

        assertTrue(ex.getMessage().contains("availableUnits"));
    }

    @Test
    void registerProduct_nonAccessoryWithoutSizes_throwsException() {
        ProductColorDTO color = new ProductColorDTO();
        color.setColorName("Red");
        color.setHexCode("#FF0000");
        color.setSizes(null); // <- faltan tallas
        color.setImages(List.of("img.jpg"));

        ProductDTO dto = new ProductDTO();
        dto.setName("T-Shirt");
        dto.setPrice(10f);
        dto.setSection(Section.TSHIRT);
        dto.setColors(List.of(color));

        AppExceptions.InvalidInputException ex = assertThrows(
                AppExceptions.InvalidInputException.class,
                () -> productService.registerProduct(dto));

        assertTrue(ex.getMessage().contains("mapa de tallas"));
    }

    @Test
    void registerProduct_nonAccessoryWithNegativeSizeQty_throwsException() {
        ProductColorDTO color = new ProductColorDTO();
        color.setColorName("Red");
        color.setHexCode("#FF0000");
        color.setSizes(Map.of("M", -5)); // <- cantidad negativa
        color.setImages(List.of("img.jpg"));

        ProductDTO dto = new ProductDTO();
        dto.setName("T-Shirt");
        dto.setPrice(10f);
        dto.setSection(Section.TSHIRT);
        dto.setColors(List.of(color));

        AppExceptions.InvalidInputException ex = assertThrows(
                AppExceptions.InvalidInputException.class,
                () -> productService.registerProduct(dto));

        assertTrue(ex.getMessage().contains("no pueden ser negativas"));
    }

    @Test
    void registerProduct_referenceCollisionAfterRetries_throwsException() {
        ProductColorDTO color = new ProductColorDTO();
        color.setColorName("Red");
        color.setHexCode("#FF0000");
        color.setSizes(Map.of("M", 1));
        color.setImages(List.of("img.jpg"));

        ProductDTO dto = new ProductDTO();
        dto.setName("T-Shirt");
        dto.setPrice(10f);
        dto.setSection(Section.TSHIRT);
        dto.setColors(List.of(color));

        // Forzar que siempre falle el save
        when(productRepository.save(any())).thenThrow(new DataIntegrityViolationException("duplicate"));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> productService.registerProduct(dto));

        assertTrue(ex.getMessage().contains("No se pudo generar una referencia única"));
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
        // Arrange: colección del producto
        Collection collection = new Collection();
        collection.setId(1L);

        // Producto
        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(100.0f);
        product.setAvailable(true);
        product.setOn_Promotion(false);
        product.setDiscount(0);
        product.setCategories(new HashSet<>());
        product.setCollection(collection);
        product.setComposition("Cotton 100%");
        product.setShippingDetails("Ships in 3-5 days");
        product.setSection(Section.TSHIRT);
        product.setColors(new ArrayList<>());

        // Act: mapear a DTO
        ProductDTO productDTO = new ProductDTO(product);

        // Assert
        assertNotNull(productDTO);
        assertEquals(product.getName(), productDTO.getName());
        assertEquals(product.getDescription(), productDTO.getDescription());
        assertEquals(product.getPrice(), productDTO.getPrice());
        assertEquals(product.getAvailable(), productDTO.getAvailable());
        assertEquals(product.getOn_Promotion(), productDTO.getOnPromotion());
        assertEquals(product.getDiscount(), productDTO.getDiscount());
        assertEquals(product.getCollection().getId(), productDTO.getCollectionId());
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
        AppExceptions.BadRequestException exception = assertThrows(AppExceptions.BadRequestException.class,
                () -> productService.saveCategory(categoryName));

        // Comprobar el mensaje de la excepción
        assertEquals("Category with the name 'NewCategory' already exists.", exception.getMessage());

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
    void testGetProducts_sizeMinusOne_returnsAllProducts_withoutSpy() {
        // Arrange: lista de productos de ejemplo
        Product product1 = createProduct("A", "desc A", true, false);
        Product product2 = createProduct("B", "desc B", false, true);
        List<Product> allProducts = List.of(product1, product2);

        // Mock del repository para que devuelva todos los productos
        when(productRepository.findAll()).thenReturn(allProducts);

        // Act
        PageResponse<ProductDTO> response = productService.getProducts(0, -1, null, null, null);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.getContent().size());

        // Verificar propiedades de paginación simuladas
        assertEquals(0, response.getPageNumber());
        assertEquals(2, response.getPageSize());
        assertEquals(2, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertTrue(response.isLast());
    }

    @Test
    void testGetProducts_AllNullParameters() {
        Page<Product> pageMock = new PageImpl<>(List.of(createProduct("A", "desc", true, false)));
        when(productRepository.findAll(ArgumentMatchers.<Specification<Product>>any(), any(Pageable.class)))
                .thenReturn(pageMock);
        PageResponse<ProductDTO> response = productService.getProducts(0, 10, null, null, null);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
    }

    @Test
    void testGetProducts_WithSearch() {
        Page<Product> pageMock = new PageImpl<>(List.of(createProduct("Test", "desc", true, false)));
        when(productRepository.findAll(ArgumentMatchers.<Specification<Product>>any(), any(Pageable.class)))
                .thenReturn(pageMock);

        PageResponse<ProductDTO> response = productService.getProducts(0, 10, "Test", null, null);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
    }

    @Test
    void testGetProducts_WithAvailable() {
        Page<Product> pageMock = new PageImpl<>(List.of(createProduct("A", "desc", true, false)));
        when(productRepository.findAll(ArgumentMatchers.<Specification<Product>>any(), any(Pageable.class)))
                .thenReturn(pageMock);

        PageResponse<ProductDTO> response = productService.getProducts(0, 10, null, true, null);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
    }

    @Test
    void testGetProducts_WithPromoted() {
        Page<Product> pageMock = new PageImpl<>(List.of(createProduct("A", "desc", true, true)));
        when(productRepository.findAll(ArgumentMatchers.<Specification<Product>>any(), any(Pageable.class)))
                .thenReturn(pageMock);

        PageResponse<ProductDTO> response = productService.getProducts(0, 10, null, null, true);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
    }

    @Test
    void testGetProducts_AllFilters() {
        Page<Product> pageMock = new PageImpl<>(List.of(createProduct("Test", "desc", true, true)));
        when(productRepository.findAll(ArgumentMatchers.<Specification<Product>>any(), any(Pageable.class)))
                .thenReturn(pageMock);

        PageResponse<ProductDTO> response = productService.getProducts(0, 10, "Test", true, true);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
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

    @Test
    void saveModel_nullFile_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> productService.saveModel(null));
    }

    @Test
    void saveModel_emptyFile_throwsException() {
        when(mockFile.isEmpty()).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> productService.saveModel(mockFile));

        assertTrue(ex.getMessage().contains("empty"));
    }

    @Test
    void saveModel_invalidExtension_throwsException() {
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("badfile.txt");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> productService.saveModel(mockFile));

        assertTrue(ex.getMessage().contains("Invalid file type"));
    }

    @Test
    void saveModel_validExtension_returnsUrl() throws IOException {
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("model.glb");
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("fake".getBytes()));

        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.copy(any(InputStream.class), any(Path.class)))
                    .thenReturn(1L);

            String result = productService.saveModel(mockFile);

            assertTrue(result.startsWith("/product_media/"));
            assertTrue(result.endsWith(".glb"));
        }
    }

    @Test
    void saveModel_ioException_throwsException() throws IOException {
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("model.glb");
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("fake".getBytes()));

        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.copy(any(InputStream.class), any(Path.class)))
                    .thenThrow(new IOException("Disk full"));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> productService.saveModel(mockFile));

            assertTrue(ex.getMessage().contains("Error while saving"));
        }
    }
}
