package com.artists_heaven.userProduct;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.entities.user.UserRepository;
import com.artists_heaven.exception.AppExceptions;
import com.artists_heaven.productVote.ProductVoteRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class UserProductServiceTest {

    @Mock
    private UserProductRepository userProductRepository;

    @Mock
    private ProductVoteRepository productVoteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private UserProductService userProductService;

    @TempDir
    Path tempDir;

    private User user;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setId(1L);
        user.setPoints(50);

        Files.createDirectories(Paths.get("artists-heaven-backend/src/main/resources/userProduct_media/"));

    }

    @AfterEach
    void cleanUp() throws Exception {
        Path uploadDir = Paths.get("artists-heaven-backend/src/main/resources/userProduct_media/");

        if (Files.exists(uploadDir)) {
            // Borrar todos los archivos dentro de la carpeta
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(uploadDir)) {
                for (Path filePath : stream) {
                    Files.deleteIfExists(filePath);
                }
            }
        }
    }

    // 🔹 createUserProduct tests
    @Test
    void createUserProduct_success_firstProduct_addsPoints() {
        UserProductDTO dto = new UserProductDTO();
        dto.setName("Test Product");
        dto.setImages(List.of("img1.png"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userProductRepository.countByOwnerIdAndCreatedAtBetween(anyLong(), any(), any())).thenReturn(0L);
        when(userProductRepository.countByOwnerId(1L)).thenReturn(0L);
        when(userProductRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        UserProduct result = userProductService.createUserProduct(dto, 1L, "en");

        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        verify(userRepository).save(user);
    }

    @Test
    void createUserProduct_fails_userNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        when(messageSource.getMessage("user.NotFound", null, new Locale("en")))
                .thenReturn("User not found");

        assertThrows(AppExceptions.ResourceNotFoundException.class,
                () -> userProductService.createUserProduct(new UserProductDTO(), 1L, "en"));
    }

    @Test
    void createUserProduct_fails_limitExceeded() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userProductRepository.countByOwnerIdAndCreatedAtBetween(anyLong(), any(), any())).thenReturn(3L);
        when(messageSource.getMessage("userProduct.limitExceed", null, new Locale("en")))
                .thenReturn("Limit exceeded");

        assertThrows(AppExceptions.LimitExceededException.class,
                () -> userProductService.createUserProduct(new UserProductDTO(), 1L, "en"));
    }

    @Test
    void createUserProduct_success_notFirstProduct_noExtraPoints() {
        UserProductDTO dto = new UserProductDTO();
        dto.setName("Second Product");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userProductRepository.countByOwnerIdAndCreatedAtBetween(anyLong(), any(), any())).thenReturn(0L);
        when(userProductRepository.countByOwnerId(1L)).thenReturn(1L);
        when(userProductRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        UserProduct result = userProductService.createUserProduct(dto, 1L, "en");

        assertEquals("Second Product", result.getName());
        verify(userRepository, never()).save(user);
    }

    // 🔹 getAllUserProducts
    @Test
    void getAllUserProducts_returnsList() {
        when(userProductRepository.findAll()).thenReturn(List.of(new UserProduct(), new UserProduct()));

        List<UserProduct> products = userProductService.getAllUserProducts();

        assertEquals(2, products.size());
    }

    // 🔹 saveImages
    @Test
    void saveImages_success() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("test.png");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("fake".getBytes()));

        List<String> urls = userProductService.saveImages(List.of(file));

        assertEquals(1, urls.size());
        assertTrue(urls.get(0).contains("/userProduct_media/"));
    }

    @Test
    void saveImages_fails_emptyFile() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> userProductService.saveImages(List.of(file)));
    }

    @Test
    void saveImages_fails_ioException() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("test.png");
        when(file.getInputStream()).thenThrow(new IOException());

        assertThrows(IllegalArgumentException.class,
                () -> userProductService.saveImages(List.of(file)));
    }

    // 🔹 getVoteCountsForProducts
    @Test
    void getVoteCountsForProducts_success() {
        when(productVoteRepository.countVotesForProducts(anyList()))
                .thenReturn(List.of(new Object[] { 1L, 5L }, new Object[] { 2L, 10L }));

        Map<Long, Integer> result = userProductService.getVoteCountsForProducts(List.of(1L, 2L));

        assertEquals(5, result.get(1L));
        assertEquals(10, result.get(2L));
    }

    // 🔹 getAllUserProductDetails
    @Test
    void getAllUserProductDetails_returnsProductsWithVotesAndUserVotes() {
        // Preparar datos
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        UserProduct product1 = new UserProduct();
        product1.setId(100L);
        product1.setName("Product 1");
        product1.setOwner(user);

        UserProduct product2 = new UserProduct();
        product2.setId(101L);
        product2.setName("Product 2");
        product2.setOwner(user);

        List<UserProduct> products = List.of(product1, product2);

        LocalDate oneMonthAgo = LocalDate.now().minusMonths(1);
        Date oneMonthAgoDate = Date.from(oneMonthAgo.atStartOfDay(ZoneId.systemDefault()).toInstant());

        // Mock repos
        when(userProductRepository.findAllByMonthAndAccepted(oneMonthAgoDate)).thenReturn(products);

        // Simular votos totales
        when(productVoteRepository.countVotesForProducts(List.of(100L, 101L)))
                .thenReturn(List.of(
                        new Object[] { 100L, 5L },
                        new Object[] { 101L, 3L }));

        // Simular productos votados por el usuario
        when(productVoteRepository.findProductIdsByUserId(user.getId()))
                .thenReturn(Set.of(101L));

        // Ejecutar método
        List<UserProductDetailsDTO> result = userProductService.getAllUserProductDetails(user.getId());

        // Verificar
        assertEquals(2, result.size());

        UserProductDetailsDTO dto1 = result.get(0);
        assertEquals(100L, dto1.getId());
        assertEquals("Product 1", dto1.getName());
        assertEquals(5, dto1.getNumVotes());
        assertFalse(dto1.isVotedByUser());

        UserProductDetailsDTO dto2 = result.get(1);
        assertEquals(101L, dto2.getId());
        assertEquals("Product 2", dto2.getName());
        assertEquals(3, dto2.getNumVotes());
        assertTrue(dto2.isVotedByUser());

        // Verificar llamadas a repos
        verify(userProductRepository, times(1)).findAllByMonthAndAccepted(oneMonthAgoDate);
        verify(productVoteRepository, times(1)).countVotesForProducts(List.of(100L, 101L));
        verify(productVoteRepository, times(1)).findProductIdsByUserId(user.getId());
    }

    @Test
    void getAllUserProductDetails_noUser_returnsProductsWithoutUserVotes() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        UserProduct product1 = new UserProduct();
        product1.setId(100L);
        product1.setName("Product 1");
        product1.setOwner(user);

        List<UserProduct> products = List.of(product1);

        LocalDate oneMonthAgo = LocalDate.now().minusMonths(1);
        Date oneMonthAgoDate = Date.from(oneMonthAgo.atStartOfDay(ZoneId.systemDefault()).toInstant());

        // Mock repos
        when(userProductRepository.findAllByMonthAndAccepted(oneMonthAgoDate)).thenReturn(products);
        when(productVoteRepository.countVotesForProducts(List.of(100L)))
                .thenReturn(Collections.singletonList(new Object[] { 100L, 2L }));

        // Ejecutar método sin usuario
        List<UserProductDetailsDTO> result = userProductService.getAllUserProductDetails(null);

        // Verificar
        assertEquals(1, result.size());
        UserProductDetailsDTO dto = result.get(0);
        assertEquals(100L, dto.getId());
        assertEquals(2, dto.getNumVotes());
        assertFalse(dto.isVotedByUser());

        // Verificar llamadas
        verify(userProductRepository, times(1)).findAllByMonthAndAccepted(oneMonthAgoDate);
        verify(productVoteRepository, times(1)).countVotesForProducts(List.of(100L));
        verify(productVoteRepository, never()).findProductIdsByUserId(anyLong());
    }

    // 🔹 findUserProductPending
    @Test
    void findUserProductPending_returnsPending() {
        when(userProductRepository.getAllPendingVerification()).thenReturn(List.of(new UserProduct()));

        List<UserProduct> result = userProductService.findUserProductPending();

        assertEquals(1, result.size());
    }

    // 🔹 approveProduct
    @Test
    void approveProduct_success() {
        UserProduct product = new UserProduct();
        product.setId(1L);
        product.setOwner(user);

        when(userProductRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userProductRepository.save(product)).thenReturn(product);

        UserProduct result = userProductService.approveProduct(1L);

        assertEquals(Status.ACCEPTED, result.getStatus());
        assertEquals(70, user.getPoints()); // 50 + 20
    }

    @Test
    void approveProduct_fails_notFound() {
        when(userProductRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(AppExceptions.ResourceNotFoundException.class,
                () -> userProductService.approveProduct(1L));
    }

    // 🔹 rejectProduct
    @Test
    void rejectProduct_success() {
        UserProduct product = new UserProduct();
        product.setId(1L);
        product.setOwner(user);

        when(userProductRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userProductRepository.save(product)).thenReturn(product);

        UserProduct result = userProductService.rejectProduct(1L);

        assertEquals(Status.REJECTED, result.getStatus());
        assertEquals(40, user.getPoints()); // 50 - 10
    }

    @Test
    void rejectProduct_fails_notFound() {
        when(userProductRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(AppExceptions.ResourceNotFoundException.class,
                () -> userProductService.rejectProduct(1L));
    }

    // 🔹 findMyUserProducts
    @Test
    void findMyUserProducts_returnsDetailsWithVotes() {
        // Preparar datos
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        UserProduct product1 = new UserProduct();
        product1.setId(100L);
        product1.setName("Product 1");
        product1.setOwner(user);

        UserProduct product2 = new UserProduct();
        product2.setId(101L);
        product2.setName("Product 2");
        product2.setOwner(user);

        List<UserProduct> products = List.of(product1, product2);

        // Simular repositorio
        when(userProductRepository.findByOwnerIdOrderByCreatedAtDesc(user.getId()))
                .thenReturn(products);

        // Simular votos
        when(productVoteRepository.countVotesForProducts(List.of(100L, 101L)))
                .thenReturn(List.of(
                        new Object[] { 100L, 5L },
                        new Object[] { 101L, 3L }));

        // Ejecutar
        List<UserProductDetailsDTO> result = userProductService.findMyUserProducts(user.getId());

        // Verificaciones
        assertEquals(2, result.size());

        UserProductDetailsDTO dto1 = result.get(0);
        assertEquals(100L, dto1.getId());
        assertEquals("Product 1", dto1.getName());
        assertEquals(5, dto1.getNumVotes());

        UserProductDetailsDTO dto2 = result.get(1);
        assertEquals(101L, dto2.getId());
        assertEquals("Product 2", dto2.getName());
        assertEquals(3, dto2.getNumVotes());

        // Verificar que se llamó al repositorio
        verify(userProductRepository, times(1)).findByOwnerIdOrderByCreatedAtDesc(user.getId());
        verify(productVoteRepository, times(1)).countVotesForProducts(List.of(100L, 101L));
    }

}
