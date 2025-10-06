package com.artists_heaven.productVote;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.standardResponse.StandardResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/productVote")
public class ProductVoteController {

    private final ProductVoteService productVoteService;

    private final MessageSource messageSource;

    public ProductVoteController(ProductVoteService productVoteService, MessageSource messageSource) {
        this.productVoteService = productVoteService;
        this.messageSource = messageSource;
    }

    @Operation(summary = "Register a positive vote for a product", description = "Allows an authenticated user to vote positively for a specific product. The response message is localized based on the 'lang' parameter.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "201", description = "Vote registered successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request or vote already registered", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    @PostMapping("/{productId}")
    public ResponseEntity<StandardResponse<String>> votePositive(@PathVariable Long productId,
            @AuthenticationPrincipal User user, String lang) {
        productVoteService.votePositive(productId, user.getId(), lang);
        Locale locale = new Locale(lang);
        String voteRegistered = messageSource.getMessage("vote.registeredSuccessfully", null, locale);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new StandardResponse<>(voteRegistered, null,
                        HttpStatus.CREATED.value()));
    }

}
