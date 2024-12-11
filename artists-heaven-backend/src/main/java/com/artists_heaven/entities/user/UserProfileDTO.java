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

    /*
     * Default constructor (necessary for Jackson)
     * For serialization and deserialization of the class.
     */
    public UserProfileDTO() {
    }

    // Constructor for User
    public UserProfileDTO(User user) {
        this.email = user.getEmail();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.username = user.getUsername();
    }
}
