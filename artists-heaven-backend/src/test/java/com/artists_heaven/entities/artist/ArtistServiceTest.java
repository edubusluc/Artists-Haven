package com.artists_heaven.entities.artist;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.artists_heaven.admin.MonthlySalesDTO;
import com.artists_heaven.entities.user.User;
import com.artists_heaven.entities.user.UserRepository;
import com.artists_heaven.entities.user.UserRole;
import com.artists_heaven.event.Event;
import com.artists_heaven.event.EventService;
import com.artists_heaven.exception.AppExceptions;
import com.artists_heaven.exception.AppExceptions.DuplicateActionException;
import com.artists_heaven.exception.AppExceptions.ResourceNotFoundException;
import com.artists_heaven.images.ImageServingUtil;
import com.artists_heaven.order.Order;
import com.artists_heaven.order.OrderItem;
import com.artists_heaven.order.OrderStatus;
import com.artists_heaven.product.CategoryRepository;
import com.artists_heaven.product.Product;
import com.artists_heaven.product.ProductService;
import com.artists_heaven.verification.Verification;
import com.artists_heaven.verification.VerificationRepository;
import com.artists_heaven.verification.VerificationStatus;

import jakarta.persistence.EntityNotFoundException;

class ArtistServiceTest {

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VerificationRepository verificationRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private MessageSource messageSource;

    @Mock
    private EventService eventService;

    @Mock
    private ProductService productService;

    @InjectMocks
    private ArtistService artistService;

    @Mock
    private ImageServingUtil imageServingUtil;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

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

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(userEmail));

        when(messageSource.getMessage(
                eq("email.duplicate"),
                any(),
                any(Locale.class))).thenReturn("El correo electrónico ya está registrado.");

        DuplicateActionException exception = assertThrows(DuplicateActionException.class, () -> {
            artistService.registerArtist(artistRegisterDTO, "es");
        });

        assertEquals("El correo electrónico ya está registrado.", exception.getMessage());
    }

    @Test
    void testRegisterArtist_ArtistNameAlreadyExists() {
        ArtistRegisterDTO artistRegisterDTO = new ArtistRegisterDTO();
        artistRegisterDTO.setEmail("test@example.com");
        artistRegisterDTO.setArtistName("existingArtist");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(artistRepository.existsByArtistName("existingArtist")).thenReturn(true);
        when(messageSource.getMessage(
                eq("artistName.alreadyExists"),
                any(),
                any(Locale.class)))
                .thenReturn("Ya existe un usuario con ese nombre registrado");

        DuplicateActionException exception = assertThrows(DuplicateActionException.class, () -> {
            artistService.registerArtist(artistRegisterDTO, "es");
        });

        assertEquals("Ya existe un usuario con ese nombre registrado", exception.getMessage());
    }

    @Test
    void testRegisterArtist_Success() {
        Artist savedArtistMock = new Artist();
        savedArtistMock.setArtistName("newArtist");
        savedArtistMock.setUsername("newArtist");
        savedArtistMock.setRole(UserRole.ARTIST);
        savedArtistMock.setPassword(passwordEncoder.encode("password"));

        ArtistRegisterDTO artistRegisterDTO = new ArtistRegisterDTO();
        artistRegisterDTO.setEmail("newArtist@example.com");
        artistRegisterDTO.setArtistName("newArtist");
        artistRegisterDTO.setPassword("password");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(artistRepository.existsByArtistName(anyString())).thenReturn(false);
        when(artistRepository.save(any(Artist.class))).thenReturn(savedArtistMock);

        Artist result = artistService.registerArtist(artistRegisterDTO, "es");

        assertNotNull(savedArtistMock);
        assertNotNull(result);
        assertEquals("newArtist", result.getArtistName());
        assertEquals("newArtist", result.getUsername());
        assertEquals(UserRole.ARTIST, result.getRole());
        assertTrue(passwordEncoder.matches("password", savedArtistMock.getPassword()));
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

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
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

        when(artistRepository.findValidAritst()).thenReturn(artists);
        List<Artist> result = artistService.getValidArtists();

        assertNotNull(result);
        assertEquals(result.size(), 1);
    }

    @Test
    void validateArtist_SuccessfulFlow() {
        // Arrange
        Long artistId = 1L;
        Long verificationId = 2L;

        Artist artist = new Artist();
        artist.setId(artistId);
        artist.setArtistName("Juan Perez");
        artist.setIsVerificated(false);

        Verification verification = new Verification();
        verification.setId(verificationId);
        verification.setStatus(VerificationStatus.PENDING);

        when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));
        when(verificationRepository.findById(verificationId)).thenReturn(Optional.of(verification));

        // Act
        artistService.validateArtist(artistId, verificationId);

        // Assert
        assertTrue(artist.getIsVerificated());
        assertEquals(VerificationStatus.ACCEPTED, verification.getStatus());
        verify(artistRepository).save(artist);
        verify(categoryRepository).save(argThat(cat -> cat.getName().equals("JUANPEREZ")));
        verify(verificationRepository).save(verification);
    }

    @Test
    void validateArtist_ArtistNotFound_ThrowsException() {
        // Arrange
        when(artistRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        AppExceptions.ResourceNotFoundException ex = assertThrows(
                AppExceptions.ResourceNotFoundException.class,
                () -> artistService.validateArtist(1L, 2L));
        assertEquals("Artist not found", ex.getMessage());
        verify(artistRepository, never()).save(any());
        verify(categoryRepository, never()).save(any());
        verify(verificationRepository, never()).save(any());
    }

    @Test
    void validateArtist_VerificationNotFound_ThrowsException() {
        // Arrange
        Long artistId = 1L;
        Long verificationId = 2L;

        Artist artist = new Artist();
        artist.setId(artistId);
        artist.setArtistName("Juan Perez");
        artist.setIsVerificated(false);

        when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));
        when(verificationRepository.findById(verificationId)).thenReturn(Optional.empty());

        // Act & Assert
        AppExceptions.ResourceNotFoundException ex = assertThrows(
                AppExceptions.ResourceNotFoundException.class,
                () -> artistService.validateArtist(artistId, verificationId));
        assertEquals("Verification not found", ex.getMessage());

        verify(artistRepository).save(artist);
        verify(categoryRepository).save(any());
        verify(verificationRepository, never()).save(any());
    }

    @Test
    void getArtistWithDetails_SuccessfulFlow() {
        // Arrange
        Long artistId = 1L;

        Artist artist = new Artist();
        artist.setId(artistId);
        artist.setArtistName("Juan Perez");
        artist.setMainColor("blue");
        artist.setBannerPhoto("banner.jpg");

        List<Product> products = List.of(new Product(), new Product());
        List<Event> events = List.of(new Event(), new Event());

        when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));
        when(productService.findProductsByArtist("juanperez")).thenReturn(products);
        when(eventService.findEventThisYearByArtist(artistId)).thenReturn(events);

        // Act
        ArtistDTO result = artistService.getArtistWithDetails(artistId);

        // Assert
        assertNotNull(result);
        assertEquals("Juan Perez", result.getArtistName());
        assertEquals("blue", result.getPrimaryColor());
        assertEquals("banner.jpg", result.getBannerPhoto());
        assertEquals(events, result.getArtistEvents());

        verify(artistRepository).findById(artistId);
        verify(productService).findProductsByArtist("JUANPEREZ");
        verify(eventService).findEventThisYearByArtist(artistId);
    }

    @Test
    void getArtistWithDetails_ArtistNotFound_ThrowsException() {
        // Arrange
        when(artistRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        AppExceptions.ResourceNotFoundException ex = assertThrows(
                AppExceptions.ResourceNotFoundException.class,
                () -> artistService.getArtistWithDetails(1L));
        assertEquals("Artist not found with id: 1", ex.getMessage());
        verify(productService, never()).findProductsByArtist(any());
        verify(eventService, never()).findEventThisYearByArtist(any());
    }

    @Test
    void getArtistDashboard_SuccessfulFlow() {
        // Arrange
        Long artistId = 1L;
        int year = 2025;

        Artist artist = new Artist();
        artist.setId(artistId);
        artist.setArtistName("Artst Name");

        Order order = new Order();
        order.setCreatedDate(LocalDateTime.now());
        order.setCountry("ESP");

        OrderItem orderItem = new OrderItem();
        orderItem.setName("OrderItem");
        orderItem.setOrder(order);

        when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));
        when(artistRepository.findFutureEventsForArtist(artistId, year)).thenReturn(10);
        when(artistRepository.findPastEventsForArtist(artistId, year)).thenReturn(5);
        when(artistRepository.isArtistVerificated(artistId)).thenReturn(true);
        when(artistRepository.findLatestVerificationStatus(artistId))
                .thenReturn(List.of(VerificationStatus.ACCEPTED));
        when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));
        when(artistRepository.getOrdersPerYear(eq(artist.getArtistName().toUpperCase()), eq(year)))
                .thenReturn(List.of(orderItem));

        // Act
        ArtistDashboardDTO result = artistService.getArtistDashboard(artistId, year);

        // Assert
        assertNotNull(result);
        assertEquals(10, result.getFutureEvents());
        assertEquals(5, result.getPastEvents());
        assertEquals(1, result.getOrderItemCount().get("OrderItem"));
        assertEquals(1, result.getMostCountrySold().get("ESP"));
    }

}
