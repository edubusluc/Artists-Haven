package com.artists_heaven.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;

public interface TokenVerifier {
    GoogleIdToken verifyToken(String idTokenString) throws Exception;
}
