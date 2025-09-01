package com.artists_heaven.order;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.product.Product;
import com.artists_heaven.product.ProductService;
import com.artists_heaven.standardResponse.StandardResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

        private final OrderService orderService;

        private final ProductService productService;

        public OrderController(OrderService orderService, ProductService productService) {
                this.orderService = orderService;
                this.productService = productService;
        }

        @GetMapping("/myOrders")
        @Operation(summary = "Get current user's orders with pagination", description = "Returns a paginated list of orders placed by the authenticated user.", security = @SecurityRequirement(name = "bearerAuth"))
        @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
        @ApiResponse(responseCode = "400", description = "Invalid request parameters")
        @ApiResponse(responseCode = "401", description = "Unauthorized")
        public ResponseEntity<StandardResponse<Map<String, Object>>> getMyOrders(
                        @AuthenticationPrincipal User user,
                        @RequestParam(defaultValue = "0") @Min(0) int page,
                        @RequestParam(defaultValue = "3") @Min(1) @Max(50) int size) {

                Sort sort = Sort.by(Sort.Order.desc("lastUpdateDateTime").nullsLast());
                Pageable pageable = PageRequest.of(page, size, sort);

                Page<Order> orderPage = orderService.getMyOrdersPageable(user.getId(), pageable);
                List<Order> orders = orderPage.getContent();

                Map<Long, String> productImages = getProductsImages(orders);
                List<OrderDetailsUserDTO> orderDetailsList = orders.stream()
                                .map(OrderDetailsUserDTO::new)
                                .toList();

                // Construir data para la respuesta
                Map<String, Object> data = new HashMap<>();
                data.put("orders", orderDetailsList);
                data.put("productImages", productImages);
                data.put("currentPage", orderPage.getNumber());
                data.put("totalPages", orderPage.getTotalPages());
                data.put("totalItems", orderPage.getTotalElements());

                StandardResponse<Map<String, Object>> response = new StandardResponse<>("Orders retrieved successfully",
                                data,
                                HttpStatus.OK.value());

                return ResponseEntity.ok(response);
        }

        @GetMapping("/{id}")
        @Operation(summary = "Get order details by ID", description = "Retrieves detailed information of an order by its ID. "
                        +
                        "Access is restricted to the order owner or administrators. " +
                        "Orders without an assigned user can only be accessed by administrators.", security = @SecurityRequirement(name = "bearerAuth"))
        @ApiResponse(responseCode = "200", description = "Successfully retrieved order details", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @ApiResponse(responseCode = "403", description = "Forbidden: Access denied to this order", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @ApiResponse(responseCode = "404", description = "Order not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        public ResponseEntity<StandardResponse<OrderDetailsDTO>> getOrder(
                        @Parameter(description = "ID of the order to retrieve", required = true) @PathVariable Long id) {

                OrderDetailsDTO orderDetailsDTO = orderService.getOrderDetailsById(id);
                return ResponseEntity.ok(
                                new StandardResponse<>("Order retrieved successfully", orderDetailsDTO,
                                                HttpStatus.OK.value()));
        }

        @GetMapping("/by-identifier")
        @Operation(summary = "Get order details by identifier", description = "Retrieves order information and related product images using a unique identifier.", security = @SecurityRequirement(name = "bearerAuth"))
        @ApiResponse(responseCode = "200", description = "Successfully retrieved order by identifier", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @ApiResponse(responseCode = "404", description = "Order not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        public ResponseEntity<StandardResponse<Map<String, Object>>> getOrderByIdentifier(
                        @Parameter(description = "Unique identifier of the order", required = true) @RequestParam Long identifier,
                        @RequestParam String lang) {

                Order order = orderService.getOrderByIdentifier(identifier, lang);

                Map<Long, String> productImages = getProductsImages(List.of(order));
                OrderDetailsUserDTO orderDetailsUserDTO = new OrderDetailsUserDTO(order);

                Map<String, Object> response = new HashMap<>();
                response.put("orders", orderDetailsUserDTO);
                response.put("productImages", productImages);

                return ResponseEntity.ok(
                                new StandardResponse<>("Order retrieved successfully", response,
                                                HttpStatus.OK.value()));
        }

        private Map<Long, String> getProductsImages(List<Order> orders) {
                // Obtener IDs de productos relacionados
                Set<Long> productIds = orders.stream()
                                .flatMap(order -> order.getItems().stream())
                                .map(OrderItem::getProductId)
                                .collect(Collectors.toSet());

                // Obtener productos
                List<Product> products = productService.findAllByIds(productIds);

                // Asociar imagen principal
                Map<Long, String> productImages = products.stream()
                                .filter(p -> p.getImages() != null && !p.getImages().isEmpty())
                                .collect(Collectors.toMap(
                                                Product::getId,
                                                p -> p.getImages().get(0)));

                return productImages;
        }

}
