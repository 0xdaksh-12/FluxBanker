package com.fluxbanker.api.service;

import com.fluxbanker.api.entity.Session;
import com.fluxbanker.api.entity.User;
import com.fluxbanker.api.exception.NotFoundException;
import com.fluxbanker.api.exception.UnauthorizedException;
import com.fluxbanker.api.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.UUID;

/**
 * Session lifecycle management.
 * Mirrors Node's services/sessionService.js.
 */
@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;

    /**
     * Creates a new valid session for the given user.
     *
     * @param user      the authenticated user
     * @param userAgent request User-Agent header
     * @param ip        client IP address
     * @return the persisted session
     */
    public Session createSession(User user, String userAgent, String ip) {
        Session session = Session.builder()
                .user(user)
                .userAgent(userAgent)
                .ip(ip)
                .valid(true)
                .expiresAt(java.time.Instant.now().plusMillis(604800000)) // 7 days
                .build();
        return sessionRepository.save(session);
    }

    /**
     * Stores a SHA-256 hashed refresh token on an existing session.
     *
     * @param sessionId    the session's UUID
     * @param refreshToken the plaintext refresh token to hash and store
     */
    public void saveToken(UUID sessionId, String refreshToken) {
        Session session = findById(sessionId);
        session.setToken(hashToken(refreshToken));
        sessionRepository.save(session);
    }

    /**
     * Marks a session as invalid (logout).
     *
     * @param sessionId the session's UUID
     */
    public void invalidateSession(UUID sessionId) {
        Session session = findById(sessionId);
        session.setValid(false);
        sessionRepository.save(session);
    }

    /**
     * Retrieves a session by ID without validation.
     *
     * @param sessionId the session's UUID
     * @return the found session
     * @throws NotFoundException if no session exists with that ID
     */
    public Session getSession(UUID sessionId) {
        return findById(sessionId);
    }

    /**
     * Validates that a session exists, is active, and that the cookie token matches
     * the stored SHA-256 hash. Mirrors Node's checkSession().
     *
     * @param cookieToken raw refresh token from the cookie
     * @param sessionId   the claimed session ID
     * @return the validated session
     * @throws UnauthorizedException if the session is invalid or the token doesn't
     *                               match
     */
    public Session checkSession(String cookieToken, UUID sessionId) {
        Session session = findById(sessionId);

        if (!session.isValid()) {
            throw new UnauthorizedException("Session is invalid");
        }
        if (!hashToken(cookieToken).equals(session.getToken())) {
            throw new UnauthorizedException("Session is invalid");
        }
        return session;
    }

    private Session findById(UUID sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Session not found"));
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash token", e);
        }
    }
}
