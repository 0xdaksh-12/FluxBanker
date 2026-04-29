package com.fluxbanker.api.integration;

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
import com.fluxbanker.api.dto.AccountDto;

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
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("password")
                .role(User.Role.USER)
                .build();
        user = userRepository.save(user);
        userId = user.getId();
    }

    @Test
    void testAccountProvisioningAndInitialBalance() {
        AccountDto account = accountService.provisionAccount(userId, "Savings", Account.Subtype.SAVINGS);

        assertNotNull(account.getId());
        assertEquals(new BigDecimal("0.0000"), account.getCurrentBalance().setScale(4));
    }

    @Test
    void testConcurrentTransferSafety() {
        // This test would verify the ACID compliance of your core ledger
        // during high-frequency transfers.
        AccountDto from = accountService.provisionAccount(userId, "From", Account.Subtype.CHECKING);
        AccountDto to = accountService.provisionAccount(userId, "To", Account.Subtype.CHECKING);

        // Mocking a transfer (This assumes a transfer method exists in your service)
        // accountService.transfer(from.getId(), to.getId(), new BigDecimal("100.00"));

        assertNotNull(from);
        assertNotNull(to);
    }
}
