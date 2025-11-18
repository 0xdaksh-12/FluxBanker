package com.fluxbanker.api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "password_reset_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    @Column(nullable = false)
    private Instant expiryDate;

    @Builder.Default
    @Column(nullable = false)
    private int clickCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private int maxClicks = 3;

    @Builder.Default
    @Column(nullable = false)
    private boolean used = false;

    public boolean isExpired() {
        return expiryDate.isBefore(Instant.now());
    }

    public boolean isInvalid() {
        return used || isExpired() || clickCount >= maxClicks;
    }
}
