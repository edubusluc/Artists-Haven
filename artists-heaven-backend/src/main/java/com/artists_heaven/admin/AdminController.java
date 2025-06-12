package com.artists_heaven.admin;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.artists_heaven.email.EmailSenderService;
import com.artists_heaven.email.EmailType;
import com.artists_heaven.entities.artist.Artist;
import com.artists_heaven.entities.artist.ArtistRepository;
import com.artists_heaven.entities.user.UserProfileDTO;
import com.artists_heaven.order.OrderService;
import com.artists_heaven.order.OrderStatus;
import com.artists_heaven.page.PageResponse;
import com.artists_heaven.verification.VerificationStatus;
import com.artists_heaven.verification.Verification;
import com.artists_heaven.verification.VerificationRepository;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.nio.file.Path;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ArtistRepository artistRepository;

    private final VerificationRepository verificationRepository;

    private final OrderService orderService;

    private final EmailSenderService emailSenderService;

    private final AdminService adminService;

    public AdminController(ArtistRepository artistRepository, VerificationRepository verificationRepository,
            OrderService orderService, EmailSenderService emailSenderService, AdminService adminService) {
        this.orderService = orderService;
        this.emailSenderService = emailSenderService;
        this.artistRepository = artistRepository;
        this.verificationRepository = verificationRepository;
        this.adminService = adminService;
    }

    @PostMapping("/validate_artist")
    public ResponseEntity<?> validateArtist(@RequestBody Map<String, Long> payload) {
        Long artistId = payload.get("id");

        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Artista no encontrado"));

        artist.setIsvalid(true);
        artistRepository.save(artist);

        Long verificationId = payload.get("verificationId");
        Verification verification = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Verificación no encontrada"));

        verification.setStatus(VerificationStatus.ACCEPTED);
        verificationRepository.save(verification);

        return ResponseEntity.ok(Map.of("message", "Artista verificado de forma correcta"));

    }

    ////////////////////////////////////////////
    // Get all verification request
    ////////////////////////////////////////////

    @GetMapping("/verification/pending")
    public ResponseEntity<?> getAllValidation() {
        List<Verification> verificationList = verificationRepository.findAll();

        return ResponseEntity.ok(verificationList);
    }

    @GetMapping("/verification_media/{fileName:.+}")
    public ResponseEntity<Resource> getVerificationVideo(@PathVariable String fileName) {
        // Obtén el directorio base del proyecto de forma dinámica
        String basePath = System.getProperty("user.dir")
                + "/artists-heaven-backend/src/main/resources/verification_media/";
        Path filePath = Paths.get(basePath, fileName);
        Resource resource = new FileSystemResource(filePath.toFile());

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "video/mp4")
                .body(resource);
    }

    @GetMapping("/staticsPerYear")
    public ResponseEntity<OrderStatisticsDTO> getNumOrdersPerYear(@RequestParam int year) {
        try {
            Integer numOrders = orderService.getNumOrdersPerYear(year);
            Double incomePerYear = orderService.getIncomePerYear(year);
            Map<EmailType, Integer> emailCounts = emailSenderService.getEmailCounts(year);
            Integer numUsers = adminService.countUsers();
            Integer numArtists = adminService.countArtists();
            Map<OrderStatus, Integer> orderStatusCounts = adminService.getOrderStatusCounts(year);
            Map<VerificationStatus, Integer> vericationStatusCount = adminService.getVerificationStatusCount(year);
            Map<String, Integer> orderItemCount = adminService.getMostSoldItems(year);
            Map<String, Integer> categoryItemCount = adminService.getMostCategory(year);
            Map<String, Integer> mostCountrySold = adminService.getCountrySold(year);

            // Create the DTO and set the values
            OrderStatisticsDTO orderStatistics = new OrderStatisticsDTO();
            orderStatistics.setNumOrders(numOrders);
            orderStatistics.setIncomePerYear(incomePerYear);
            orderStatistics.setEmailCounts(emailCounts);
            orderStatistics.setNumUsers(numUsers);
            orderStatistics.setNumArtists(numArtists);
            orderStatistics.setOrderStatusCounts(orderStatusCounts);
            orderStatistics.setVerificationStatusCounts(vericationStatusCount);
            orderStatistics.setOrderItemCount(orderItemCount);
            orderStatistics.setCategoryItemCount(categoryItemCount);
            orderStatistics.setMostCountrySold(mostCountrySold);
            return ResponseEntity.ok(orderStatistics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/sales/monthly")
    public ResponseEntity<List<MonthlySalesDTO>> getMonthlySalesData(@RequestParam int year) {
        try {
            List<MonthlySalesDTO> monthlySalesData = adminService.getMonthlySalesData(year);
            return ResponseEntity.ok(monthlySalesData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/product-management")
    public ResponseEntity<ProductManagementDTO> getProductManagement() {
        try {
            Integer notAvailableProducts = adminService.getNotAvailableProducts();
            Integer availableProducts = adminService.getAvailableProducts();
            Integer promotedProducts = adminService.getPromotedProducts();
            Integer totalProducts = adminService.getTotalProducts();

            ProductManagementDTO productManagement = new ProductManagementDTO();
            productManagement.setNotAvailableProducts(notAvailableProducts);
            productManagement.setAvailableProducts(availableProducts);
            productManagement.setPromotedProducts(promotedProducts);
            productManagement.setTotalProducts(totalProducts);
            return ResponseEntity.ok(productManagement);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/users")
    public PageResponse<UserProfileDTO> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(required = false) String search) {

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<UserProfileDTO> userPage = adminService.getAllUsers(search, pageRequest);

        return new PageResponse<>(userPage);
    }

}
