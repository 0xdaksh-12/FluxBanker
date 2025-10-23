package com.fluxbanker.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity representing an authenticated user session. Mirrors Node's Session
 * Mongoose model.
 */
@Entity
@Table(name = "sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Lazily-loaded user reference — mirrors Mongoose's { ref: 'User' } without
     * auto-population.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "is_valid", nullable = false)
    @Builder.Default
    private boolean valid = true;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "ip_address")
    private String ip;

    /** BCrypt-hashed refresh token, set after session creation. */
    @Column(name = "refresh_token_hash")
    private String token;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;
}
