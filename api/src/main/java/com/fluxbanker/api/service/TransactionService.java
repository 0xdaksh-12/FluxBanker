package com.fluxbanker.api.service;

import com.fluxbanker.api.dto.TransactionDto;
import com.fluxbanker.api.entity.Account;
import com.fluxbanker.api.entity.Transaction;
import com.fluxbanker.api.event.TransactionEvent;
import com.fluxbanker.api.exception.ForbiddenException;
import com.fluxbanker.api.kafka.TransactionEventProducer;
import com.fluxbanker.api.repository.AccountRepository;
import com.fluxbanker.api.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionEventProducer eventProducer;

    @Transactional(readOnly = true)
    public void validateAccountOwnership(UUID accountId, UUID userId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        if (!account.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You do not have permission to access this account");
        }
    }

    @Transactional(readOnly = true)
    public Page<TransactionDto> getTransactionsForAccount(UUID accountId, Pageable pageable) {
        return transactionRepository.findByAccountIdOrderByTimestampDesc(accountId, pageable)
                .map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public Page<TransactionDto> getTransactionsForUser(UUID userId, Pageable pageable) {
        return transactionRepository.findByAccountUserIdOrderByTimestampDesc(userId, pageable)
                .map(this::mapToDto);
    }

    @Transactional
    public void transferMoney(UUID sourceAccountId, UUID destinationAccountId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }

        Account sourceAccount = accountRepository.findById(sourceAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Source account not found"));
        Account destAccount = accountRepository.findById(destinationAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Destination account not found"));

        if (sourceAccount.getAvailableBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds");
        }

        // Debit Source
        sourceAccount.setAvailableBalance(sourceAccount.getAvailableBalance().subtract(amount));
        sourceAccount.setCurrentBalance(sourceAccount.getCurrentBalance().subtract(amount));
        accountRepository.save(sourceAccount);

        Transaction debitTx = Transaction.builder()
                .account(sourceAccount)
                .amount(amount.negate())
                .type(Transaction.Type.TRANSFER)
                .status(Transaction.Status.COMPLETED)
                .category("Transfer to " + destAccount.getMask())
                .counterpartyName(destAccount.getName())
                .build();
        transactionRepository.save(debitTx);

        // Credit Destination
        destAccount.setAvailableBalance(destAccount.getAvailableBalance().add(amount));
        destAccount.setCurrentBalance(destAccount.getCurrentBalance().add(amount));
        accountRepository.save(destAccount);

        Transaction creditTx = Transaction.builder()
                .account(destAccount)
                .amount(amount)
                .type(Transaction.Type.TRANSFER)
                .status(Transaction.Status.COMPLETED)
                .category("Transfer from " + sourceAccount.getMask())
                .counterpartyName(sourceAccount.getName())
                .build();
        transactionRepository.save(creditTx);

        // Publish event
        eventProducer.publish(TransactionEvent.transfer(sourceAccountId, destinationAccountId, amount));
    }

    @Transactional
    public TransactionDto depositFunds(UUID accountId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        account.setAvailableBalance(account.getAvailableBalance().add(amount));
        account.setCurrentBalance(account.getCurrentBalance().add(amount));
        accountRepository.save(account);

        Transaction depositTx = Transaction.builder()
                .account(account)
                .amount(amount)
                .type(Transaction.Type.DEPOSIT)
                .status(Transaction.Status.COMPLETED)
                .category("Simulated Deposit")
                .counterpartyName("System")
                .build();

        Transaction savedTx = transactionRepository.save(depositTx);
        eventProducer.publish(TransactionEvent.deposit(accountId, amount));

        return mapToDto(savedTx);
    }

    private TransactionDto mapToDto(Transaction transaction) {
        return TransactionDto.builder()
                .id(transaction.getId())
                .accountId(transaction.getAccount().getId())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .status(transaction.getStatus())
                .category(transaction.getCategory())
                .counterpartyName(transaction.getCounterpartyName())
                .timestamp(transaction.getTimestamp())
                .build();
    }
}
