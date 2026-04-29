package com.fluxbanker.api.repository;

import com.fluxbanker.api.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, UUID> {
    @EntityGraph(attributePaths = {"user"})
    Optional<Session> findById(UUID id);
}
