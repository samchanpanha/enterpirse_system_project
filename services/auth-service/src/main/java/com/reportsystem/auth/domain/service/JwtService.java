package com.reportsystem.auth.domain.service;

import com.reportsystem.shared.security.JwtTokenProvider;
import java.util.Base64;
import javax.crypto.SecretKey;
import io.jsonwebtoken.security.Keys;

public class JwtService {

    private final JwtTokenProvider tokenProvider;

    public JwtService(String secret, long accessTokenExpiration, long refreshTokenExpiration) {
        this.tokenProvider = new JwtTokenProvider(secret, accessTokenExpiration, refreshTokenExpiration);
    }

    public JwtTokenProvider getTokenProvider() {
        return tokenProvider;
    }

    public static String generateSecret() {
        SecretKey key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
}
