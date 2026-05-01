package com.fluxbanker.api.controller;

import com.fluxbanker.api.dto.TransactionDto;
import com.fluxbanker.api.security.CustomUserDetails;
import com.fluxbanker.api.service.AccountService;
import com.fluxbanker.api.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final AccountService accountService;

    @GetMapping("/account/{accountId}")
    public ResponseEntity<Page<TransactionDto>> getAccountTransactions(
            @PathVariable UUID accountId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        transactionService.validateAccountOwnership(accountId, userDetails.getUserId());
        Page<TransactionDto> transactions = transactionService.getTransactionsForAccount(accountId, pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/user")
    public ResponseEntity<Page<TransactionDto>> getUserTransactions(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<TransactionDto> transactions = transactionService.getTransactionsForUser(userDetails.getUserId(),
                pageable);
        return ResponseEntity.ok(transactions);
    }

    @PostMapping("/transfer")
    public ResponseEntity<Map<String, String>> transferMoney(
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Object sourceObj = request.get("sourceAccountId");
        Object destObj = request.get("destinationAccountId");
        Object amountObj = request.get("amount");

        if (sourceObj == null || destObj == null || amountObj == null) {
            return ResponseEntity.badRequest().build();
        }

        UUID sourceAccountId = UUID.fromString(sourceObj.toString());
        UUID destinationAccountId = UUID.fromString(destObj.toString());
        BigDecimal amount = new BigDecimal(amountObj.toString());

        transactionService.validateAccountOwnership(sourceAccountId, userDetails.getUserId());
        transactionService.transferMoney(sourceAccountId, destinationAccountId, amount);

        // Evict cache as balances changed
        accountService.evictUserAccountsCache(userDetails.getUserId());

        return ResponseEntity.ok(Map.of("status", "Transfer successful"));
    }
}
