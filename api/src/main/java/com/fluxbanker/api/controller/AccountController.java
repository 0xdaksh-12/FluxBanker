package com.fluxbanker.api.controller;

import com.fluxbanker.api.dto.AccountDto;
import com.fluxbanker.api.dto.TransactionDto;
import com.fluxbanker.api.entity.Account;
import com.fluxbanker.api.security.CustomUserDetails;
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
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(accountService.getAccountsForUser(userDetails.getUserId()));
    }

    @PostMapping({"", "/mock"})
    public ResponseEntity<AccountDto> provisionAccount(
            Authentication authentication,
            @RequestBody Map<String, String> request) {
        
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String name = request.getOrDefault("name", "Flux Checking");
        Account.Subtype subtype = Account.Subtype.valueOf(request.getOrDefault("subtype", "CHECKING").toUpperCase());
        
        return ResponseEntity.ok(accountService.provisionAccount(userDetails.getUserId(), name, subtype));
    }

    @PostMapping("/{accountId}/deposit")
    public ResponseEntity<TransactionDto> simulateDeposit(
            Authentication authentication,
            @PathVariable UUID accountId,
            @RequestBody Map<String, BigDecimal> request) {
        
        BigDecimal amount = request.getOrDefault("amount", BigDecimal.ZERO);
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        
        List<AccountDto> myAccounts = accountService.getAccountsForUser(userDetails.getUserId());
        boolean ownsAccount = myAccounts.stream().anyMatch(a -> a.getId().equals(accountId));
        
        if (!ownsAccount) {
            return ResponseEntity.status(403).build();
        }

        TransactionDto tx = transactionService.depositFunds(accountId, amount);
        accountService.evictUserAccountsCache(userDetails.getUserId());
        
        return ResponseEntity.ok(tx);
    }
}
