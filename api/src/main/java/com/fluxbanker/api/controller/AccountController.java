package com.fluxbanker.api.controller;

import com.fluxbanker.api.dto.AccountDto;
import com.fluxbanker.api.dto.TransactionDto;
import com.fluxbanker.api.entity.Account;
import com.fluxbanker.api.service.AccountService;
import com.fluxbanker.api.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.fluxbanker.api.security.CustomUserDetails;
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
    public ResponseEntity<List<AccountDto>> getMyAccounts(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null)
            return ResponseEntity.status(401).build();
        return ResponseEntity.ok(accountService.getAccountsForUser(userDetails.getUserId()));
    }

    @PostMapping({ "", "/mock" })
    public ResponseEntity<AccountDto> provisionAccount(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody Map<String, String> request) {

        if (userDetails == null)
            return ResponseEntity.status(401).build();

        UUID userId = userDetails.getUserId();
        String name = request.getOrDefault("name", "Flux Checking");
        Account.Subtype subtype = Account.Subtype.valueOf(request.getOrDefault("subtype", "CHECKING").toUpperCase());

        return ResponseEntity.ok(accountService.provisionAccount(userId, name, subtype));
    }

    @PostMapping("/apply/loan")
    public ResponseEntity<AccountDto> applyForLoan(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody Map<String, Object> request) {

        if (userDetails == null)
            return ResponseEntity.status(401).build();

        UUID userId = userDetails.getUserId();
        BigDecimal principalAmount = new BigDecimal(request.getOrDefault("principalAmount", "5000").toString());
        Integer termMonths = Integer.parseInt(request.getOrDefault("termMonths", "36").toString());

        return ResponseEntity.ok(accountService.applyForLoan(userId, principalAmount, termMonths));
    }

    @PostMapping("/apply/credit")
    public ResponseEntity<AccountDto> applyForCredit(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody Map<String, Object> request) {

        if (userDetails == null)
            return ResponseEntity.status(401).build();

        UUID userId = userDetails.getUserId();
        BigDecimal creditLimit = new BigDecimal(request.getOrDefault("creditLimit", "10000").toString());

        return ResponseEntity.ok(accountService.openCreditCard(userId, creditLimit));
    }

    @PostMapping("/{accountId}/deposit")
    public ResponseEntity<TransactionDto> simulateDeposit(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID accountId,
            @RequestBody Map<String, BigDecimal> request) {

        if (userDetails == null)
            return ResponseEntity.status(401).build();

        BigDecimal amount = request.getOrDefault("amount", BigDecimal.ZERO);
        UUID userId = userDetails.getUserId();

        List<AccountDto> myAccounts = accountService.getAccountsForUser(userId);

        boolean ownsAccount = myAccounts.stream().anyMatch(a -> a.getId().equals(accountId));

        if (!ownsAccount) {
            return ResponseEntity.status(403).build();
        }

        TransactionDto tx = transactionService.depositFunds(accountId, amount);
        accountService.evictUserAccountsCache(userId);

        return ResponseEntity.ok(tx);
    }

    @PostMapping("/{accountId}/withdraw")
    public ResponseEntity<TransactionDto> simulateWithdrawal(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID accountId,
            @RequestBody Map<String, BigDecimal> request) {

        if (userDetails == null)
            return ResponseEntity.status(401).build();

        BigDecimal amount = request.getOrDefault("amount", BigDecimal.ZERO);
        UUID userId = userDetails.getUserId();

        List<AccountDto> myAccounts = accountService.getAccountsForUser(userId);

        boolean ownsAccount = myAccounts.stream().anyMatch(a -> a.getId().equals(accountId));

        if (!ownsAccount) {
            return ResponseEntity.status(403).build();
        }

        TransactionDto tx = transactionService.withdrawFunds(accountId, amount);
        accountService.evictUserAccountsCache(userId);

        return ResponseEntity.ok(tx);
    }

    @PostMapping("/{accountId}/transfer/external")
    public ResponseEntity<Void> externalTransfer(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID accountId,
            @RequestBody Map<String, Object> request) {

        if (userDetails == null)
            return ResponseEntity.status(401).build();

        UUID userId = userDetails.getUserId();

        List<AccountDto> myAccounts = accountService.getAccountsForUser(userId);
        boolean ownsAccount = myAccounts.stream().anyMatch(a -> a.getId().equals(accountId));

        if (!ownsAccount) {
            return ResponseEntity.status(403).build();
        }

        BigDecimal amount = new BigDecimal(request.getOrDefault("amount", "0").toString());
        String routingNumber = (String) request.get("routingNumber");
        String accountNumber = (String) request.get("accountNumber");
        String recipientName = (String) request.get("recipientName");

        transactionService.externalTransfer(accountId, routingNumber, accountNumber, recipientName, amount);
        accountService.evictUserAccountsCache(userId);

        return ResponseEntity.ok().build();
    }
}
