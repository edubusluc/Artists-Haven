package com.artists_heaven.admin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CollectionDTO {
    private Long id;
    private String name;
    private Boolean isPromoted;

    public CollectionDTO(Long id, String name, Boolean isPromoted) {
        this.id = id;
        this.name = name;
        this.isPromoted = isPromoted;
    }

}
