package com.artists_heaven.rating;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RatingResponseDTO {
    private Long id;
    private Integer score;
    private String comment;
    private String username;
    private LocalDate createdAt;

    public RatingResponseDTO(Rating rating) {
        this.id = rating.getId();
        this.score = rating.getScore();
        this.comment = rating.getComment();
        this.createdAt = rating.getCreatedAt();
        this.username = rating.getUser() != null ? rating.getUser().getUsername() : null;
    }
    
}
