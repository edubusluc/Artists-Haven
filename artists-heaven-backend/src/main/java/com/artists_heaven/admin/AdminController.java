package com.artists_heaven.admin;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.artists_heaven.email.EmailSenderService;
import com.artists_heaven.email.EmailType;
import com.artists_heaven.entities.artist.Artist;
import com.artists_heaven.entities.artist.ArtistRepository;
import com.artists_heaven.entities.user.UserProfileDTO;
import com.artists_heaven.order.Order;
import com.artists_heaven.order.OrderDetailsDTO;
import com.artists_heaven.order.OrderService;
import com.artists_heaven.order.OrderStatus;
import com.artists_heaven.page.PageResponse;
import com.artists_heaven.product.Category;
import com.artists_heaven.product.CategoryRepository;
import com.artists_heaven.product.ProductService;
import com.artists_heaven.verification.VerificationStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.persistence.EntityNotFoundException;

import com.artists_heaven.verification.Verification;
import com.artists_heaven.verification.VerificationRepository;
import com.artists_heaven.verification.VerificationService;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.nio.file.Path;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    private final CategoryRepository categoryRepository;

    private final VerificationService verificationService;

    private final ProductService productService;

    public AdminController(ArtistRepository artistRepository, VerificationRepository verificationRepository,
            OrderService orderService, EmailSenderService emailSenderService, AdminService adminService,
            CategoryRepository categoryRepository, VerificationService verificationService,
            ProductService productService) {
        this.orderService = orderService;
        this.emailSenderService = emailSenderService;
        this.artistRepository = artistRepository;
        this.verificationRepository = verificationRepository;
        this.adminService = adminService;
        this.categoryRepository = categoryRepository;
        this.verificationService = verificationService;
        this.productService = productService;
    }

    @Operation(summary = "Validate artist account", description = "This endpoint marks an artist as verified, creates a new category using the artist's name, and updates the verification request status to 'ACCEPTED'.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Artist successfully validated", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"message\": \"Artist verified successfully\"}"))),
            @ApiResponse(responseCode = "400", description = "Missing or invalid payload data", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"error\": \"Missing artist ID or verification ID\"}"))),
            @ApiResponse(responseCode = "404", description = "Artist or verification request not found", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"error\": \"Artist not found\"}")))
    })
    @PostMapping("/validate_artist")
    public ResponseEntity<Map<String, String>> validateArtist(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Payload containing the artist ID and verification request ID", required = true, content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"id\": 123, \"verificationId\": 456}"))) @RequestBody Map<String, Long> payload) {

        final String ID_KEY = "id";
        final String VERIFICATION_ID_KEY = "verificationId";

        if (payload == null || !payload.containsKey(ID_KEY) || !payload.containsKey(VERIFICATION_ID_KEY) ||
                payload.get(ID_KEY) == null || payload.get(VERIFICATION_ID_KEY) == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing artist ID or verification ID");
        }

        Long artistId = payload.get(ID_KEY);
        Long verificationId = payload.get(VERIFICATION_ID_KEY);

        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Artist not found"));

        artist.setIsVerificated(true);
        artistRepository.save(artist);

        // Create a new category with the artist's name in uppercase
        Category category = new Category();
        category.setName(artist.getArtistName().toUpperCase());
        categoryRepository.save(category);

        Verification verification = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Verification not found"));

        verification.setStatus(VerificationStatus.ACCEPTED);
        verificationRepository.save(verification);

        return ResponseEntity.ok(Map.of("message", "Artist verified successfully"));
    }

    @PostMapping("/{verificationId}/refuse")
    public ResponseEntity<Map<String, String>> refuseVerification(@PathVariable Long verificationId) {
        try {
            verificationService.refuseVerification(verificationId);
            return ResponseEntity.ok(Map.of("message", "Verification refused successfully"));
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Verification not found");
        }
    }

    @Operation(summary = "Get all pending verifications", description = "Returns a list of all verification requests, regardless of their status. You may want to filter by 'PENDING' on the client side.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of verification requests retrieved successfully", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Verification.class))))
    })
    @GetMapping("/verification/pending")
    public ResponseEntity<List<Verification>> getAllValidation() {
        List<Verification> verificationList = verificationRepository.findAll(Sort.by(Sort.Direction.DESC, "date"));
        return ResponseEntity.ok(verificationList);
    }

    @Operation(summary = "Get verification video by filename", description = "Retrieves a video file used for artist verification from the local server directory. The file must exist under the verification_media folder.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Video file retrieved successfully", content = @Content(mediaType = "video/mp4", schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "404", description = "Video file not found")
    })
    @GetMapping("/verification_media/{fileName:.+}")
    public ResponseEntity<Resource> getVerificationVideo(@PathVariable String fileName) {
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

    @Operation(summary = "Get yearly platform statistics", description = "Returns statistical data for a given year, including number of orders, total income, email counts, user and artist counts, order statuses, verification statuses, top-selling items, top categories, and countries with the most sales.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Yearly statistics retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderStatisticsDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid year parameter or processing error")
    })
    @GetMapping("/staticsPerYear")
    public ResponseEntity<OrderStatisticsDTO> getNumOrdersPerYear(
            @Parameter(description = "Year for which to retrieve statistics", example = "2024", required = true) @RequestParam int year) {
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

    @Operation(summary = "Get monthly sales data", description = "Returns a list of sales figures per month for the specified year. Each item in the list contains the month and total sales amount.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Monthly sales data retrieved successfully", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = MonthlySalesDTO.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid year parameter or processing error")
    })
    @GetMapping("/sales/monthly")
    public ResponseEntity<List<MonthlySalesDTO>> getMonthlySalesData(
            @Parameter(description = "Year for which to retrieve monthly sales data", example = "2024", required = true) @RequestParam int year) {
        try {
            List<MonthlySalesDTO> monthlySalesData = adminService.getMonthlySalesData(year);
            return ResponseEntity.ok(monthlySalesData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Get product management statistics", description = "Retrieves counts of products by status, including not available, available, promoted, and total products.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product management statistics retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductManagementDTO.class))),
            @ApiResponse(responseCode = "400", description = "Failed to retrieve product statistics")
    })
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

    @Operation(summary = "Get paginated list of users", description = "Retrieves a paginated list of user profiles. Supports optional search by user attributes.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paginated list of users retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PageResponse.class)))
    })
    @GetMapping("/users")
    public PageResponse<UserProfileDTO> getUsers(
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size (number of users per page)", example = "6") @RequestParam(defaultValue = "6") int size,

            @Parameter(description = "Optional search keyword to filter users", example = "john") @RequestParam(required = false) String search) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<UserProfileDTO> userPage = adminService.getAllUsers(search, pageRequest);

        return new PageResponse<>(userPage);
    }

    @Operation(summary = "Get paginated list of orders", description = "Retrieves a paginated list of orders sorted by date, returning detailed order information.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paginated list of orders retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PageResponse.class)))
    })
    @GetMapping("/orders")
    public PageResponse<OrderDetailsDTO> getOrders(
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size (number of orders per page)", example = "6") @RequestParam(defaultValue = "6") int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Order> orderPage = adminService.getAllOrderSortByDate(pageRequest);

        // Map each Order entity to OrderDetailsDTO
        Page<OrderDetailsDTO> dtoPage = orderPage.map(OrderDetailsDTO::new);

        return new PageResponse<>(dtoPage);
    }

    @Operation(summary = "Update the status of an order", description = "Updates the status of a specific order based on the provided order ID and new status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order status updated successfully", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred while updating the order status", content = @Content(mediaType = "text/plain"))
    })
    @PostMapping("/updateStatus")
    public ResponseEntity<String> updateOrderStatus(
            @Parameter(description = "Request payload containing order ID and new status", required = true) @RequestBody OrderStatusUpdateDTO request) {
        try {
            adminService.updateOrderStatus(request.getOrderId(), request.getStatus());
            return ResponseEntity.ok("Order status updated successfully.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while updating the order status.");
        }
    }

    @PostMapping("/{productId}/disable")
    public ResponseEntity<Map<String, String>> disableProduct(@PathVariable Long productId) {
        try {
            productService.disableProduct(productId);
            return ResponseEntity.ok(Map.of("message", "Product disabled successfully"));
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/{productId}/enable")
    public ResponseEntity<Map<String, String>> enableProduct(@PathVariable Long productId) {
        try {
            productService.enableProduct(productId);
            return ResponseEntity.ok(Map.of("message", "Product enabled successfully"));
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

}
