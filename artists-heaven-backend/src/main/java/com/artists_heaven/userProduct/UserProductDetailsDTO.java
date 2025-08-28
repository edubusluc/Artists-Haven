package com.artists_heaven.userProduct;

import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserProductDetailsDTO {
    private Long id;
    private String name;
    private List<String> images;
    private Integer numVotes;
    private boolean votedByUser;
    private String username;
    private Status status;
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
