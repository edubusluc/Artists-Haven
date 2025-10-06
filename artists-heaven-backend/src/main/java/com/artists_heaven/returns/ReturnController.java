package com.artists_heaven.returns;

import org.springframework.web.bind.annotation.RestController;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.exception.AppExceptions;
import com.artists_heaven.order.Order;
import com.artists_heaven.order.OrderService;
import com.artists_heaven.standardResponse.StandardResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/returns")
public class ReturnController {

    private final ReturnService returnService;

    private final OrderService orderService;

    private final MessageSource messageSource;

    public ReturnController(ReturnService returnService, OrderService orderService, MessageSource messageSource) {
        this.returnService = returnService;
        this.orderService = orderService;
        this.messageSource = messageSource;
    }

    @PostMapping("/create")
    @Operation(summary = "Create a return request for an order", description = "Allows a user to create a return request for a given order if valid.")
    @ApiResponse(responseCode = "201", description = "Return request created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden action", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    @ApiResponse(responseCode = "404", description = "Order not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    @ApiResponse(responseCode = "500", description = "Unexpected error occurred", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    public ResponseEntity<StandardResponse<String>> createReturnForOrder(
            @Parameter(description = "Return request details including order ID, reason, and email", required = true) @RequestBody ReturnRequestDTO returnRequestDTO,
            @RequestParam String lang) {

        Order order = orderService.findOrderById(returnRequestDTO.getOrderId());
        returnService.createReturnForOrder(order, returnRequestDTO.getReason(), returnRequestDTO.getEmail(),lang);

        Locale locale = new Locale(lang);
        String message = messageSource.getMessage("return.message.successful", null, locale);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new StandardResponse<>(
                        message,
                        HttpStatus.CREATED.value()));
    }

    @GetMapping("/{orderId}/label")
    @Operation(summary = "Get return label PDF", description = "Generates and returns the return label PDF for a given order.")
    @ApiResponse(responseCode = "200", description = "Return label PDF generated successfully", content = @Content(mediaType = "application/pdf"))
    @ApiResponse(responseCode = "403", description = "Forbidden - user not authorized to access this return label", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    @ApiResponse(responseCode = "404", description = "Order not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    @ApiResponse(responseCode = "500", description = "Unexpected error occurred", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    public ResponseEntity<byte[]> getReturnLabel(
            @Parameter(description = "ID of the order to get the return label for", required = true) @PathVariable Long orderId,
            @Parameter(description = "Optional email verification if not authenticated") @RequestParam(required = false) String email) {

        Order order = orderService.findOrderById(orderId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof User user) {
            if (!order.getUser().getId().equals(user.getId())) {
                throw new AppExceptions.ForbiddenActionException("You are not allowed to access this return label");
            }
        } else {
            if (email == null || !order.getEmail().equalsIgnoreCase(email)) {
                throw new AppExceptions.ForbiddenActionException("You are not allowed to access this return label");
            }
        }

        boolean isAnonymous = authentication == null
                || authentication instanceof AnonymousAuthenticationToken;

        byte[] pdf = returnService.generateReturnLabelPdf(orderId, isAnonymous);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.inline()
                .filename("ORDER-RETURN" + order.getIdentifier() + ".pdf").build());

        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }

    @Operation(summary = "Get return data by ID", description = "Retrieves detailed return information for a given return ID.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Return found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    @ApiResponse(responseCode = "404", description = "Return not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    @GetMapping("/{returnId}/return")
    public ResponseEntity<StandardResponse<Return>> getMethodName(
            @Parameter(description = "ID of the order to get the return label for", required = true) @PathVariable Long returnId) {
        Return data = returnService.findById(returnId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new StandardResponse<>(
                        "Return found",
                        data,
                        HttpStatus.OK.value()));
    }

}
