package com.artists_heaven.rating;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.standardResponse.StandardResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/rating")
public class RatingController {

        private final RatingService ratingService;

        public RatingController(RatingService ratingService) {
                this.ratingService = ratingService;
        }

        @GetMapping("/productReview/{id}")
        @Operation(summary = "Retrieve all ratings for a product", description = "Fetches and returns a list of all ratings associated with the specified product ID.")
        @ApiResponse(responseCode = "200", description = "Successfully retrieved list of product ratings", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @ApiResponse(responseCode = "500", description = "Unexpected error occurred", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        public ResponseEntity<StandardResponse<List<RatingResponseDTO>>> getProductRatings(
                        @Parameter(description = "ID of the product to retrieve ratings for", required = true) @PathVariable Long id) {

                List<Rating> ratings = ratingService.getProductRatings(id);
                List<RatingResponseDTO> ratingDTO = ratings.stream()
                                .map(RatingResponseDTO::new)
                                .toList();

                return ResponseEntity.ok(
                                new StandardResponse<>("Successfully retrieved product ratings", ratingDTO,
                                                HttpStatus.OK.value()));
        }

        @PostMapping("/new")
        @Operation(summary = "Create a new rating for a product", description = "Allows an authenticated user to create a new rating for a specified product, including score and optional comment.")
        @ApiResponse(responseCode = "201", description = "Rating created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @ApiResponse(responseCode = "403", description = "Forbidden - user has not purchased this item", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @ApiResponse(responseCode = "409", description = "Conflict - user has already rated this product", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @ApiResponse(responseCode = "500", description = "Unexpected error occurred", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        public ResponseEntity<StandardResponse<RatingResponseDTO>> createNewRating(
                        @Parameter(description = "Rating data including product ID, score, and comment", required = true) @RequestBody RatingRequestDTO ratingRequestDTO,
                        @RequestParam String lang) {

                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                User principalUser = (User) authentication.getPrincipal();

                Rating rating = ratingService.createRating(
                                principalUser.getId(),
                                ratingRequestDTO.getProductId(),
                                ratingRequestDTO.getScore(),
                                ratingRequestDTO.getComment(),
                                lang);

                RatingResponseDTO responseDTO = new RatingResponseDTO(rating);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(new StandardResponse<>("Rating created successfully", responseDTO,
                                                HttpStatus.CREATED.value()));
        }

}
