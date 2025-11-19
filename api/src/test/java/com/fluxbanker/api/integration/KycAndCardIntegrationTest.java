package com.fluxbanker.api.integration;

import com.fluxbanker.api.dto.AccountDto;
import com.fluxbanker.api.dto.CardDto;
import com.fluxbanker.api.entity.Account;
import com.fluxbanker.api.entity.Card;
import com.fluxbanker.api.entity.User;
import com.fluxbanker.api.repository.CardRepository;
import com.fluxbanker.api.repository.UserRepository;
import com.fluxbanker.api.service.AccountService;
import com.fluxbanker.api.service.CardService;
import com.fluxbanker.api.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class KycAndCardIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private CardService cardService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UUID userId;
    private UUID accountId;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .firstName("Card")
                .lastName("Holder")
                .email("card_" + System.nanoTime() + "@fluxbanker.com")
                .password("secure123")
                .role(User.Role.USER)
                .build();
        user = userRepository.save(user);
        userId = user.getId();

        AccountDto account = accountService.provisionAccount(userId, "Checking", Account.Subtype.CHECKING);
        accountId = account.getId();
    }

    @Test
    void testUpdateKycStatus() {
        User user = userService.getUserById(userId);
        assertEquals(User.KycStatus.PENDING, user.getKycStatus());

        User updatedUser = userService.updateKycStatus(userId, User.KycStatus.APPROVED);
        assertEquals(User.KycStatus.APPROVED, updatedUser.getKycStatus());
    }

    @Test
    void testIssueCard() {
        CardDto cardDto = cardService.issueCard(userId, accountId, Card.Type.PHYSICAL);

        assertNotNull(cardDto.getId());
        assertEquals(Card.Status.ACTIVE, cardDto.getStatus());
        assertEquals(Card.Type.PHYSICAL, cardDto.getType());
        assertEquals(Card.Subtype.DEBIT, cardDto.getSubtype()); // Derived from checking account

        // Verify PAN masking
        assertTrue(cardDto.getCardNumber().startsWith("**** **** **** "));

        Card card = cardRepository.findById(cardDto.getId()).orElseThrow();
        assertEquals(16, card.getCardNumber().length());
        assertEquals(3, card.getCvv().length());
        assertNotNull(card.getExpiryDate());
    }

    @Test
    void testFreezeAndUnfreezeCard() {
        CardDto cardDto = cardService.issueCard(userId, accountId, Card.Type.VIRTUAL);
        assertEquals(Card.Status.ACTIVE, cardDto.getStatus());

        CardDto frozenCard = cardService.freezeCard(userId, cardDto.getId());
        assertEquals(Card.Status.FROZEN, frozenCard.getStatus());

        CardDto unfrozenCard = cardService.unfreezeCard(userId, cardDto.getId());
        assertEquals(Card.Status.ACTIVE, unfrozenCard.getStatus());
    }

    @Test
    void testSetPin() {
        CardDto cardDto = cardService.issueCard(userId, accountId, Card.Type.PHYSICAL);
        
        String plaintextPin = "1234";
        cardService.setPin(userId, cardDto.getId(), plaintextPin);

        Card card = cardRepository.findById(cardDto.getId()).orElseThrow();
        assertNotNull(card.getPinHash());
        assertNotEquals(plaintextPin, card.getPinHash()); // Must be hashed
        assertTrue(passwordEncoder.matches(plaintextPin, card.getPinHash())); // Hash must match BCrypt
    }
}
