package com.artists_heaven.userProduct;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Date;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.entities.user.UserRepository;
import com.artists_heaven.exception.AppExceptions;
import com.artists_heaven.productVote.ProductVoteRepository;

@Service
public class UserProductService {

    private final UserProductRepository userProductRepository;

    private final ProductVoteRepository productVoteRepository;

    private final UserRepository userRepository;

    private final MessageSource messageSource;

    private static final String UPLOAD_DIR = "artists-heaven-backend/src/main/resources/userProduct_media/";
    private static final Path TARGET_PATH = new File(UPLOAD_DIR).toPath().normalize();

    public UserProductService(UserProductRepository userProductRepository, UserRepository userRepository,
            ProductVoteRepository productVoteRepository, MessageSource messageSource) {
        this.userProductRepository = userProductRepository;
        this.userRepository = userRepository;
        this.productVoteRepository = productVoteRepository;
        this.messageSource = messageSource;
    }

    public UserProduct createUserProduct(UserProductDTO userProductDTO, Long userId, String lang) {
        Locale locale = new Locale(lang);

        // buscar el usuario
        String userNotFound = messageSource.getMessage("user.NotFound", null, locale);
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException(userNotFound));

        // Obtener el inicio y fin del mes actual
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

    public List<UserProduct> getAllUserProducts() {
        return userProductRepository.findAll();
    }

    public List<String> saveImages(List<MultipartFile> images) {
        List<String> imageUrls = new ArrayList<>();
        // Validate that all images are valid before proceeding
        validateImages(images);

        // Iterate through each image and save it
        for (MultipartFile image : images) {
            // Clean the filename to ensure it is safe to use
            String fileName = StringUtils.cleanPath(image.getOriginalFilename());
            String extension = "";

            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex > 0) {
                extension = fileName.substring(dotIndex);
            }

            String uniqueFileName = UUID.randomUUID().toString() + extension;

            Path targetPath = Paths.get(UPLOAD_DIR, uniqueFileName).normalize();

            // Ensure the file is not saved outside the target directory for security
            if (!targetPath.startsWith(TARGET_PATH)) {
                throw new IllegalArgumentException("Entry is outside of the target directory");
            }

            try {
                // Save the image to the directory
                Files.copy(image.getInputStream(), targetPath);
                // Add the image's URL to the list (adjust the URL according to the system)
                imageUrls.add("/userProduct_media/" + uniqueFileName);
            } catch (IOException e) {
                // Throw an exception if an error occurs while saving the image
                throw new IllegalArgumentException("Error while saving images.");
            }
        }

        // Return the list of image URLs after all images are saved
        return imageUrls;
    }

    /**
     * Validates the list of images to ensure none of them are empty.
     *
     * @param images List of images to be validated.
     * @throws IllegalArgumentException if any image is empty.
     */
    private void validateImages(List<MultipartFile> images) {
        // Iterate through the list of images
        for (MultipartFile image : images) {
            // Check if any image is empty
            if (image.isEmpty()) {
                // Throw an exception if an image is empty
                throw new IllegalArgumentException("Images cannot be empty.");
            }
        }
    }

    public Map<Long, Integer> getVoteCountsForProducts(List<Long> productIds) {
        List<Object[]> voteCounts = productVoteRepository.countVotesForProducts(productIds);

        return voteCounts.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Long) row[1]).intValue()));
    }

    public List<UserProductDetailsDTO> getAllUserProductDetails(Long userId) {
        LocalDate oneMonthAgo = LocalDate.now().minusMonths(1);
        Date oneMonthAgoDate = Date.from(oneMonthAgo.atStartOfDay(ZoneId.systemDefault()).toInstant());

        List<UserProduct> userProducts = userProductRepository.findAllByMonthAndAccepted(oneMonthAgoDate);

        // Extraer IDs de los productos
        List<Long> productIds = userProducts.stream()
                .map(UserProduct::getId)
                .toList();

        // Obtener conteo de votos por producto
        Map<Long, Integer> votesMap = getVoteCountsForProducts(productIds);

        // Si el usuario está logueado, cargamos sus votos; si no, es un set vacío
        Set<Long> votedProducts = (userId != null)
                ? productVoteRepository.findProductIdsByUserId(userId)
                : Set.of();

        // Mapear a DTOs incluyendo los votos
        return userProducts.stream()
                .map(product -> {
                    Integer votes = votesMap.getOrDefault(product.getId(), 0);
                    boolean votedByUser = (userId != null) && votedProducts.contains(product.getId());
                    return new UserProductDetailsDTO(product, votes, votedByUser);
                })
                .toList();
    }

    public List<UserProduct> findUserProductPending() {
        return userProductRepository.getAllPendingVerification();
    }

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
