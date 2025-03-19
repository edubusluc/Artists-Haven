package com.artists_heaven.rating;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.junit.jupiter.api.Test;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.entities.user.UserService;
import com.artists_heaven.order.Order;
import com.artists_heaven.order.OrderItem;
import com.artists_heaven.order.OrderRepository;
import com.artists_heaven.product.Product;
import com.artists_heaven.product.ProductRepository;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RatingServiceTest {

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserService userService;

    @Mock
    private OrderRepository orderRepository;

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
        order.setUserId(user.getId());
        order.setItems(new ArrayList<>(List.of(item)));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userService.getUserById(1L)).thenReturn(user);
        when(orderRepository.getOrdersByUserId(1L)).thenReturn(List.of(order));
        when(ratingRepository.save(any(Rating.class))).thenReturn(rating);

        Rating result = ratingService.createRating(1L, 1L, 5, "Great product");

        assertNotNull(result);
        assertEquals(5, result.getScore());
        assertEquals("Great product", result.getComment());

        verify(productRepository, times(1)).findById(1L);
        verify(userService, times(1)).getUserById(1L);
        verify(orderRepository, times(1)).getOrdersByUserId(1L);
        verify(ratingRepository, times(1)).save(any(Rating.class));
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void createRatingTest_UserHasNotPurchasedItem() {
        User user = new User();
        user.setId(1L);

        when(userService.getUserById(1L)).thenReturn(user);
        when(orderRepository.getOrdersByUserId(1L)).thenReturn(new ArrayList<>());

        try {
            ratingService.createRating(1L, 1L, 5, "Great product");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("User has not purchased this item"));
        }

        verify(productRepository, times(0)).findById(1L);
        verify(orderRepository, times(1)).getOrdersByUserId(1L);
        verify(ratingRepository, times(0)).save(any(Rating.class));
        verify(productRepository, times(0)).save(any(Product.class));
    }

}
