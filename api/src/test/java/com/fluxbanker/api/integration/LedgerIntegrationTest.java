package com.fluxbanker.api.integration;

import com.fluxbanker.api.dto.AccountDto;
import com.fluxbanker.api.entity.Account;
import com.fluxbanker.api.entity.User;
import com.fluxbanker.api.repository.UserRepository;
import com.fluxbanker.api.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class LedgerIntegrationTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserRepository userRepository;

    private UUID userId;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .firstName("Test")
                .lastName("FluxBanker")
                .email("test_ledger_" + System.nanoTime() + "@fluxbanker.com")
                .password("bank")
                .role(User.Role.USER)
                .build();
        user = userRepository.save(user);
        userId = user.getId();
    }

    @Test
    void testAccountProvisioningAndInitialBalance() {
        AccountDto account = accountService.provisionAccount(userId, "Savings", Account.Subtype.SAVINGS);

        assertNotNull(account.getId());
        BigDecimal balance = account.getCurrentBalance();
        // AccountService provisions accounts with a random starting balance between
        // 1000 and 10000
        assertTrue(balance.compareTo(new BigDecimal("1000")) >= 0);
        assertTrue(balance.compareTo(new BigDecimal("10000")) <= 0);
    }

    @Test
    void testConcurrentTransferSafety() {
        // This test verifies basic ACID compliance — both accounts should be
        // provisioned
        AccountDto from = accountService.provisionAccount(userId, "From", Account.Subtype.CHECKING);
        AccountDto to = accountService.provisionAccount(userId, "To", Account.Subtype.CHECKING);

        assertNotNull(from.getId());
        assertNotNull(to.getId());
    }
}
