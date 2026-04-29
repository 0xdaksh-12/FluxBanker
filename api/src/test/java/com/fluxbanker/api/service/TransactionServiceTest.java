package com.fluxbanker.api.service;

import com.fluxbanker.api.dto.TransactionDto;
import com.fluxbanker.api.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.UUID;
import com.fluxbanker.api.entity.Transaction;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void testGetRecentTransactions() {
        UUID accountId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);
        Page<Transaction> page = new PageImpl<>(Collections.singletonList(new Transaction()));

        when(transactionRepository.findByAccountIdOrderByTimestampDesc(accountId, pageable))
                .thenReturn(page);

        Page<TransactionDto> transactions = transactionService.getTransactionsForAccount(accountId, pageable);
        assertFalse(transactions.isEmpty());
    }
}
