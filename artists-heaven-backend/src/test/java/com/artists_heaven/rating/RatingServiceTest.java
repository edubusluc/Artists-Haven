package com.artists_heaven.rating;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.junit.jupiter.api.Test;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.entities.user.UserService;
import com.artists_heaven.exception.AppExceptions.DuplicateActionException;
import com.artists_heaven.exception.AppExceptions.ForbiddenActionException;
import com.artists_heaven.exception.AppExceptions.ResourceNotFoundException;
import com.artists_heaven.order.Order;
import com.artists_heaven.order.OrderItem;
import com.artists_heaven.order.OrderRepository;
import com.artists_heaven.product.Product;
import com.artists_heaven.product.ProductRepository;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

class RatingServiceTest {

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserService userService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private RatingService ratingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetProductRatings() {
        Rating rating = new Rating();
        rating.setScore(5);
        rating.setComment("Great product");
        List<Rating> ratings = List.of(rating);

        Product product = new Product();
        product.setRatings(ratings);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        List<Rating> result = ratingService.getProductRatings(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(5, result.get(0).getScore());
        assertEquals("Great product", result.get(0).getComment());
    }

    @Test
    void createRatingTest() {
        Rating rating = new Rating();
        rating.setScore(5);
        rating.setComment("Great product");

        Product product = new Product();
        product.setId(1L);
        product.setRatings(new ArrayList<>());

        User user = new User();
        user.setId(1L);
        OrderItem item = new OrderItem();
        item.setProductId(product.getId());

        Order order = new Order();
        order.setUser(user);
        order.setItems(new ArrayList<>(List.of(item)));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userService.getUserById(1L)).thenReturn(user);
        when(orderRepository.getOrdersByUserId(1L)).thenReturn(List.of(order));
        when(ratingRepository.save(any(Rating.class))).thenReturn(rating);

        Rating result = ratingService.createRating(1L, 1L, 5, "Great product", "es");

        assertNotNull(result);
        assertEquals(5, result.getScore());
        assertEquals("Great product", result.getComment());

        verify(productRepository, times(1)).findById(1L);
        verify(userService, times(1)).getUserById(1L);
        verify(orderRepository, times(1)).getOrdersByUserId(1L);
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void createRatingTest_UserHasNotPurchasedItem() {
        // Preparación de datos
        User user = new User();
        user.setId(1L);

        // Mock de los servicios
        when(userService.getUserById(1L)).thenReturn(user);
        when(orderRepository.getOrdersByUserId(1L)).thenReturn(new ArrayList<>());
        when(messageSource.getMessage(
                eq("rating.not_allowed"),
                any(),
                any(Locale.class))).thenReturn("No puedes dejar una reseña porque no has comprado este producto.");

        // Llamada al servicio y verificación de la excepción
        ForbiddenActionException exception = assertThrows(ForbiddenActionException.class, () -> {
            ratingService.createRating(1L, 1L, 5, "Excelente producto", "es");
        });

        // Verificación del mensaje de la excepción
        assertEquals("No puedes dejar una reseña porque no has comprado este producto.", exception.getMessage());

        // Verificar interacciones con los repositorios
        verify(productRepository, times(0)).findById(1L);
        verify(orderRepository, times(1)).getOrdersByUserId(1L);
        verify(ratingRepository, times(0)).save(any(Rating.class));
        verify(productRepository, times(0)).save(any(Product.class));
    }

    @Test
    void createRatingTest_UserAlreadyRatedProduct() {
        User user = new User();
        user.setId(1L);

        Product product = new Product();
        product.setId(1L);

        OrderItem orderItem = new OrderItem();
        orderItem.setProductId(1L);

        Order order = new Order();
        order.setUser(user);
        order.setItems(List.of(orderItem));

        Rating rating = new Rating();

        when(ratingRepository.findByProductIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(rating));
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        when(orderRepository.getOrdersByUserId(1L)).thenReturn(List.of(mock(Order.class))); // simula que compró el
                                                                                            // producto
        when(messageSource.getMessage(
                eq("rating.already"),
                any(),
                any(Locale.class))).thenReturn("Ya has valorado este producto.");

        when(orderRepository.getOrdersByUserId(anyLong())).thenReturn(List.of(order));

        DuplicateActionException exception = assertThrows(DuplicateActionException.class, () -> {
            ratingService.createRating(1L, 1L, 5, "Excelente producto", "es");
        });

        assertEquals("Ya has valorado este producto.", exception.getMessage());

        verify(productRepository, times(0)).findById(1L);
        verify(ratingRepository, times(0)).save(any(Rating.class));
        verify(productRepository, times(0)).save(any(Product.class));
    }

    @Test
    void createRatingTest_ProductNotFound() {
        User user = new User();
        user.setId(1L);

        OrderItem orderItem = new OrderItem();
        orderItem.setProductId(1L);

        Order order = new Order();
        order.setUser(user);
        order.setItems(List.of(orderItem));

        when(userService.getUserById(1L)).thenReturn(user);
        when(orderRepository.getOrdersByUserId(anyLong())).thenReturn(List.of(order));

        when(productRepository.findById(1L)).thenReturn(Optional.empty());
        when(messageSource.getMessage(
                eq("product.not_found"),
                any(),
                any(Locale.class))).thenReturn("Producto no encontrado.");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            ratingService.createRating(1L, 1L, 5, "Excelente producto", "es");
        });

        assertEquals("Producto no encontrado.", exception.getMessage());
        verify(productRepository, times(1)).findById(1L);
        verify(ratingRepository, times(0)).save(any(Rating.class));
    }

    @Test
    void testRatingResponseDTO() {
        User user = new User();
        user.setUsername("userTest");

        Rating rating = new Rating();
        rating.setComment("Comment Test");
        rating.setId(1L);
        rating.setScore(5);
        rating.setCreatedAt(LocalDate.now());
        rating.setUser(user);

        RatingResponseDTO ratingResponseDTO = new RatingResponseDTO(rating);
        assertTrue(ratingResponseDTO.getUsername().equals("userTest"));
    }

    @Test
    public void testGetUserEmail_WhenUserIsPresent() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");

        Rating rating = new Rating();
        rating.setUser(user);

        // Act
        String email = rating.getUserEmail();

        // Assert
        assertEquals("test@example.com", email);
    }

    @Test
    public void testGetUserEmail_WhenUserIsNull() {
        // Arrange
        Rating rating = new Rating();
        rating.setUser(null);

        // Act
        String email = rating.getUserEmail();

        // Assert
        assertNull(email);
    }

}
