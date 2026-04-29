package com.fluxbanker.api.service;

import com.fluxbanker.api.exception.UnauthorizedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

/**
 * JWT token generation and verification.
 * Mirrors Node's lib/jwt.js and utils/isTokenHalfExpired.js.
 */
@Service
public class JwtService {

    @Value("${jwt.secret.access}")
    private String accessSecret;

    @Value("${jwt.secret.refresh}")
    private String refreshSecret;

    @Value("${jwt.expiration-ms}")
    private long accessExpirationMs;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    /**
     * Generates a signed access token containing userId and sessionId.
     *
     * @param userId    the authenticated user's ID
     * @param sessionId the created session's ID
     * @return signed JWT string
     */
    public String generateAccessToken(UUID userId, UUID sessionId) {
        return buildToken(userId, sessionId, accessSecret, accessExpirationMs);
    }

    /**
     * Generates a signed refresh token containing userId and sessionId.
     *
     * @param userId    the authenticated user's ID
     * @param sessionId the created session's ID
     * @return signed JWT string
     */
    public String generateRefreshToken(UUID userId, UUID sessionId) {
        return buildToken(userId, sessionId, refreshSecret, refreshExpirationMs);
    }

    /**
     * Verifies an access token and returns its claims.
     *
     * @param token the JWT string
     * @return parsed Claims
     * @throws UnauthorizedException if token is expired or invalid
     */
    public Claims verifyAccessToken(String token) {
        return verify(token, accessSecret);
    }

    /**
     * Verifies a refresh token and returns its claims.
     *
     * @param token the JWT string
     * @return parsed Claims
     * @throws UnauthorizedException if token is expired or invalid
     */
    public Claims verifyRefreshToken(String token) {
        return verify(token, refreshSecret);
    }

    /**
     * Returns true if the token has passed the halfway point of its lifetime.
     * Mirrors utils/isTokenHalfExpired.js.
     *
     * @param claims parsed token claims
     * @return true if elapsed time >= half the total lifetime
     */
    public boolean isTokenHalfExpired(Claims claims) {
        long issuedAt = claims.getIssuedAt().getTime();
        long expiry = claims.getExpiration().getTime();
        long now = System.currentTimeMillis();
        long lifetime = expiry - issuedAt;
        long elapsed = now - issuedAt;
        return elapsed >= lifetime / 2;
    }

    private String buildToken(UUID userId, UUID sessionId, String secret, long expirationMs) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("sessionId", sessionId.toString())
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMs))
                .signWith(signingKey(secret))
                .compact();
    }

    private Claims verify(String token, String secret) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey(secret))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException ex) {
            throw new UnauthorizedException("Token expired", ex);
        } catch (Exception ex) {
            throw new UnauthorizedException("Invalid token", ex);
        }
    }

    /** Decodes the base64 secret into a HMAC-SHA key. */
    private SecretKey signingKey(String secret) {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }
}
