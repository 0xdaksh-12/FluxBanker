package com.fluxbanker.api.service;

import com.fluxbanker.api.entity.Account;
import com.fluxbanker.api.entity.CreditDetails;
import com.fluxbanker.api.entity.LoanDetails;
import com.fluxbanker.api.entity.Transaction;
import com.fluxbanker.api.repository.AccountRepository;
import com.fluxbanker.api.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinancialSchedulerService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    // Run every day at midnight
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void processNightlyFinancials() {
        log.info("Starting nightly financial processing...");
        
        List<Account> allAccounts = accountRepository.findAll();
        
        for (Account account : allAccounts) {
            processSavingsInterest(account);
            processLoanInterest(account);
            processCreditCardInterest(account);
            processOverdrafts(account);
        }
        
        log.info("Finished nightly financial processing.");
    }

    private void processSavingsInterest(Account account) {
        if (account.getSubtype() == Account.Subtype.SAVINGS && account.getCurrentBalance().compareTo(BigDecimal.ZERO) > 0) {
            // Mock 2.5% APY
            BigDecimal apy = BigDecimal.valueOf(0.025);
            BigDecimal dailyRate = apy.divide(BigDecimal.valueOf(365), 10, RoundingMode.HALF_UP);
            BigDecimal interest = account.getCurrentBalance().multiply(dailyRate).setScale(4, RoundingMode.HALF_UP);
            
            if (interest.compareTo(BigDecimal.ZERO) > 0) {
                account.setCurrentBalance(account.getCurrentBalance().add(interest));
                account.setAvailableBalance(account.getAvailableBalance().add(interest));
                accountRepository.save(account);

                Transaction tx = Transaction.builder()
                        .account(account)
                        .amount(interest)
                        .type(Transaction.Type.DEPOSIT)
                        .status(Transaction.Status.COMPLETED)
                        .category("Interest Accrual")
                        .counterpartyName("FluxBanker")
                        .build();
                transactionRepository.save(tx);
            }
        }
    }

    private void processLoanInterest(Account account) {
        if (account.getType() == Account.Type.LOAN && account.getLoanDetails() != null) {
            LoanDetails loan = account.getLoanDetails();
            BigDecimal balance = account.getCurrentBalance(); // Outstanding principal
            
            if (balance.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal dailyRate = loan.getInterestRate().divide(BigDecimal.valueOf(365), 10, RoundingMode.HALF_UP);
                BigDecimal interest = balance.multiply(dailyRate).setScale(4, RoundingMode.HALF_UP);
                
                // Increase the outstanding balance by the interest amount
                account.setCurrentBalance(balance.add(interest));
                accountRepository.save(account);
                
                // In a real system, we'd log this as a specific interest transaction or accumulate it for the statement
            }
        }
    }

    private void processCreditCardInterest(Account account) {
        if (account.getType() == Account.Type.CREDIT && account.getCreditDetails() != null) {
            CreditDetails credit = account.getCreditDetails();
            BigDecimal outstandingBalance = credit.getCreditLimit().subtract(account.getAvailableBalance());
            
            if (outstandingBalance.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal dailyRate = credit.getApr().divide(BigDecimal.valueOf(365), 10, RoundingMode.HALF_UP);
                BigDecimal interest = outstandingBalance.multiply(dailyRate).setScale(4, RoundingMode.HALF_UP);
                
                // Decrease available balance to reflect interest charge
                account.setAvailableBalance(account.getAvailableBalance().subtract(interest));
                accountRepository.save(account);
                
                // Accumulate on the statement balance
                credit.setStatementBalance(credit.getStatementBalance().add(interest));
                // We'd also need to save the credit details, but it's cascaded from account.
            }
        }
    }

    private void processOverdrafts(Account account) {
        if (account.getSubtype() == Account.Subtype.CHECKING && account.getCurrentBalance().compareTo(BigDecimal.ZERO) < 0) {
            // Apply a $35 overdraft fee if negative
            BigDecimal fee = BigDecimal.valueOf(35.00);
            
            account.setCurrentBalance(account.getCurrentBalance().subtract(fee));
            account.setAvailableBalance(account.getAvailableBalance().subtract(fee));
            accountRepository.save(account);

            Transaction tx = Transaction.builder()
                    .account(account)
                    .amount(fee.negate())
                    .type(Transaction.Type.WITHDRAWAL)
                    .status(Transaction.Status.COMPLETED)
                    .category("Overdraft Fee")
                    .counterpartyName("FluxBanker")
                    .build();
            transactionRepository.save(tx);
        }
    }
}
