package com.fluxbanker.api.dto;

import com.fluxbanker.api.entity.Card;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CardDto {
    private UUID id;
    private UUID accountId;
    private String cardNumber; // Masked in response usually, but full if needed
    private String expiryDate;
    private String cvv;
    private Card.Status status;
    private Card.Type type;
    private Card.Subtype subtype;

    public static CardDto fromEntity(Card card) {
        // Mask card number for security, e.g., **** **** **** 1234
        String maskedCardNumber = "**** **** **** " + card.getCardNumber().substring(card.getCardNumber().length() - 4);
        
        return CardDto.builder()
                .id(card.getId())
                .accountId(card.getAccount().getId())
                .cardNumber(maskedCardNumber)
                .expiryDate(card.getExpiryDate())
                .cvv("***") // Never expose CVV over API like this usually, but mock it
                .status(card.getStatus())
                .type(card.getType())
                .subtype(card.getSubtype())
                .build();
    }
}
