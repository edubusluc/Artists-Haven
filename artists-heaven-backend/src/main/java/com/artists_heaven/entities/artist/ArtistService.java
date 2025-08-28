package com.artists_heaven.entities.artist;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

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
            ProductService productService, EventService eventService, MessageSource messageSource, ImageServingUtil imageServingUtil) {
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
        String bannerUrl = imageServingUtil.saveImages(request.getBannerImage(), UPLOAD_DIR,
                "/mainArtist_media/",
                false);

        artist.setMainViewPhoto(imageUrl);
        artist.setBannerPhoto(bannerUrl);

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

    public Artist findById(Long id) {
        return artistRepository.findById(id)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Artist not found with id: " + id));
    }

    public String isArtistVerificated(Long id) {
        Boolean isVerified = artistRepository.isArtistVerificated(id);
        List<VerificationStatus> latestVerificationStatus = artistRepository.findLatestVerificationStatus(id);
        String result = "";
        if (isVerified) {
            result = "Verified";
        } else {
            result = "Not Verified";
        }

        if (latestVerificationStatus.size() != 0) {
            result = latestVerificationStatus.get(0).toString();
        }
        return result;
    }

    public Integer getFutureEvents(Long id, Integer year) {
        return artistRepository.findFutureEventsForArtist(id, year);
    }

    public Integer getPastEvents(Long id, Integer year) {
        return artistRepository.findPastEventsForArtist(id, year);
    }

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
                .collect(Collectors.toList());
    }

    public List<Artist> getValidArtists() {
        return artistRepository.findValidAritst();
    }

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

    public ArtistDTO getArtistWithDetails(Long artistId) {
        // Buscar artista
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(
                        () -> new AppExceptions.ResourceNotFoundException("Artist not found with id: " + artistId));

        // Obtener productos
        String artistKey = normalizeArtistName(artist.getArtistName());
        List<Product> artistProducts = productService.findProductsByArtist(artistKey);

        // Obtener eventos de este año
        List<Event> artistEvents = eventService.findEventThisYearByArtist(artistId);

        // Mapear a DTO
        ArtistDTO artistDTO = new ArtistDTO();
        artistDTO.setArtistName(artist.getArtistName());
        artistDTO.setArtistEvents(artistEvents);
        artistDTO.setArtistProducts(artistProducts);
        artistDTO.setPrimaryColor(artist.getMainColor());
        artistDTO.setBannerPhoto(artist.getBannerPhoto());

        return artistDTO;
    }

    private String normalizeArtistName(final String artistName) {
        if (artistName == null)
            return "";
        return artistName.toUpperCase().replaceAll("\\s+", "");
    }

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
