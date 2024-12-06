package com.artists_heaven.entities.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileDTO {

    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String artistName;

    // Constructor predeterminado (necesario para Jackson)
    public UserProfileDTO() {}

    // Constructor para User
    public UserProfileDTO(User user) {
        this.email = user.getEmail();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.username = user.getUsername();
    }

}

