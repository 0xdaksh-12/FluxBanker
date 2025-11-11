package com.fluxbanker.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "user")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(name = "account_number", nullable = false, unique = true, length = 50)
    private String accountNumber;

    @Column(nullable = false, length = 4)
    private String mask;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal currentBalance;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal availableBalance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Subtype subtype;

    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private CreditDetails creditDetails;

    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private LoanDetails loanDetails;

    public enum Type {
        DEPOSITORY, CREDIT, LOAN, INVESTMENT
    }

    public enum Subtype {
        CHECKING, SAVINGS, CREDIT_CARD, MONEY_MARKET
    }
}
