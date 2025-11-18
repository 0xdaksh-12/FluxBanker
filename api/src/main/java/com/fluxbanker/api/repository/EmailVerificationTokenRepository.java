package com.fluxbanker.api.repository;

import com.fluxbanker.api.entity.EmailVerificationToken;
import com.fluxbanker.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {
    Optional<EmailVerificationToken> findByToken(String token);

    void deleteByUser(User user);
}
