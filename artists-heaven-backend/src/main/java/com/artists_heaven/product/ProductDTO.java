package com.artists_heaven.product;

import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductDTO {

    @NotBlank
    private String name;

    @Size(max = 1000)
    private String description;

    @NotNull
    @Positive
    private Float price;

    private Map<String,Integer> sizes;

    private Set<Category> categories;

    private List<String> images;

}
