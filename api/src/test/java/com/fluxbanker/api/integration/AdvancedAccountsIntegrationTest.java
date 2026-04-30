package com.fluxbanker.api.integration;

import com.fluxbanker.api.dto.AccountDto;
import com.fluxbanker.api.entity.Account;
import com.fluxbanker.api.entity.User;
import com.fluxbanker.api.repository.AccountRepository;
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
public class AdvancedAccountsIntegrationTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    private UUID userId;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .firstName("Advanced")
                .lastName("User")
                .email("advanced_" + System.nanoTime() + "@fluxbanker.com")
                .password("secure123")
                .role(User.Role.USER)
                .build();
        user = userRepository.save(user);
        userId = user.getId();
    }

    @Test
    void testApplyForLoan() {
        BigDecimal principal = new BigDecimal("10000.00");
        int termMonths = 60;

        AccountDto loanAccountDto = accountService.applyForLoan(userId, principal, termMonths);

        assertNotNull(loanAccountDto.getId());
        assertEquals(Account.Type.LOAN, loanAccountDto.getType());
        
        // Ensure balance reflects outstanding principal
        assertEquals(principal, loanAccountDto.getCurrentBalance());

        // Verify database relationship
        Account account = accountRepository.findById(loanAccountDto.getId()).orElseThrow();
        assertNotNull(account.getLoanDetails(), "LoanDetails should not be null");
        assertEquals(termMonths, account.getLoanDetails().getTermMonths());
        
        // Assert interest rate (e.g. 5.5%)
        assertNotNull(account.getLoanDetails().getInterestRate());
        
        // Assert monthly payment calculation
        assertNotNull(account.getLoanDetails().getMonthlyPayment());
        assertTrue(account.getLoanDetails().getMonthlyPayment().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testOpenCreditCard() {
        BigDecimal creditLimit = new BigDecimal("5000.00");

        AccountDto creditCardDto = accountService.openCreditCard(userId, creditLimit);

        assertNotNull(creditCardDto.getId());
        assertEquals(Account.Type.CREDIT, creditCardDto.getType());
        assertEquals(Account.Subtype.CREDIT_CARD, creditCardDto.getSubtype());

        // Balance logic for new credit card: Available balance = credit limit, current balance = 0
        assertEquals(0, creditLimit.compareTo(creditCardDto.getAvailableBalance()));
        assertEquals(0, BigDecimal.ZERO.compareTo(creditCardDto.getCurrentBalance()));

        // Verify database relationship
        Account account = accountRepository.findById(creditCardDto.getId()).orElseThrow();
        assertNotNull(account.getCreditDetails(), "CreditDetails should not be null");
        assertEquals(0, creditLimit.compareTo(account.getCreditDetails().getCreditLimit()));
        assertEquals(0, BigDecimal.ZERO.compareTo(account.getCreditDetails().getStatementBalance()));
        assertNotNull(account.getCreditDetails().getApr());
    }
}
