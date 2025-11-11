package com.fluxbanker.api.repository;

import com.fluxbanker.api.entity.Session;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, UUID> {

    @Override
    @EntityGraph(attributePaths = {"user"})
    Optional<Session> findById(UUID id);

    @Modifying
    @Query("UPDATE Session s SET s.valid = false WHERE s.user.id = :userId")
    void invalidateAllByUserId(@Param("userId") UUID userId);
}
