package com.artists_heaven.entities.artist;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.artists_heaven.admin.MonthlySalesDTO;
import com.artists_heaven.entities.user.UserRepository;
import com.artists_heaven.entities.user.UserRole;
import com.artists_heaven.event.Event;
import com.artists_heaven.event.EventService;
import com.artists_heaven.exception.AppExceptions;
import com.artists_heaven.exception.AppExceptions.DuplicateActionException;
import com.artists_heaven.images.ImageServingUtil;
import com.artists_heaven.order.OrderItem;
import com.artists_heaven.order.OrderStatus;
import com.artists_heaven.product.Category;
import com.artists_heaven.product.CategoryRepository;
import com.artists_heaven.product.Product;
import com.artists_heaven.product.ProductService;
import com.artists_heaven.verification.Verification;
import com.artists_heaven.verification.VerificationRepository;
import com.artists_heaven.verification.VerificationStatus;

@Service
public class ArtistService {

    private final ArtistRepository artistRepository;

    private final UserRepository userRepository;

    private final CategoryRepository categoryRepository;

    private final VerificationRepository verificationRepository;

    private final ProductService productService;

    private final EventService eventService;

    private final MessageSource messageSource;

    private final ImageServingUtil imageServingUtil;

    private static final String UPLOAD_DIR = "artists-heaven-backend/src/main/resources/mainArtist_media/";

    public ArtistService(ArtistRepository artistRepository, UserRepository userRepository,
            CategoryRepository categoryRepository, VerificationRepository verificationRepository,
            ProductService productService, EventService eventService, MessageSource messageSource,
            ImageServingUtil imageServingUtil) {
        this.artistRepository = artistRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.verificationRepository = verificationRepository;
        this.productService = productService;
        this.eventService = eventService;
        this.messageSource = messageSource;
        this.imageServingUtil = imageServingUtil;
    }

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Registers a new artist in the system.
     *
     * @param artist the artist object containing registration details
     * @return the registered artist after being saved in the database
     * @throws IllegalArgumentException if the email or artist name already exists
     */
    public Artist registerArtist(ArtistRegisterDTO request, String lang) {

        Locale locale = new Locale(lang);

        // Validate email address
        userRepository.findByEmail(request.getEmail()).ifPresent(u -> {
            String msg = messageSource.getMessage("email.duplicate", null, locale);
            throw new DuplicateActionException(msg);
        });

        // Validate artist name
        if (artistRepository.existsByArtistName(request.getArtistName())) {
            String msg = messageSource.getMessage("artistName.alreadyExists", null, locale);
            throw new DuplicateActionException(msg);
        }

        Artist artist = new Artist();

        String imageUrl = imageServingUtil.saveImages(request.getImage(), UPLOAD_DIR, "/mainArtist_media/",
                false);

        artist.setMainViewPhoto(imageUrl);

        // Configure artist data
        String username = request.getArtistName();
        artist.setUsername(username);
        artist.setArtistName(username);
        artist.setRole(UserRole.ARTIST);
        artist.setPassword(passwordEncoder.encode(request.getPassword()));
        artist.setEmail(request.getEmail());
        artist.setLastName(request.getLastName());
        artist.setFirstName(request.getFirstName());
        artist.setUrl(request.getUrl());
        artist.setMainColor(request.getColor());

        return artistRepository.save(artist);
    }

    /**
     * Finds an artist by their ID.
     *
     * @param id the artist's ID
     * @return the {@link Artist} object
     * @throws AppExceptions.ResourceNotFoundException if the artist does not exist
     */
    public Artist findById(Long id) {
        return artistRepository.findById(id)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Artist not found with id: " + id));
    }

    /**
     * Returns the verification status of an artist.
     *
     * @param id the artist's ID
     * @return a string representing the verification status ("Verified", "Not
     *         Verified", etc.)
     */
    public String isArtistVerificated(Long id) {
        List<VerificationStatus> latestVerificationStatus = artistRepository.findLatestVerificationStatus(id);
        return latestVerificationStatus.isEmpty()
                ? (artistRepository.isArtistVerificated(id) ? "Verified" : "Not Verified")
                : latestVerificationStatus.get(0).toString();
    }

    /**
     * Retrieves the number of future events for an artist in a given year.
     *
     * @param id   the artist's ID
     * @param year the year to filter events
     * @return the number of future events
     */
    public Integer getFutureEvents(Long id, Integer year) {
        return artistRepository.findFutureEventsForArtist(id, year);
    }

    /**
     * Retrieves the number of past events for an artist in a given year.
     *
     * @param id   the artist's ID
     * @param year the year to filter events
     * @return the number of past events
     */
    public Integer getPastEvents(Long id, Integer year) {
        return artistRepository.findPastEventsForArtist(id, year);
    }

    /**
     * Retrieves the count of order items sold by an artist in a given year.
     *
     * @param id   the artist's ID
     * @param year the year to filter orders
     * @return a map of product name to quantity sold
     */
    public Map<String, Integer> getOrderItemCount(Long id, Integer year) {
        Artist artist = findById(id);
        List<OrderItem> ordersItemsByYear = artistRepository.getOrdersPerYear(artist.getArtistName().toUpperCase(),
                year);
        Map<String, Integer> itemsCount = new HashMap<>();

        for (OrderItem orderItem : ordersItemsByYear) {
            String itemName = orderItem.getName();
            itemsCount.merge(itemName, 1, Integer::sum);
        }
        return itemsCount;
    }

    /**
     * Retrieves the countries where an artist's products were sold in a given year.
     *
     * @param id   the artist's ID
     * @param year the year to filter orders
     * @return a map of country name to number of items sold
     */
    public Map<String, Integer> getMostCountrySold(Long id, Integer year) {
        Artist artist = findById(id);
        List<OrderItem> ordersItemsByYear = artistRepository.getOrdersPerYear(artist.getArtistName().toUpperCase(),
                year);
        Map<String, Integer> itemsCount = new HashMap<>();

        for (OrderItem orderItem : ordersItemsByYear) {
            itemsCount.merge(orderItem.getOrder().getCountry(), 1, Integer::sum);
        }
        return itemsCount;
    }

    /**
     * Retrieves monthly sales data for a specific artist.
     *
     * @param artistId the artist's ID
     * @param year     the year to filter orders
     * @return a list of {@link MonthlySalesDTO} with monthly sales information
     */
    public List<MonthlySalesDTO> getMonthlySalesDataPerArtist(Long artistId, int year) {
        Artist artist = findById(artistId);

        List<Object[]> results = artistRepository.findMonthlySalesData(
                artist.getArtistName().toUpperCase(),
                year,
                OrderStatus.RETURN_ACCEPTED);

        return results.stream()
                .map(result -> {
                    MonthlySalesDTO dto = new MonthlySalesDTO();
                    dto.setMonth((Integer) result[0]);
                    dto.setTotalOrders((Long) result[1]);
                    return dto;
                })
                .toList();
    }

    /**
     * Retrieves all valid artists in the system.
     *
     * @return a list of {@link Artist}
     */
    public List<Artist> getValidArtists() {
        return artistRepository.findValidAritst();
    }

    /**
     * Validates an artist, marks them as verified, creates a category,
     * and updates the corresponding verification record.
     *
     * @param artistId       the artist's ID
     * @param verificationId the verification record ID
     * @throws AppExceptions.ResourceNotFoundException if the artist or verification
     *                                                 does not exist
     */
    public void validateArtist(Long artistId, Long verificationId) {
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Artist not found"));

        artist.setIsVerificated(true);
        artistRepository.save(artist);

        Category category = new Category();
        category.setName(artist.getArtistName().toUpperCase().replaceAll("\\s+", ""));
        categoryRepository.save(category);

        Verification verification = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Verification not found"));

        verification.setStatus(VerificationStatus.ACCEPTED);
        verificationRepository.save(verification);
    }

    /**
     * Retrieves an artist's detailed information including products and events.
     *
     * @param artistId the artist's ID
     * @return an {@link ArtistDTO} containing artist details
     * @throws AppExceptions.ResourceNotFoundException if the artist does not exist
     */
    public ArtistDTO getArtistWithDetails(Long artistId) {
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(
                        () -> new AppExceptions.ResourceNotFoundException("Artist not found with id: " + artistId));

        String artistKey = normalizeArtistName(artist.getArtistName());
        List<Product> artistProducts = productService.findProductsByArtist(artistKey);

        List<Event> artistEvents = eventService.findEventThisYearByArtist(artistId);

        ArtistDTO artistDTO = new ArtistDTO();
        artistDTO.setArtistName(artist.getArtistName());
        artistDTO.setArtistEvents(artistEvents);
        artistDTO.setArtistProducts(artistProducts);
        artistDTO.setPrimaryColor(artist.getMainColor());

        return artistDTO;
    }

    /**
     * Normalizes an artist name by converting it to uppercase and removing
     * whitespace.
     *
     * @param artistName the original artist name
     * @return the normalized artist name
     */
    private String normalizeArtistName(final String artistName) {
        if (artistName == null)
            return "";
        return artistName.toUpperCase().replaceAll("\\s+", "");
    }

    /**
     * Retrieves dashboard data for an artist for a given year.
     *
     * @param artistId the artist's ID
     * @param year     the year to filter events and orders
     * @return an {@link ArtistDashboardDTO} containing dashboard metrics
     * @throws AppExceptions.ResourceNotFoundException if the artist does not exist
     */
    public ArtistDashboardDTO getArtistDashboard(Long artistId, int year) {
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Artist not found with id " + artistId));

        ArtistDashboardDTO dto = new ArtistDashboardDTO();
        dto.setIsVerificated(isArtistVerificated(artistId));
        dto.setFutureEvents(artistRepository.findFutureEventsForArtist(artistId, year));
        dto.setPastEvents(artistRepository.findPastEventsForArtist(artistId, year));
        dto.setOrderItemCount(getOrderItemCount(artist.getId(), year));
        dto.setMostCountrySold(getMostCountrySold(artist.getId(), year));

        return dto;
    }

}
