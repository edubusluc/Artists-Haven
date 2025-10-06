package com.artists_heaven.admin;

import org.springframework.web.bind.annotation.RestController;

import com.artists_heaven.email.EmailSenderService;
import com.artists_heaven.entities.artist.ArtistService;
import com.artists_heaven.entities.user.UserProfileDTO;
import com.artists_heaven.exception.AppExceptions.BadRequestException;
import com.artists_heaven.order.Order;
import com.artists_heaven.order.OrderDetailsDTO;
import com.artists_heaven.order.OrderService;
import com.artists_heaven.page.PageResponse;
import com.artists_heaven.product.ProductService;
import com.artists_heaven.standardResponse.StandardResponse;
import com.artists_heaven.userProduct.UserProduct;
import com.artists_heaven.userProduct.UserProductDetailsDTO;
import com.artists_heaven.userProduct.UserProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import com.artists_heaven.verification.Verification;
import com.artists_heaven.verification.VerificationRepository;
import com.artists_heaven.verification.VerificationService;

import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

        private final VerificationRepository verificationRepository;

        private final OrderService orderService;

        private final EmailSenderService emailSenderService;

        private final AdminService adminService;

        private final VerificationService verificationService;

        private final ProductService productService;

        private final ArtistService artistService;

        private final UserProductService userProductService;

        private final ResourceLoader resourceLoader;

        public AdminController(VerificationRepository verificationRepository,
                        OrderService orderService, EmailSenderService emailSenderService, AdminService adminService,
                        VerificationService verificationService,
                        ProductService productService, ArtistService artistService,
                        UserProductService userProductService,
                        ResourceLoader resourceLoader) {
                this.orderService = orderService;
                this.emailSenderService = emailSenderService;
                this.artistService = artistService;
                this.verificationRepository = verificationRepository;
                this.adminService = adminService;
                this.verificationService = verificationService;
                this.productService = productService;
                this.userProductService = userProductService;
                this.resourceLoader = resourceLoader;
        }

        private <T> ResponseEntity<StandardResponse<Object>> handleRequest(
                        T dto,
                        Consumer<T> serviceAction,
                        String successMessage,
                        HttpStatus status) {

                serviceAction.accept(dto);
                return ResponseEntity.ok(new StandardResponse<>(successMessage, HttpStatus.OK.value()));
        }

        @Operation(summary = "Validate artist account", description = "Marks an artist as verified, creates a new category using the artist's name, and updates the verification request status to 'ACCEPTED'.")
        @ApiResponse(responseCode = "200", description = "Artist successfully validated", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @ApiResponse(responseCode = "400", description = "Missing or invalid payload data", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"message\": \"Missing artist ID or verification ID\", \"status\": 400}")))
        @ApiResponse(responseCode = "404", description = "Artist or verification request not found", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"message\": \"Artist not found\", \"status\": 404}")))
        @PostMapping("/validate_artist")
        public ResponseEntity<StandardResponse<String>> validateArtist(
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Payload containing the artist ID and verification request ID", required = true, content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"id\": 123, \"verificationId\": 456}"))) @RequestBody Map<String, Long> payload) {

                final String ID_KEY = "id";
                final String VERIFICATION_ID_KEY = "verificationId";

                Long artistId = payload.get(ID_KEY);
                Long verificationId = payload.get(VERIFICATION_ID_KEY);

                if (artistId == null || verificationId == null) {
                        throw new BadRequestException("Missing artist ID or verification ID");
                }

                artistService.validateArtist(artistId, verificationId);

                return ResponseEntity.ok(new StandardResponse<>("Artist verified successfully", HttpStatus.OK.value()));
        }

        @Operation(summary = "Reject verification request", description = "This endpoint marks a verification request as REJECTED by its ID.")
        @ApiResponse(responseCode = "200", description = "Verification rejected successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Verification rejected successfully\", \"data\": null, \"status\": 200 }")))
        @ApiResponse(responseCode = "404", description = "Verification not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Verification not found\", \"data\": null, \"status\": 404 }")))
        @PostMapping("/{verificationId}/refuse")
        public ResponseEntity<StandardResponse<Void>> refuseVerification(
                        @Parameter(description = "ID of the verification request to reject", required = true) @PathVariable Long verificationId) {

                verificationService.refuseVerification(verificationId);
                return ResponseEntity.ok(
                                new StandardResponse<>("Verification rejected successfully", HttpStatus.OK.value()));
        }

        @Operation(summary = "Get all pending verifications", description = "Returns a list of all verification requests, regardless of their status. You may want to filter by 'PENDING' on the client side.")
        @ApiResponse(responseCode = "200", description = "List of verification requests retrieved successfully", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Verification.class))))
        @GetMapping("/verification/pending")
        public ResponseEntity<StandardResponse<List<Verification>>> getAllPendingVerifications() {
                List<Verification> verificationList = verificationRepository
                                .findAll(Sort.by(Sort.Direction.DESC, "date"));
                return ResponseEntity.ok(
                                new StandardResponse<>("Pending verifications retrieved", verificationList,
                                                HttpStatus.OK.value()));
        }

        @Operation(summary = "Get verification video by filename", description = "Retrieves a video file used for artist verification from the local server directory. "
                        + "The file must exist under the verification_media folder. "
                        + "The filename must not contain invalid characters or paths (e.g., '..').")
        @ApiResponse(responseCode = "200", description = "Video file retrieved successfully", content = @Content(mediaType = "video/mp4", schema = @Schema(type = "string", format = "binary")))
        @ApiResponse(responseCode = "400", description = "Invalid file name")
        @ApiResponse(responseCode = "404", description = "Video file not found")
        @GetMapping("/verification_media/{fileName:.+}")
        public ResponseEntity<Resource> getVerificationVideo(
                        @PathVariable String fileName) {

                try {
                        Resource resource = resourceLoader.getResource("classpath:verification_media/" + fileName);
                        if (resource.exists()) {
                                return ResponseEntity.ok()
                                                .contentType(MediaType.valueOf("video/mp4"))
                                                .body(resource);
                        } else {
                                return ResponseEntity.notFound().build();
                        }
                } catch (Exception e) {
                        return ResponseEntity.internalServerError().build();
                }
        }

        @Operation(summary = "Get yearly platform statistics", description = "Returns statistical data for a given year, including number of orders, total income, email counts, "
                        + "user and artist counts, order statuses, verification statuses, top-selling items, top categories, "
                        + "and countries with the most sales.")
        @ApiResponse(responseCode = "200", description = "Yearly statistics retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @ApiResponse(responseCode = "400", description = "Invalid year parameter or processing error", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"message\": \"Invalid year parameter\", \"status\": 400}")))
        @GetMapping("/staticsPerYear")
        public ResponseEntity<StandardResponse<OrderStatisticsDTO>> getYearlyStatistics(
                        @Parameter(description = "Year for which to retrieve statistics", example = "2024", required = true) @RequestParam int year) {

                OrderStatisticsDTO orderStatistics = new OrderStatisticsDTO();
                orderStatistics.setNumOrders(orderService.getNumOrdersPerYear(year));
                orderStatistics.setIncomePerYear(orderService.getIncomePerYear(year));
                orderStatistics.setEmailCounts(emailSenderService.getEmailCounts(year));
                orderStatistics.setNumUsers(adminService.countUsers());
                orderStatistics.setNumArtists(adminService.countArtists());
                orderStatistics.setOrderStatusCounts(adminService.getOrderStatusCounts(year));
                orderStatistics.setVerificationStatusCounts(adminService.getVerificationStatusCount(year));
                orderStatistics.setOrderItemCount(adminService.getMostSoldItems(year));
                orderStatistics.setCategoryItemCount(adminService.getMostCategory(year));
                orderStatistics.setMostCountrySold(adminService.getCountrySold(year));

                return ResponseEntity.ok(
                                new StandardResponse<OrderStatisticsDTO>(
                                                "Yearly statistics retrieved successfully",
                                                orderStatistics,
                                                HttpStatus.OK.value()));
        }

        @Operation(summary = "Get monthly sales data", description = "Returns a list of sales figures per month for the specified year. Each item in the list contains the month, total orders and total revenue.")
        @ApiResponse(responseCode = "200", description = "Monthly sales data retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @ApiResponse(responseCode = "400", description = "Invalid year parameter", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"message\": \"Invalid year parameter\", \"status\": 400}")))
        @GetMapping("/sales/monthly")
        public ResponseEntity<StandardResponse<List<MonthlySalesDTO>>> getMonthlySalesData(
                        @Parameter(description = "Year for which to retrieve monthly sales data", example = "2024", required = true) @RequestParam int year) {

                if (year < 2000 || year > Year.now().getValue()) {
                        throw new BadRequestException("Invalid year parameter");
                }

                List<MonthlySalesDTO> monthlySalesData = adminService.getMonthlySalesData(year);

                return ResponseEntity.ok(
                                new StandardResponse<>(
                                                "Monthly sales data retrieved successfully",
                                                monthlySalesData,
                                                HttpStatus.OK.value()));
        }

        @Operation(summary = "Get product management statistics", description = "Retrieves counts of products by status, including not available, available, promoted, and total products.")
        @ApiResponse(responseCode = "200", description = "Product management statistics retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @ApiResponse(responseCode = "400", description = "Failed to retrieve product statistics", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"message\": \"Failed to retrieve product statistics\", \"status\": 400}")))
        @GetMapping("/product-management")
        public ResponseEntity<StandardResponse<ProductManagementDTO>> getProductManagement() {
                ProductManagementDTO productManagement = new ProductManagementDTO();
                productManagement.setNotAvailableProducts(adminService.getNotAvailableProducts());
                productManagement.setAvailableProducts(adminService.getAvailableProducts());
                productManagement.setPromotedProducts(adminService.getPromotedProducts());
                productManagement.setTotalProducts(adminService.getTotalProducts());

                return ResponseEntity.ok(
                                new StandardResponse<>(
                                                "Product management statistics retrieved successfully",
                                                productManagement,
                                                HttpStatus.OK.value()));
        }

        @Operation(summary = "Get paginated list of users", description = "Retrieves a paginated list of user profiles. Supports optional search by user attributes.")
        @ApiResponse(responseCode = "200", description = "Paginated list of users retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PageResponse.class)))
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
        @ApiResponse(responseCode = "200", description = "Paginated list of orders retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PageResponse.class)))
        @GetMapping("/orders")
        public PageResponse<OrderDetailsDTO> getOrders(
                        @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Page size (number of orders per page)", example = "6") @RequestParam(defaultValue = "6") int size,
                        @Parameter(description = "Filter by status", example = "PAID") @RequestParam(required = false) String status,
                        @Parameter(description = "Search by identifier or paymentIntent") @RequestParam(required = false) String search) {
                PageRequest pageRequest = PageRequest.of(page, size);
                Page<Order> orderPage = adminService.getOrdersFiltered(status, search, pageRequest);

                Page<OrderDetailsDTO> dtoPage = orderPage.map(OrderDetailsDTO::new);
                return new PageResponse<>(dtoPage);
        }

        @Operation(summary = "Update the status of an order", description = "Updates the status of a specific order based on the provided order ID and new status.")
        @ApiResponse(responseCode = "200", description = "Order status updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @ApiResponse(responseCode = "404", description = "Order not found", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"message\": \"Order not found\", \"status\": 404}")))
        @ApiResponse(responseCode = "500", description = "Internal server error occurred while updating the order status", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"message\": \"Unexpected error occurred\", \"status\": 500}")))
        @PostMapping("/updateStatus")
        public ResponseEntity<StandardResponse<Void>> updateOrderStatus(
                        @Parameter(description = "Request payload containing order ID and new status", required = true) @RequestBody OrderStatusUpdateDTO request) {

                if (request.getOrderId() == null || request.getStatus() == null) {
                        throw new BadRequestException("Order ID and status must be provided");
                }

                adminService.updateOrderStatus(request.getOrderId(), request.getStatus());

                return ResponseEntity.ok(
                                new StandardResponse<>("Order status updated successfully", null,
                                                HttpStatus.OK.value()));
        }

        @Operation(summary = "Disable a product", description = "Disables a specific product by its ID. Throws an error if the product is already disabled.")
        @ApiResponse(responseCode = "200", description = "Product disabled successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"message\": \"Product not found\", \"status\": 404}")))
        @ApiResponse(responseCode = "400", description = "Product is already disabled", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"message\": \"Product is already disabled\", \"status\": 400}")))
        @PostMapping("/{productId}/disable")
        public ResponseEntity<StandardResponse<Void>> disableProduct(@PathVariable Long productId) {
                productService.disableProduct(productId);
                return ResponseEntity.ok(
                                new StandardResponse<>("Product disabled successfully", null, HttpStatus.OK.value()));
        }

        @Operation(summary = "Enable a product", description = "Enables a specific product by its ID. Throws an error if the product is already enabled.")
        @ApiResponse(responseCode = "200", description = "Product enabled successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"message\": \"Product not found\", \"status\": 404}")))
        @ApiResponse(responseCode = "400", description = "Product is already enabled", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"message\": \"Product is already enabled\", \"status\": 400}")))
        @PostMapping("/{productId}/enable")
        public ResponseEntity<StandardResponse<Void>> enableProduct(@PathVariable Long productId) {
                productService.enableProduct(productId);
                return ResponseEntity.ok(
                                new StandardResponse<>("Product enabled successfully", null, HttpStatus.OK.value()));
        }

        @Operation(summary = "Create a new category", description = "Creates a new product category with the specified name")
        @ApiResponse(responseCode = "201", description = "Category created successfully")
        @ApiResponse(responseCode = "400", description = "Invalid request")
        @PostMapping("newCategory")
        public ResponseEntity<StandardResponse<Object>> createCategory(@RequestBody CategoryDTO categoryDTO) {
                return handleRequest(
                                categoryDTO,
                                dto -> productService.saveCategory(dto.getName().replaceAll("\\s+", "")),
                                "Category created successfully",
                                HttpStatus.CREATED);
        }

        @Operation(summary = "Edit an existing category", description = "Edits the name of an existing category")
        @ApiResponse(responseCode = "200", description = "Category edited successfully")
        @ApiResponse(responseCode = "400", description = "Invalid request")
        @PostMapping("editCategory")
        public ResponseEntity<StandardResponse<Object>> editCategory(@RequestBody CategoryDTO categoryDTO) {
                return handleRequest(
                                categoryDTO,
                                productService::editCategory,
                                "Category edited successfully",
                                HttpStatus.OK);
        }

        @Operation(summary = "Create a new collection", description = "Creates a new product collection with the specified name")
        @ApiResponse(responseCode = "201", description = "Collection created successfully")
        @ApiResponse(responseCode = "400", description = "Invalid request")
        @PostMapping("newCollection")
        public ResponseEntity<StandardResponse<Object>> createCollection(@RequestBody CollectionDTO collectionDTO) {
                return handleRequest(
                                collectionDTO,
                                dto -> productService.saveCollection(dto.getName().replaceAll("\\s+", "-")),
                                "Collection created successfully",
                                HttpStatus.CREATED);
        }

        @Operation(summary = "Edit an existing collection", description = "Edits the name and promotion status of an existing collection")
        @ApiResponse(responseCode = "200", description = "Collection edited successfully")
        @ApiResponse(responseCode = "400", description = "Invalid request")
        @PostMapping("editCollection")
        public ResponseEntity<StandardResponse<Object>> editCollection(@RequestBody CollectionDTO collectionDTO) {
                return handleRequest(
                                collectionDTO,
                                productService::editCollection,
                                "Collection edited successfully",
                                HttpStatus.OK);
        }

        @Operation(summary = "Get all pending user products", description = "Retrieves a list of user-submitted products that are currently pending approval.")
        @ApiResponse(responseCode = "200", description = "List of pending user products retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @GetMapping("userProduct/pending")
        public ResponseEntity<StandardResponse<List<UserProductDetailsDTO>>> getAllUserProductPening() {
                List<UserProduct> userProducts = userProductService.findUserProductPending();
                List<UserProductDetailsDTO> userProductsDTO = userProducts.stream()
                                .map(UserProductDetailsDTO::new)
                                .toList();
                return ResponseEntity.ok(
                                new StandardResponse<>("userProducts retrieved successfully", userProductsDTO,
                                                HttpStatus.OK.value()));
        }

        @Operation(summary = "Approve a user product", description = "Approves a user-submitted product identified by its ID.")
        @ApiResponse(responseCode = "200", description = "Product approved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"message\": \"Product not found\", \"status\": 404}")))
        @PostMapping("userProduct/{id}/approve")
        public ResponseEntity<StandardResponse<UserProductDetailsDTO>> approveProduct(@PathVariable Long id) {

                UserProduct approved = userProductService.approveProduct(id);
                UserProductDetailsDTO response = new UserProductDetailsDTO(approved);
                return ResponseEntity.ok(
                                new StandardResponse<>("OK APPROVE", response,
                                                HttpStatus.OK.value()));

        }

        @Operation(summary = "Reject a user product", description = "Rejects a user-submitted product identified by its ID.")
        @ApiResponse(responseCode = "200", description = "Product rejected successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"message\": \"Product not found\", \"status\": 404}")))
        @PostMapping("userProduct/{id}/reject")
        public ResponseEntity<StandardResponse<UserProductDetailsDTO>> rejectProduct(@PathVariable Long id) {

                UserProduct rejected = userProductService.rejectProduct(id);
                UserProductDetailsDTO response = new UserProductDetailsDTO(rejected);
                return ResponseEntity.ok(
                                new StandardResponse<>("OK REJECT", response,
                                                HttpStatus.OK.value()));

        }

}
