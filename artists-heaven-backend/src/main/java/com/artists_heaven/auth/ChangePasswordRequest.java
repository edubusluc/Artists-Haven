package com.artists_heaven.auth;

import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter
public class ChangePasswordRequest {
    private String currentPassword;
    private String newPassword;

    public ChangePasswordRequest(String currentPassword, String newPassword){
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }
}
