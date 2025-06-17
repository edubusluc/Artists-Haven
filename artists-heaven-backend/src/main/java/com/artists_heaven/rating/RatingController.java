package com.artists_heaven.rating;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.artists_heaven.entities.user.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/rating")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    // Endpoint to retrieve all ratings for a product
    @GetMapping("/productReview/{id}")
    @Operation(summary = "Retrieve all ratings for a product", description = "Fetches and returns a list of all ratings associated with the specified product ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of product ratings", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Rating.class))))
    })
    public List<Rating> getProductRatings(
            @Parameter(description = "ID of the product to retrieve ratings for", required = true) @PathVariable Long id) {
        // Fetching and returning the list of all ratings for the product from the
        // rating service
        return ratingService.getProductRatings(id);
    }

    @PostMapping("/new")
    @Operation(summary = "Create a new rating for a product", description = "Allows an authenticated user to create a new rating for a specified product, including score and optional comment.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rating created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Rating.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - user must be authenticated", content = @Content)
    })
    public ResponseEntity<Rating> createNewRating(
            @Parameter(description = "Rating data including product ID, score, and comment", required = true) @RequestBody RatingRequestDTO ratingRequestDTO) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User principalUser = (User) authentication.getPrincipal();
        try {
            Rating rating = ratingService.createRating(
                    principalUser.getId(),
                    ratingRequestDTO.getProductId(),
                    ratingRequestDTO.getScore(),
                    ratingRequestDTO.getComment());
            return ResponseEntity.ok(rating);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

}
