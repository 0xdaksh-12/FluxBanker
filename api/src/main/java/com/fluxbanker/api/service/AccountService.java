package com.fluxbanker.api.service;

import com.fluxbanker.api.dto.AccountDto;
import com.fluxbanker.api.entity.Account;
import com.fluxbanker.api.entity.User;
import com.fluxbanker.api.repository.AccountRepository;
import com.fluxbanker.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
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

        Random random = new Random();
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
    public void evictUserAccountsCache(UUID userId) {
        // Just for manual eviction
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
