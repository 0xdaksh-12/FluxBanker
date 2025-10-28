package com.fluxbanker.api.repository;

import com.fluxbanker.api.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Page<Transaction> findByAccountIdOrderByTimestampDesc(UUID accountId, Pageable pageable);
    
    Page<Transaction> findByAccountUserIdOrderByTimestampDesc(UUID userId, Pageable pageable);
}
