package com.artists_heaven.entities.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegisterDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String password;
    private String phone;
    private String country;
    private String postalCode;
    private String city;
    private String address;
}
