package com.QuadGPT.backend.auth.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private static final long EXPIRATION_TIME =
            1000 * 60 * 60; // 1 heure

    private static final String ISSUER = "quadgpt";
    private static final String AUDIENCE = "quadgpt-api";

    private final SecretKey key;

   public JwtService(@Value("${JWT_SECRET}") String secret) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET must be at least 32 characters long"
            );
        }

        this.key = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }

    public String generateToken(String email) {

        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
            .subject(email)
            .issuer(ISSUER)
            .audience().add(AUDIENCE).and()
            .issuedAt(now)
            .expiration(expiration)
            .signWith(key)
            .compact();
    }

    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(ISSUER)
                .requireAudience(AUDIENCE)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            extractEmail(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}