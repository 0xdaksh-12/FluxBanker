package com.fluxbanker.api.service;

import com.fluxbanker.api.dto.AccountDto;
import com.fluxbanker.api.entity.Account;
import com.fluxbanker.api.entity.CreditDetails;
import com.fluxbanker.api.entity.LoanDetails;
import com.fluxbanker.api.entity.User;
import com.fluxbanker.api.repository.AccountRepository;
import com.fluxbanker.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Cacheable(value = "userAccounts", key = "#userId")
    @Transactional(readOnly = true)
    public List<AccountDto> getAccountsForUser(UUID userId) {
        return accountRepository.findByUserId(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = "userAccounts", key = "#userId")
    @Transactional
    public AccountDto provisionAccount(UUID userId, String accountName, Account.Subtype subtype) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        SecureRandom random = new SecureRandom();
        // Generate a 10-digit account number (simulated)
        long accountNum = 1000000000L + (long) (random.nextDouble() * 8999999999L);
        String accountNumber = String.valueOf(accountNum);
        String mask = accountNumber.substring(accountNumber.length() - 4);

        // Give the mock account a random starting balance between $1,000 and $10,000
        BigDecimal startingBalance = BigDecimal.valueOf(1000 + (random.nextDouble() * 9000));

        Account.Type type = (subtype == Account.Subtype.CREDIT_CARD) ? Account.Type.CREDIT : Account.Type.DEPOSITORY;

        Account account = Account.builder()
                .user(user)
                .name(accountName)
                .accountNumber(accountNumber)
                .mask(mask)
                .currentBalance(startingBalance)
                .availableBalance(startingBalance)
                .type(type)
                .subtype(subtype)
                .build();

        Account savedAccount = accountRepository.save(account);
        return mapToDto(savedAccount);
    }

    @CacheEvict(value = "userAccounts", key = "#userId")
    @Transactional
    public AccountDto applyForLoan(UUID userId, BigDecimal principalAmount, Integer termMonths) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        SecureRandom random = new SecureRandom();
        long accountNum = 1000000000L + (long) (random.nextDouble() * 8999999999L);
        String accountNumber = String.valueOf(accountNum);

        Account account = Account.builder()
                .user(user)
                .name("Personal Loan")
                .accountNumber(accountNumber)
                .mask(accountNumber.substring(accountNumber.length() - 4))
                .currentBalance(principalAmount) // Outstanding principal
                .availableBalance(BigDecimal.ZERO)
                .type(Account.Type.LOAN)
                .subtype(Account.Subtype.MONEY_MARKET) // No specific loan subtype right now, let's just use MONEY_MARKET or add one later
                .build();

        LoanDetails loanDetails = LoanDetails.builder()
                .account(account)
                .originalPrincipal(principalAmount)
                .interestRate(BigDecimal.valueOf(0.0599)) // 5.99% fixed mock
                .termMonths(termMonths)
                .monthlyPayment(principalAmount.divide(BigDecimal.valueOf(termMonths), 4, java.math.RoundingMode.HALF_UP))
                .nextPaymentDueDate(java.time.LocalDate.now().plusMonths(1))
                .build();

        account.setLoanDetails(loanDetails);

        Account savedAccount = accountRepository.save(account);
        return mapToDto(savedAccount);
    }

    @CacheEvict(value = "userAccounts", key = "#userId")
    @Transactional
    public AccountDto openCreditCard(UUID userId, BigDecimal creditLimit) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        SecureRandom random = new SecureRandom();
        long accountNum = 4000000000000000L + (long) (random.nextDouble() * 999999999999999L);
        String accountNumber = String.valueOf(accountNum);

        Account account = Account.builder()
                .user(user)
                .name("Flux Rewards Visa")
                .accountNumber(accountNumber)
                .mask(accountNumber.substring(accountNumber.length() - 4))
                .currentBalance(BigDecimal.ZERO) // No balance initially
                .availableBalance(creditLimit) // Full credit limit available
                .type(Account.Type.CREDIT)
                .subtype(Account.Subtype.CREDIT_CARD)
                .build();

        CreditDetails creditDetails = CreditDetails.builder()
                .account(account)
                .creditLimit(creditLimit)
                .apr(BigDecimal.valueOf(0.1999)) // 19.99% APR
                .statementBalance(BigDecimal.ZERO)
                .minimumPaymentDue(BigDecimal.ZERO)
                .nextPaymentDueDate(java.time.LocalDate.now().plusMonths(1))
                .build();

        account.setCreditDetails(creditDetails);

        Account savedAccount = accountRepository.save(account);
        return mapToDto(savedAccount);
    }

    @CacheEvict(value = "userAccounts", key = "#userId")
    public void evictUserAccountsCache(UUID userId) {
        log.info("Evicting user accounts cache for user: {}", userId);
    }

    @CacheEvict(value = "userAccounts", allEntries = true)
    public void evictAllUserAccountsCache() {
        // Just for manual eviction
    }

    private AccountDto mapToDto(Account account) {
        return AccountDto.builder()
                .id(account.getId())
                .name(account.getName())
                .mask(account.getMask())
                .currentBalance(account.getCurrentBalance())
                .availableBalance(account.getAvailableBalance())
                .type(account.getType())
                .subtype(account.getSubtype())
                .build();
    }
}
