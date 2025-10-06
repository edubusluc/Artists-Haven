package com.artists_heaven.userProduct;

import java.util.Date;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(name = "UserProductDetailsDTO", description = "Represents detailed information of a user's product, including images, votes, status, owner, and creation date.")
public class UserProductDetailsDTO {

    @Schema(description = "Unique identifier of the product", example = "101")
    private Long id;

    @Schema(description = "Name of the product", example = "Handmade Wooden Chair")
    private String name;

    @Schema(description = "List of URLs pointing to the product images", example = "[\"https://example.com/image1.jpg\", \"https://example.com/image2.jpg\"]")
    private List<String> images;

    @Schema(description = "Number of votes this product has received", example = "42")
    private Integer numVotes;

    @Schema(description = "Indicates whether the current user has voted for this product", example = "true")
    private boolean votedByUser;

    @Schema(description = "Username of the product's owner", example = "artist123")
    private String username;

    @Schema(description = "Current status of the product", example = "ACTIVE")
    private Status status;

    @Schema(description = "Date when the product was created", example = "2025-09-29T12:34:56.789Z")
    private Date createdAt;

    public UserProductDetailsDTO(UserProduct product, Integer numVotes, boolean votedByUser) {
        this.id = product.getId();
        this.name = product.getName();
        this.images = product.getImages();
        this.numVotes = numVotes;
        this.votedByUser = votedByUser;
        this.username = product.getOwner().getUsername();
        this.createdAt = product.getCreatedAt();
    }

    public UserProductDetailsDTO(UserProduct product, Integer numVotes) {
        this.id = product.getId();
        this.name = product.getName();
        this.images = product.getImages();
        this.numVotes = numVotes;
        this.username = product.getOwner().getUsername();
        this.status = product.getStatus();
        this.createdAt = product.getCreatedAt();
    }

    public UserProductDetailsDTO(UserProduct userProduct) {
        this.id = userProduct.getId();
        this.name = userProduct.getName();
        this.images = userProduct.getImages();
        this.username = userProduct.getOwner().getUsername();
        this.status = userProduct.getStatus();
    }

}
