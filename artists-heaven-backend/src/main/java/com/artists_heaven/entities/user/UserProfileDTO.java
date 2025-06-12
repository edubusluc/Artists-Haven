package com.artists_heaven.entities.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileDTO {

    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String artistName;
    private String address;
    private String role;
    private String city;

    /*
     * Default constructor (necessary for Jackson)
     * For serialization and deserialization of the class.
     */
    public UserProfileDTO() {
    }

    // Constructor for User
    public UserProfileDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.address = user.getAddress();
        this.city = user.getCity();
        this.role = user.getRole().name();

    }
}
