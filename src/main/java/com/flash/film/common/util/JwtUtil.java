package com.flash.film.common.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flash.film.common.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final AppProperties appProperties;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private SecretKey buildKey(String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            keyBytes = Arrays.copyOf(keyBytes, 32);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // ── Token generation ────────────────────────────────────────────────────

    public String generateAccessToken(Long userId, String username, String userSecret) {
        long expMs = appProperties.getJwt().getAccessTokenExpirationMs();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .claim("type", "access")
                .claim("user_id", userId)
                .claim("username", username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expMs))
                .signWith(buildKey(userSecret))
                .compact();
    }

    public String generateRefreshToken(Long userId, String username, String userSecret) {
        long expMs = appProperties.getJwt().getRefreshTokenExpirationMs();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .claim("type", "refresh")
                .claim("user_id", userId)
                .claim("username", username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expMs))
                .signWith(buildKey(userSecret))
                .compact();
    }

    // ── Token parsing ───────────────────────────────────────────────────────

    public Claims parseToken(String token, String userSecret) {
        return Jwts.parser()
                .verifyWith(buildKey(userSecret))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token, String userSecret) {
        try {
            parseToken(token, userSecret);
            return true;
        } catch (Exception e) {
            log.debug("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    public Long getUserIdFromToken(String token, String userSecret) {
        return parseToken(token, userSecret).get("user_id", Long.class);
    }

    public String getJtiFromToken(String token, String userSecret) {
        return parseToken(token, userSecret).getId();
    }

    public String getTokenType(String token, String userSecret) {
        return parseToken(token, userSecret).get("type", String.class);
    }

    // ── Payload decode (no verification) ───────────────────────────────────
    // Dùng để lấy userId trước, sau đó mới load user secret từ DB để verify

    public String decodePayloadRaw(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) throw new IllegalArgumentException("Invalid JWT format");
        byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
        return new String(decoded, StandardCharsets.UTF_8);
    }

    public Long extractUserIdWithoutVerification(String token) {
        try {
            JsonNode node = MAPPER.readTree(decodePayloadRaw(token));
            return node.get("user_id").asLong();
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot decode JWT payload", e);
        }
    }
}
