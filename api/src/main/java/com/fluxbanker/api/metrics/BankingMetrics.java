package com.fluxbanker.api.metrics;

import com.fluxbanker.api.repository.AccountRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class BankingMetrics {
    private final MeterRegistry registry;

    public BankingMetrics(MeterRegistry registry, AccountRepository accountRepository) {
        this.registry = registry;
        Gauge.builder("banking.accounts.active", accountRepository, repo -> {
            try {
                return repo.count();
            } catch (Exception e) {
                return 0.0;
            }
        }).description("Total number of provisioned bank accounts").register(registry);
    }

    public void incrementTransfer(String txType, String status) {
        Counter.builder("banking.transfers.total")
                .description("Total number of banking transactions processed")
                .tag("type", txType)
                .tag("status", status)
                .register(this.registry)
                .increment();
    }
}
