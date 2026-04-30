package com.fluxbanker.api.controller;

import com.fluxbanker.api.dto.AccountDto;
import com.fluxbanker.api.dto.TransactionDto;
import com.fluxbanker.api.entity.Account;
import com.fluxbanker.api.security.SecurityUtils;

import com.fluxbanker.api.service.AccountService;
import com.fluxbanker.api.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<List<AccountDto>> getMyAccounts(Authentication authentication) {
        UUID userId = SecurityUtils.getUserId(authentication);
        if (userId == null)
            return ResponseEntity.status(401).build();
        return ResponseEntity.ok(accountService.getAccountsForUser(userId));
    }

    @PostMapping({ "", "/mock" })
    public ResponseEntity<AccountDto> provisionAccount(
            Authentication authentication,
            @RequestBody Map<String, String> request) {

        UUID userId = SecurityUtils.getUserId(authentication);
        if (userId == null)
            return ResponseEntity.status(401).build();

        String name = request.getOrDefault("name", "Flux Checking");
        Account.Subtype subtype = Account.Subtype.valueOf(request.getOrDefault("subtype", "CHECKING").toUpperCase());

        return ResponseEntity.ok(accountService.provisionAccount(userId, name, subtype));
    }

    @PostMapping("/{accountId}/deposit")
    public ResponseEntity<TransactionDto> simulateDeposit(
            Authentication authentication,
            @PathVariable UUID accountId,
            @RequestBody Map<String, BigDecimal> request) {

        BigDecimal amount = request.getOrDefault("amount", BigDecimal.ZERO);
        UUID userId = SecurityUtils.getUserId(authentication);
        if (userId == null)
            return ResponseEntity.status(401).build();

        List<AccountDto> myAccounts = accountService.getAccountsForUser(userId);

        boolean ownsAccount = myAccounts.stream().anyMatch(a -> a.getId().equals(accountId));

        if (!ownsAccount) {
            return ResponseEntity.status(403).build();
        }

        TransactionDto tx = transactionService.depositFunds(accountId, amount);
        accountService.evictUserAccountsCache(userId);

        return ResponseEntity.ok(tx);
    }
}
