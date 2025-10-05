package com.artists_heaven.rating;

import java.time.LocalDate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "RatingResponseDTO", description = "Represents a rating submitted by a user, including score, comment, author username, and creation date.")
public class RatingResponseDTO {

    @Schema(description = "Unique identifier of the rating", example = "401")
    private Long id;

    @Schema(description = "Score given by the user, typically on a scale (e.g., 1-5)", example = "5")
    private Integer score;

    @Schema(description = "Comment provided by the user along with the score", example = "Amazing product, really loved it!")
    private String comment;

    @Schema(description = "Username of the user who submitted the rating", example = "janedoe123")
    private String username;

    @Schema(description = "Date when the rating was created", example = "2025-09-29")
    private LocalDate createdAt;

    public RatingResponseDTO(Rating rating) {
        this.id = rating.getId();
        this.score = rating.getScore();
        this.comment = rating.getComment();
        this.createdAt = rating.getCreatedAt();
        this.username = rating.getUser() != null ? rating.getUser().getUsername() : null;
    }

}
