package com.fluxbanker.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true, length = 19)
    private String cardNumber; // Simulating 16 digits

    @Column(nullable = false, length = 5)
    private String expiryDate; // MM/YY

    @Column(nullable = false, length = 4)
    private String cvv;

    @Column(length = 64)
    private String pinHash; // Hashed PIN

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Subtype subtype;

    public enum Status {
        ACTIVE, FROZEN, CANCELED
    }

    public enum Type {
        PHYSICAL, VIRTUAL
    }

    public enum Subtype {
        DEBIT, CREDIT
    }
}
