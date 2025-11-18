package com.fluxbanker.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "credit_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    private Account account;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal creditLimit;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal apr; // e.g., 0.1999 for 19.99%

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal statementBalance;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal minimumPaymentDue;

    @Column(nullable = false)
    private LocalDate nextPaymentDueDate;
}
