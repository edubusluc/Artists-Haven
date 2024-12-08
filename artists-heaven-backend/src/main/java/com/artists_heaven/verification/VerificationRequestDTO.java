package com.artists_heaven.verification;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerificationRequestDTO {
    private String email;
    private String url;
    private MultipartFile video;
}
