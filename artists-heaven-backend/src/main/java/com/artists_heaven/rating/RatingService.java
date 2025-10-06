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
import com.github.pemistahl.lingua.api.LanguageDetector;
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;
import com.modernmt.text.profanity.ProfanityFilter;
import com.github.pemistahl.lingua.api.Language;

@Service
public class RatingService {

    private final OrderRepository orderRepository;

    private final ProductRepository productRepository;

    private final UserService userService;

    private final RatingRepository ratingRepository;

    private final MessageSource messageSource;

    private final ProfanityFilter profanityFilter = new ProfanityFilter();
    LanguageDetector detector = LanguageDetectorBuilder
            .fromLanguages(Language.ENGLISH, Language.SPANISH, Language.FRENCH, Language.GERMAN, Language.ITALIAN)
            .build();

    public RatingService(OrderRepository orderRepository,
            ProductRepository productRepository, UserService userService, AdminController adminController,
            RatingRepository ratingRepository, MessageSource messageSource) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userService = userService;
        this.ratingRepository = ratingRepository;
        this.messageSource = messageSource;
    }

    /**
     * Returns all ratings for a given product.
     *
     * @param productId the ID of the product
     * @return a list of ratings for the product
     * @throws AppExceptions.ResourceNotFoundException if the product does not exist
     */
    public List<Rating> getProductRatings(Long productId) {
        return productRepository.findById(productId)
                .map(Product::getRatings)
                .orElseThrow(
                        () -> new AppExceptions.ResourceNotFoundException("Product not found with id: " + productId));
    }

    /**
     * Creates a rating for a product by a user.
     *
     * Rules:
     * <ul>
     * <li>The comment must not contain profanity.</li>
     * <li>The user must have purchased the product.</li>
     * <li>The user can only rate a product once.</li>
     * </ul>
     *
     * @param userId    the ID of the user making the rating
     * @param productId the ID of the product being rated
     * @param score     the rating score (e.g., 1-5)
     * @param comment   the rating comment
     * @param lang      the language code for localized messages
     * @return the created Rating
     * @throws AppExceptions.InvalidInputException     if the comment contains
     *                                                 profanity
     * @throws AppExceptions.ForbiddenActionException  if the user has not purchased
     *                                                 the product
     * @throws AppExceptions.DuplicateActionException  if the user already rated
     *                                                 this product
     * @throws AppExceptions.ResourceNotFoundException if the product does not exist
     */
    public Rating createRating(Long userId, Long productId, Integer score, String comment, String lang) {
        Locale locale = new Locale(lang);
        String detectedLang = detector.detectLanguageOf(comment).getIsoCode639_1().toString().toLowerCase();

        if (profanityFilter.test(detectedLang, comment)) {
            String msg = messageSource.getMessage("rating.bad_comment", null, locale);
            throw new AppExceptions.InvalidInputException(msg);
        }

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

    /**
     * Checks if a user has already rated a product.
     *
     * @param userId    the user's ID
     * @param productId the product's ID
     * @return true if the user already rated the product
     */
    private boolean checkUserRatingProduct(Long userId, Long productId) {
        return ratingRepository.findByProductIdAndUserId(productId, userId).isPresent();
    }

    /**
     * Checks if a user has purchased a product.
     *
     * @param userId    the user's ID
     * @param productId the product's ID
     * @return true if the user purchased the product
     */
    private Boolean checkUserPurchaseItem(Long userId, Long productId) {
        return orderRepository.getOrdersByUserId(userId).stream()
                .flatMap(order -> order.getItems().stream())
                .anyMatch(orderItem -> orderItem.getProductId().equals(productId));
    }
}
