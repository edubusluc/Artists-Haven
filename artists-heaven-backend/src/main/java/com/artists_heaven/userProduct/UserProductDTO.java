package com.artists_heaven.userProduct;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProductDTO {

    private String name;
    private String description;
    private List<String> images;

}
