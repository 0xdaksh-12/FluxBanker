package com.fluxbanker.api.service;

import com.fluxbanker.api.dto.CardDto;
import com.fluxbanker.api.entity.Account;
import com.fluxbanker.api.entity.Card;
import com.fluxbanker.api.entity.User;
import com.fluxbanker.api.repository.AccountRepository;
import com.fluxbanker.api.repository.CardRepository;
import com.fluxbanker.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<CardDto> getMyCards(UUID userId) {
        return cardRepository.findByUserId(userId).stream()
                .map(CardDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public CardDto issueCard(UUID userId, UUID accountId, Card.Type type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (!account.getUser().getId().equals(userId)) {
            throw new SecurityException("Not authorized to issue card for this account");
        }

        SecureRandom random = new SecureRandom();
        long pan = 4000000000000000L + (long) (random.nextDouble() * 999999999999999L);
        String cardNumber = String.valueOf(pan);
        
        LocalDate expiry = LocalDate.now().plusYears(4);
        String expiryDate = String.format("%02d/%02d", expiry.getMonthValue(), expiry.getYear() % 100);
        
        int cvvVal = 100 + random.nextInt(900);
        String cvv = String.valueOf(cvvVal);

        Card.Subtype subtype = account.getType() == Account.Type.CREDIT ? Card.Subtype.CREDIT : Card.Subtype.DEBIT;

        Card card = Card.builder()
                .user(user)
                .account(account)
                .cardNumber(cardNumber)
                .expiryDate(expiryDate)
                .cvv(cvv)
                .status(Card.Status.ACTIVE)
                .type(type)
                .subtype(subtype)
                .build();

        Card savedCard = cardRepository.save(card);
        return CardDto.fromEntity(savedCard);
    }

    @Transactional
    public CardDto freezeCard(UUID userId, UUID cardId) {
        Card card = getCardForUser(userId, cardId);
        card.setStatus(Card.Status.FROZEN);
        return CardDto.fromEntity(cardRepository.save(card));
    }

    @Transactional
    public CardDto unfreezeCard(UUID userId, UUID cardId) {
        Card card = getCardForUser(userId, cardId);
        card.setStatus(Card.Status.ACTIVE);
        return CardDto.fromEntity(cardRepository.save(card));
    }

    @Transactional
    public void setPin(UUID userId, UUID cardId, String pin) {
        Card card = getCardForUser(userId, cardId);
        if (pin == null || pin.length() != 4 || !pin.matches("\\d{4}")) {
            throw new IllegalArgumentException("PIN must be 4 digits");
        }
        card.setPinHash(passwordEncoder.encode(pin));
        cardRepository.save(card);
    }

    private Card getCardForUser(UUID userId, UUID cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found"));
        if (!card.getUser().getId().equals(userId)) {
            throw new SecurityException("Not authorized to access this card");
        }
        return card;
    }
}
