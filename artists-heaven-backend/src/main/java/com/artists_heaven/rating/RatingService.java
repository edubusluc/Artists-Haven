package com.artists_heaven.rating;

import java.util.List;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import com.artists_heaven.admin.AdminController;
import com.artists_heaven.entities.user.User;
import com.artists_heaven.entities.user.UserService;
import com.artists_heaven.exception.AppExceptions;
import com.artists_heaven.order.OrderRepository;
import com.artists_heaven.product.Product;
import com.artists_heaven.product.ProductRepository;

@Service
public class RatingService {

    private final OrderRepository orderRepository;

    private final ProductRepository productRepository;

    private final UserService userService;

    private final RatingRepository ratingRepository;

    private final MessageSource messageSource;

    public RatingService(OrderRepository orderRepository,
            ProductRepository productRepository, UserService userService, AdminController adminController,
            RatingRepository ratingRepository, MessageSource messageSource) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userService = userService;
        this.ratingRepository = ratingRepository;
        this.messageSource = messageSource;
    }

    public List<Rating> getProductRatings(Long productId) {
        return productRepository.findById(productId)
                .map(Product::getRatings)
                .orElseThrow(
                        () -> new AppExceptions.ResourceNotFoundException("Product not found with id: " + productId));
    }

    public Rating createRating(Long userId, Long productId, Integer score, String comment, String lang) {
        Locale locale = new Locale(lang);

        if (!checkUserPurchaseItem(userId, productId)) {
            String msg = messageSource.getMessage("rating.not_allowed", null, locale);
            throw new AppExceptions.ForbiddenActionException(msg);
        }

        if (checkUserRatingProduct(userId, productId)) {
            String msg = messageSource.getMessage("rating.already", null, locale);
            throw new AppExceptions.DuplicateActionException(msg);
        }

        User user = userService.getUserById(userId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    String msg = messageSource.getMessage("product.not_found", null, locale);
                    return new AppExceptions.ResourceNotFoundException(msg);
                });

        Rating rating = new Rating();
        rating.setScore(score);
        rating.setComment(comment);
        rating.setUser(user);
        rating.setProduct(product);

        product.getRatings().add(rating);
        productRepository.save(product);

        return rating;
    }

    private boolean checkUserRatingProduct(Long userId, Long productId) {
        return ratingRepository.findByProductIdAndUserId(productId, userId).isPresent();
    }

    private Boolean checkUserPurchaseItem(Long userId, Long productId) {
        return orderRepository.getOrdersByUserId(userId).stream()
                .flatMap(order -> order.getItems().stream())
                .anyMatch(orderItem -> orderItem.getProductId().equals(productId));
    }
}
