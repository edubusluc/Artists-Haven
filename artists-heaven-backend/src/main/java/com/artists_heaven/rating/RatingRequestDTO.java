package com.artists_heaven.rating;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RatingRequestDTO {

    private Long productId;
    private Integer score;
    private String comment;

}
