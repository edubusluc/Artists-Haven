package com.artists_heaven.rating;

import java.util.List;

import org.springframework.stereotype.Service;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.entities.user.UserService;
import com.artists_heaven.order.Order;
import com.artists_heaven.order.OrderRepository;
import com.artists_heaven.product.Product;
import com.artists_heaven.product.ProductRepository;

@Service
public class RatingService {

    private final RatingRepository ratingRepository;

    private final OrderRepository orderRepository;

    private final ProductRepository productRepository;

    private final UserService userService;

    public RatingService(RatingRepository ratingRepository, OrderRepository orderRepository,
            ProductRepository productRepository, UserService userService) {
        this.ratingRepository = ratingRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userService = userService;
    }

    public List<Rating> getProductRatings(Long productId) {
        return productRepository.findById(productId).get().getRatings();
    }

    public Rating createRating(Long userId, Long productId, Integer score, String comment) {
        Rating rating = new Rating();
        if (!checkUserPurchaseItem(userId, productId)) {
            throw new RuntimeException("User has not purchased this item");
        }

        User user = userService.getUserById(userId);

        rating.setScore(score);
        rating.setComment(comment);
        rating.setUser(user);

        Product product = productRepository.findById(productId).get();
        product.getRatings().add(rating);
        productRepository.save(product);

        return ratingRepository.save(rating);
    }

    private Boolean checkUserPurchaseItem(Long userId, Long productId) {
        List<Order> orders = orderRepository.getOrdersByUserId(userId);
    
        return orders.stream()
            .flatMap(order -> order.getItems().stream()) // Desempaquetamos los items de cada orden
            .anyMatch(item -> item.getProductId().equals(productId)); // Comprobamos si algún item coincide con el producto
    }
    

}
