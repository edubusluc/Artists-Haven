package com.artists_heaven.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {
    private String token;
    private String newPassword;

    public ResetPasswordRequest(String token, String newPassword){
        this.token = token;
        this.newPassword = newPassword;
    }
}
