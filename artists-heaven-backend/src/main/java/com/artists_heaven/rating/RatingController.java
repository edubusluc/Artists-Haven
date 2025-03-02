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
    public List<Rating> getProductRatings(@PathVariable Long id) {
        // Fetching and returning the list of all ratings for the product from the
        // rating service
        return ratingService.getProductRatings(id);
    }

    @PostMapping("/new")
    public ResponseEntity<Rating> createNewRating(@RequestBody RatingRequestDTO ratingRequestDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User principalUser = (User)authentication.getPrincipal();
       try{
        Rating rating = ratingService.createRating(principalUser.getId(), ratingRequestDTO.getProductId(), ratingRequestDTO.getScore(), ratingRequestDTO.getComment());
        return ResponseEntity.ok(rating);
       } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    
    }
}
