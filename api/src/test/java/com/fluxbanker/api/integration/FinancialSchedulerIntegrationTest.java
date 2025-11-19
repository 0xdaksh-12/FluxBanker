package com.fluxbanker.api.integration;

import com.fluxbanker.api.dto.AccountDto;
import com.fluxbanker.api.entity.Account;
import com.fluxbanker.api.entity.User;
import com.fluxbanker.api.repository.AccountRepository;
import com.fluxbanker.api.repository.TransactionRepository;
import com.fluxbanker.api.repository.UserRepository;
import com.fluxbanker.api.service.AccountService;
import com.fluxbanker.api.service.FinancialSchedulerService;
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
public class FinancialSchedulerIntegrationTest {

    @Autowired
    private FinancialSchedulerService schedulerService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    private UUID userId;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .firstName("Scheduler")
                .lastName("Test")
                .email("schedule_" + System.nanoTime() + "@fluxbanker.com")
                .password("secure123")
                .role(User.Role.USER)
                .build();
        user = userRepository.save(user);
        userId = user.getId();
    }

    @Test
    void testSavingsInterestAccrual() {
        AccountDto savings = accountService.provisionAccount(userId, "Savings", Account.Subtype.SAVINGS);
        
        Account account = accountRepository.findById(savings.getId()).orElseThrow();
        BigDecimal initialBalance = new BigDecimal("1000.00");
        account.setCurrentBalance(initialBalance);
        account.setAvailableBalance(initialBalance);
        accountRepository.save(account);

        long initialTxCount = transactionRepository.count();

        // Run the nightly job
        schedulerService.processNightlyFinancials();

        // Verify balance increased
        Account updatedAccount = accountRepository.findById(savings.getId()).orElseThrow();
        assertTrue(updatedAccount.getCurrentBalance().compareTo(initialBalance) > 0);

        // Verify a new transaction was recorded
        assertEquals(initialTxCount + 1, transactionRepository.count());
    }

    @Test
    void testLoanAndCreditInterestAccrual() {
        // Loan
        BigDecimal loanPrincipal = new BigDecimal("10000.00");
        AccountDto loan = accountService.applyForLoan(userId, loanPrincipal, 60);

        // Credit
        BigDecimal creditLimit = new BigDecimal("5000.00");
        AccountDto credit = accountService.openCreditCard(userId, creditLimit);
        
        // Simulate a credit card purchase to create a statement balance
        Account creditAccount = accountRepository.findById(credit.getId()).orElseThrow();
        creditAccount.setAvailableBalance(creditLimit.subtract(new BigDecimal("1000.00")));
        accountRepository.save(creditAccount);

        schedulerService.processNightlyFinancials();

        // Verify loan principal increased due to interest
        Account updatedLoan = accountRepository.findById(loan.getId()).orElseThrow();
        assertTrue(updatedLoan.getCurrentBalance().compareTo(loanPrincipal) > 0);

        // Verify credit statement balance increased due to interest
        Account updatedCredit = accountRepository.findById(credit.getId()).orElseThrow();
        assertTrue(updatedCredit.getCreditDetails().getStatementBalance().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(updatedCredit.getAvailableBalance().compareTo(creditLimit.subtract(new BigDecimal("1000.00"))) < 0);
    }

    @Test
    void testOverdraftFeeDetection() {
        AccountDto checking = accountService.provisionAccount(userId, "Checking", Account.Subtype.CHECKING);
        
        Account account = accountRepository.findById(checking.getId()).orElseThrow();
        BigDecimal negativeBalance = new BigDecimal("-10.00");
        account.setCurrentBalance(negativeBalance);
        account.setAvailableBalance(negativeBalance);
        accountRepository.save(account);

        long initialTxCount = transactionRepository.count();

        schedulerService.processNightlyFinancials();

        Account updatedAccount = accountRepository.findById(checking.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("-45.00").compareTo(updatedAccount.getCurrentBalance()));

        assertEquals(initialTxCount + 1, transactionRepository.count());
    }
}
