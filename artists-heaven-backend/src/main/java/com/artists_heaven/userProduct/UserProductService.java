package com.artists_heaven.userProduct;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Date;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.entities.user.UserRepository;
import com.artists_heaven.exception.AppExceptions;
import com.artists_heaven.images.ImageServingUtil;
import com.artists_heaven.productVote.ProductVoteRepository;

@Service
public class UserProductService {

    private final UserProductRepository userProductRepository;

    private final ProductVoteRepository productVoteRepository;

    private final UserRepository userRepository;

    private final ImageServingUtil imageServingUtil;

    private final MessageSource messageSource;

    public UserProductService(UserProductRepository userProductRepository, UserRepository userRepository,
            ProductVoteRepository productVoteRepository, MessageSource messageSource,
            ImageServingUtil imageServingUtil) {
        this.userProductRepository = userProductRepository;
        this.userRepository = userRepository;
        this.productVoteRepository = productVoteRepository;
        this.messageSource = messageSource;
        this.imageServingUtil = imageServingUtil;
    }

    /**
     * Creates a new user product and associates it with the given user.
     * Awards points to users based on conditions and enforces monthly creation
     * limits.
     *
     * @param userProductDTO DTO containing product details
     * @param userId         ID of the user creating the product
     * @param lang           language code for localized messages
     * @return the created UserProduct
     * @throws AppExceptions.ResourceNotFoundException if the user does not exist
     * @throws AppExceptions.LimitExceededException    if the user exceeds the
     *                                                 monthly product limit
     */
    public UserProduct createUserProduct(UserProductDTO userProductDTO, Long userId, String lang) {
        Locale locale = new Locale(lang);

        String userNotFound = messageSource.getMessage("user.NotFound", null, locale);
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException(userNotFound));

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startOfMonth = calendar.getTime();

        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.MILLISECOND, -1);
        Date endOfMonth = calendar.getTime();

        Long monthlyProductCount = userProductRepository.countByOwnerIdAndCreatedAtBetween(userId, startOfMonth,
                endOfMonth);
        String limitExceed = messageSource.getMessage("userProduct.limitExceed", null, locale);
        if (monthlyProductCount >= 3) {
            throw new AppExceptions.LimitExceededException(limitExceed);
        }

        Long productCount = userProductRepository.countByOwnerId(userId);
        if (productCount == 0) {
            owner.setPoints(owner.getPoints() + 100);
            userRepository.save(owner);
        }

        UserProduct userProduct = new UserProduct();
        userProduct.setName(userProductDTO.getName());
        userProduct.setImages(userProductDTO.getImages());
        userProduct.setOwner(owner);
        return userProductRepository.save(userProduct);
    }

    /**
     * Retrieves all user-submitted products.
     *
     * @return a list of all UserProduct entities
     */
    public List<UserProduct> getAllUserProducts() {
        return userProductRepository.findAll();
    }

    /**
     * Saves a list of image files to the server and returns their URLs.
     *
     * @param images list of images to save
     * @return list of URLs for the saved images
     */
    public List<String> saveImages(List<MultipartFile> images) {
        List<String> imageUrls = new ArrayList<>();

        for (MultipartFile image : images) {
            String imageUrl = imageServingUtil.saveMediaFile(
                    image,
                    "userProduct_media",
                    "/userProduct_media/",
                    false);

            imageUrls.add(imageUrl);
        }

        return imageUrls;
    }

    /**
     * Retrieves the number of votes for each product in the given list of product
     * IDs.
     *
     * @param productIds list of product IDs
     * @return a map with product ID as key and vote count as value
     */
    public Map<Long, Integer> getVoteCountsForProducts(List<Long> productIds) {
        List<Object[]> voteCounts = productVoteRepository.countVotesForProducts(productIds);

        return voteCounts.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Long) row[1]).intValue()));
    }

    /**
     * Retrieves detailed information about all user products submitted within the
     * last month.
     * Includes the number of votes for each product and whether the given user has
     * voted for them.
     *
     * @param userId ID of the logged-in user (nullable)
     * @return a list of UserProductDetailsDTO containing product details and vote
     *         information
     */
    public List<UserProductDetailsDTO> getAllUserProductDetails(Long userId) {
        LocalDate oneMonthAgo = LocalDate.now().minusMonths(1);
        Date oneMonthAgoDate = Date.from(oneMonthAgo.atStartOfDay(ZoneId.systemDefault()).toInstant());

        List<UserProduct> userProducts = userProductRepository.findAllByMonthAndAccepted(oneMonthAgoDate);

        List<Long> productIds = userProducts.stream()
                .map(UserProduct::getId)
                .toList();

        Map<Long, Integer> votesMap = getVoteCountsForProducts(productIds);

        Set<Long> votedProducts = (userId != null)
                ? productVoteRepository.findProductIdsByUserId(userId)
                : Set.of();

        return userProducts.stream()
                .map(product -> {
                    Integer votes = votesMap.getOrDefault(product.getId(), 0);
                    boolean votedByUser = (userId != null) && votedProducts.contains(product.getId());
                    return new UserProductDetailsDTO(product, votes, votedByUser);
                })
                .toList();
    }

    /**
     * Retrieves all user products that are pending verification.
     *
     * @return a list of UserProduct entities pending approval
     */
    public List<UserProduct> findUserProductPending() {
        return userProductRepository.getAllPendingVerification();
    }

    /**
     * Approves a pending user product.
     * Updates the product status to ACCEPTED and awards points to the owner.
     *
     * @param productId ID of the product to approve
     * @return the updated UserProduct
     * @throws AppExceptions.ResourceNotFoundException if the product does not exist
     */
    public UserProduct approveProduct(Long productId) {
        UserProduct product = userProductRepository.findById(productId)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Producto no encontrado"));
        product.setStatus(Status.ACCEPTED);
        User user = product.getOwner();
        user.setPoints(user.getPoints() + 20);
        userRepository.save(user);
        return userProductRepository.save(product);
    }

    public UserProduct rejectProduct(Long productId) {
        UserProduct product = userProductRepository.findById(productId)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Producto no encontrado"));
        product.setStatus(Status.REJECTED);
        User user = product.getOwner();
        user.setPoints(user.getPoints() - 10);
        userRepository.save(user);
        return userProductRepository.save(product);
    }

    /**
     * Rejects a pending user product.
     * Updates the product status to REJECTED and deducts points from the owner.
     *
     * @param productId ID of the product to reject
     * @return the updated UserProduct
     * @throws AppExceptions.ResourceNotFoundException if the product does not exist
     */
    public List<UserProductDetailsDTO> findMyUserProducts(Long userId) {
        List<UserProduct> myUserProducts = userProductRepository.findByOwnerIdOrderByCreatedAtDesc(userId);

        List<Long> productIds = myUserProducts.stream()
                .map(UserProduct::getId)
                .toList();

        Map<Long, Integer> votesMap = getVoteCountsForProducts(productIds);
        return myUserProducts.stream()
                .map(product -> {
                    Integer votes = votesMap.getOrDefault(product.getId(), 0);
                    return new UserProductDetailsDTO(product, votes);
                })
                .toList();
    }
}
