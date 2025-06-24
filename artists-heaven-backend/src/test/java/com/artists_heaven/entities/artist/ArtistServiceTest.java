package com.artists_heaven.entities.artist;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.artists_heaven.admin.MonthlySalesDTO;
import com.artists_heaven.entities.user.User;
import com.artists_heaven.entities.user.UserRepository;
import com.artists_heaven.entities.user.UserRole;
import com.artists_heaven.order.Order;
import com.artists_heaven.order.OrderItem;
import com.artists_heaven.order.OrderStatus;
import com.artists_heaven.verification.VerificationStatus;

import java.util.UUID;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;

import jakarta.persistence.EntityNotFoundException;

class ArtistServiceTest {

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ArtistService artistService;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final String UPLOAD_DIR = "artists-heaven-backend/src/main/resources/mainArtist_media/";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterArtist_EmailAlreadyExists() {
        Artist artist = new Artist();
        artist.setEmail("test@example.com");

        ArtistRegisterDTO artistRegisterDTO = new ArtistRegisterDTO();
        artistRegisterDTO.setEmail("test@example.com");

        User userEmail = new User();
        userEmail.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com")).thenReturn(userEmail);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            artistService.registerArtist(artistRegisterDTO, artist);
        });

        assertEquals("El correo electrónico ya está registrado.", exception.getMessage());
    }

    @Test
    void testRegisterArtist_ArtistNameAlreadyExists() {
        Artist artist = new Artist();
        artist.setArtistName("existingArtist");

        ArtistRegisterDTO artistRegisterDTO = new ArtistRegisterDTO();
        artistRegisterDTO.setEmail("test@example.com");
        artistRegisterDTO.setArtistName("existingArtist");

        when(userRepository.findByEmail(anyString())).thenReturn(null);
        when(artistRepository.existsByArtistName("existingArtist")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            artistService.registerArtist(artistRegisterDTO, artist);
        });

        assertEquals("Ya existe un usuario con ese nombre registrado", exception.getMessage());
    }

    @Test
    void testRegisterArtist_Success() {
        Artist artist = new Artist();

        ArtistRegisterDTO artistRegisterDTO = new ArtistRegisterDTO();
        artistRegisterDTO.setEmail("newArtist@example.com");
        artistRegisterDTO.setArtistName("newArtist");
        artistRegisterDTO.setPassword("password");

        when(userRepository.findByEmail(anyString())).thenReturn(null);
        when(artistRepository.existsByArtistName(anyString())).thenReturn(false);
        when(artistRepository.save(any(Artist.class))).thenReturn(artist);

        Artist savedArtist = artistService.registerArtist(artistRegisterDTO, artist);

        assertNotNull(savedArtist);
        assertEquals("newArtist", savedArtist.getArtistName());
        assertEquals("newArtist", savedArtist.getUsername());
        assertEquals(UserRole.ARTIST, savedArtist.getRole());
        assertTrue(passwordEncoder.matches("password", savedArtist.getPassword()));
    }

    @Test
    void testFindById() {

        Artist artist = new Artist();
        artist.setArtistName("existingArtist");
        artist.setId(1L);

        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));
        Artist foundArtist = artistService.findById(1L);
        assertNotNull(foundArtist);
        assertEquals("existingArtist", foundArtist.getArtistName());

    }

    @Test
    void testFindByIdException() {

        Artist artist = new Artist();
        artist.setArtistName("existingArtist");

        when(artistRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            artistService.findById(1L);
        });

        assertEquals("Artist not found with id: 1", exception.getMessage());
    }

    @Test
    void testIsArtistVerificated_verifiedNoStatus() {
        // Simulamos que el artista está verificado
        when(artistRepository.isArtistVerificated(1L)).thenReturn(true);
        when(artistRepository.findLatestVerificationStatus(1L)).thenReturn(Collections.emptyList());

        // Llamada al servicio
        String result = artistService.isArtistVerificated(1L);

        // Verificamos que el resultado sea "Verified"
        assertEquals("Verified", result);
    }

    @Test
    void testIsArtistVerificated_notVerifiedNoStatus() {
        // Simulamos que el artista no está verificado
        when(artistRepository.isArtistVerificated(1L)).thenReturn(false);
        when(artistRepository.findLatestVerificationStatus(1L)).thenReturn(Collections.emptyList());

        // Llamada al servicio
        String result = artistService.isArtistVerificated(1L);

        // Verificamos que el resultado sea "Not Verified"
        assertEquals("Not Verified", result);
    }

    @Test
    void testIsArtistVerificated_notVerifiedWithStatus() {
        // Simulamos que el artista no está verificado
        when(artistRepository.isArtistVerificated(1L)).thenReturn(false);

        when(artistRepository.findLatestVerificationStatus(1L))
                .thenReturn(Collections.singletonList(VerificationStatus.PENDING));

        // Llamada al servicio
        String result = artistService.isArtistVerificated(1L);

        // Verificamos que el resultado sea el estado de verificación reciente
        assertEquals("PENDING", result);
    }

    @Test
    void testGetFutureEvents() {
        Integer year = 2025;

        when(artistRepository.findFutureEventsForArtist(1L, year)).thenReturn(1);
        Integer result = artistService.getFutureEvents(1L, year);

        assertNotNull(result);
        assertEquals(result, 1);
    }

    @Test
    void testGetPastEvents() {
        Integer year = 2024;

        when(artistRepository.findPastEventsForArtist(1L, year)).thenReturn(1);
        Integer result = artistService.getPastEvents(1L, year);

        assertNotNull(result);
        assertEquals(result, 1);
    }

    @Test
    void testGetOrderItemCount_noOrderItems() {
        // Configuramos un artista
        Artist artist = new Artist();
        artist.setArtistName("artistName");
        artist.setId(1L);

        // Mock de la búsqueda del artista
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));
        when(artistRepository.getOrdersPerYear("ARTISTNAME", 2023)).thenReturn(Collections.emptyList());

        // Llamada al servicio
        Map<String, Integer> result = artistService.getOrderItemCount(1L, 2023);

        // Verificamos que el mapa esté vacío
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetOrderItemCount_multipleOrderItems_noDuplicates() {
        // Configuramos un artista
        Artist artist = new Artist();
        artist.setArtistName("artistName");

        // Creamos una lista de OrderItems
        OrderItem item1 = new OrderItem();
        item1.setName("Item1");
        OrderItem item2 = new OrderItem();
        item2.setName("Item2");
        List<OrderItem> orderItems = Arrays.asList(item1, item2);

        // Mock de la búsqueda de los artículos de pedido
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));
        when(artistRepository.getOrdersPerYear("ARTISTNAME", 2023)).thenReturn(orderItems);

        // Llamada al servicio
        Map<String, Integer> result = artistService.getOrderItemCount(1L, 2023);

        // Verificamos que el mapa tenga las claves correctas y los valores esperados
        assertEquals(2, result.size());
        assertEquals(Integer.valueOf(1), result.get("Item1"));
        assertEquals(Integer.valueOf(1), result.get("Item2"));
    }

    @Test
    void testGetOrderItemCount_duplicateOrderItems() {
        // Configuramos un artista
        Artist artist = new Artist();
        artist.setArtistName("artistName");

        // Creamos una lista de OrderItems con duplicados
        OrderItem item1 = new OrderItem();
        item1.setName("Item1");
        OrderItem item2 = new OrderItem();
        item2.setName("Item1");
        List<OrderItem> orderItems = Arrays.asList(item1, item2);

        // Mock de la búsqueda de los artículos de pedido
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));
        when(artistRepository.getOrdersPerYear("ARTISTNAME", 2023)).thenReturn(orderItems);

        // Llamada al servicio
        Map<String, Integer> result = artistService.getOrderItemCount(1L, 2023);

        // Verificamos que el mapa tenga el ítem repetido con el conteo correcto
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(2), result.get("Item1"));
    }

    @Test
    void testGetOrderItemCount_artistNotFound() {
        // Configuramos el mock para que findById lance una excepción
        when(artistRepository.findById(1L)).thenThrow(new EntityNotFoundException("Artist not found with id: 1"));

        // Verificamos que se lanza la excepción correcta cuando se llama al servicio
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            artistService.getOrderItemCount(1L, 2023);
        });

        // Verificamos el mensaje de la excepción
        assertEquals("Artist not found with id: 1", exception.getMessage());
    }

    @Test
    void testGetMostCountrySold_noOrderItems() {
        // Configuramos un artista
        Artist artist = new Artist();
        artist.setArtistName("artistName");

        // Mock de la búsqueda de los artículos de pedido
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));
        when(artistRepository.getOrdersPerYear("ARTISTNAME", 2023)).thenReturn(Collections.emptyList());

        // Llamada al servicio
        Map<String, Integer> result = artistService.getMostCountrySold(1L, 2023);

        // Verificamos que el mapa esté vacío
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetMostCountrySold_multipleCountries() {
        // Configuramos un artista
        Artist artist = new Artist();
        artist.setArtistName("artistName");

        Order order1 = new Order();
        Order order2 = new Order();

        // Creamos una lista de OrderItems con diferentes países
        OrderItem item1 = new OrderItem();
        item1.setOrder(order1);
        item1.setName("Item1");
        item1.getOrder().setCountry("USA");
        OrderItem item2 = new OrderItem();
        item2.setOrder(order2);
        item2.setName("Item2");
        item2.getOrder().setCountry("Canada");
        List<OrderItem> orderItems = Arrays.asList(item1, item2);

        // Mock de la búsqueda de los artículos de pedido
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));
        when(artistRepository.getOrdersPerYear("ARTISTNAME", 2023)).thenReturn(orderItems);

        // Llamada al servicio
        Map<String, Integer> result = artistService.getMostCountrySold(1L, 2023);

        // Verificamos que el mapa tiene 2 países con las cantidades correctas
        assertEquals(2, result.size());
        assertEquals(Integer.valueOf(1), result.get("USA"));
        assertEquals(Integer.valueOf(1), result.get("Canada"));
    }

    @Test
    void testGetMostCountrySold_duplicateCountries() {
        // Configuramos un artista
        Artist artist = new Artist();
        artist.setArtistName("artistName");

        Order order1 = new Order();
        Order order2 = new Order();

        // Creamos una lista de OrderItems con países repetidos
        OrderItem item1 = new OrderItem();
        item1.setOrder(order1);
        item1.setName("Item1");
        item1.getOrder().setCountry("USA");
        OrderItem item2 = new OrderItem();
        item2.setOrder(order2);
        item2.setName("Item2");
        item2.getOrder().setCountry("USA");
        List<OrderItem> orderItems = Arrays.asList(item1, item2);

        // Mock de la búsqueda de los artículos de pedido
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));
        when(artistRepository.getOrdersPerYear("ARTISTNAME", 2023)).thenReturn(orderItems);

        // Llamada al servicio
        Map<String, Integer> result = artistService.getMostCountrySold(1L, 2023);

        // Verificamos que el país "USA" tiene la cantidad correcta
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(2), result.get("USA"));
    }

    @Test
    void testGetMostCountrySold_artistNotFound() {
        // Configuramos el mock para que findById lance una excepción
        when(artistRepository.findById(1L)).thenThrow(new EntityNotFoundException("Artist not found with id: 1"));

        // Verificamos que se lanza la excepción correcta cuando se llama al servicio
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            artistService.getMostCountrySold(1L, 2023);
        });

        // Verificamos el mensaje de la excepción
        assertEquals("Artist not found with id: 1", exception.getMessage());
    }

    @Test
    void testGetMonthlySalesDataPerArtist_noSalesData() {
        // Configuramos un artista
        Artist artist = new Artist();
        artist.setArtistName("artistName");

        // Mock de la búsqueda de los artículos de pedido
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));
        when(artistRepository.findMonthlySalesData("ARTISTNAME", 2023, OrderStatus.RETURN_ACCEPTED))
                .thenReturn(Collections.emptyList());

        // Llamada al servicio
        List<MonthlySalesDTO> result = artistService.getMonthlySalesDataPerArtist(1L, 2023);

        // Verificamos que la lista esté vacía
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetMonthlySalesDataPerArtist_withSalesData() {
        // Configuramos un artista
        Artist artist = new Artist();
        artist.setArtistName("artistName");

        // Creamos datos simulados de ventas
        Object[] result1 = { 1, 50L }; // Mes 1, 50 ventas
        Object[] result2 = { 2, 30L }; // Mes 2, 30 ventas
        List<Object[]> results = Arrays.asList(result1, result2);

        // Mock de la búsqueda de los artículos de pedido
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));
        when(artistRepository.findMonthlySalesData("ARTISTNAME", 2023, OrderStatus.RETURN_ACCEPTED))
                .thenReturn(results);

        // Llamada al servicio
        List<MonthlySalesDTO> result = artistService.getMonthlySalesDataPerArtist(1L, 2023);

        // Verificamos que la lista no esté vacía y contenga los datos correctos
        assertEquals(2, result.size());
        assertEquals(Integer.valueOf(1), result.get(0).getMonth());
        assertEquals(Long.valueOf(50), result.get(0).getTotalOrders());
        assertEquals(Integer.valueOf(2), result.get(1).getMonth());
        assertEquals(Long.valueOf(30), result.get(1).getTotalOrders());
    }

    @Test
    void testGetMonthlySalesDataPerArtist_artistNotFound() {
        // Configuramos el mock para que findById lance una excepción
        when(artistRepository.findById(1L)).thenThrow(new EntityNotFoundException("Artist not found with id: 1"));

        // Verificamos que se lanza la excepción correcta cuando se llama al servicio
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            artistService.getMonthlySalesDataPerArtist(1L, 2023);
        });

        // Verificamos el mensaje de la excepción
        assertEquals("Artist not found with id: 1", exception.getMessage());
    }

    @Test
    void testGetValidateArtist() {
        Artist artist = new Artist();
        artist.setIsVerificated(true);

        List<Artist> artists = Arrays.asList(artist);

        when(artistRepository.findValidaAritst()).thenReturn(artists);
        List<Artist> result = artistService.getValidArtists();

        assertNotNull(result);
        assertEquals(result.size(), 1);
    }

    @Test
    void testSaveImagesSuccess() {
        String originalFilename = "test.jpg";
        String sanitizedFilename = "test.jpg";
        String fileName = UUID.randomUUID().toString() + "_" + sanitizedFilename;
        Path targetPath = Paths.get(UPLOAD_DIR, fileName);

        MultipartFile newMultipartFile = new MockMultipartFile("file", originalFilename, "image/jpeg",
                new byte[] { 1, 2, 3, 4 });

        // Mock the static method Files.copy
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.copy(any(InputStream.class), eq(targetPath))).thenAnswer(invocation -> null);

            String imageUrl = artistService.saveImages(newMultipartFile);
            System.out.println(imageUrl);

            assertTrue(imageUrl.contains("/mainArtist_media/"));
            assertTrue(imageUrl.contains(sanitizedFilename));
        }
    }

    @Test
    void testSaveImagesInvalidFilename() {
        MultipartFile file = new MockMultipartFile("file", "", "image/jpeg", new byte[] { 1, 2, 3 });

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            artistService.saveImages(file);
        });

        assertEquals("The file name is invalid.", exception.getMessage());
    }

    @Test
    void testSaveImagesInvalidImage() {
        MultipartFile file = new MockMultipartFile("file", "bad.jpg", "image/jpeg", new byte[0]);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            artistService.saveImages(file);
        });

        assertEquals("The file is not a valid image.", exception.getMessage());
    }


}
