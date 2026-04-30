package com.fluxbanker.api.controller;

import com.fluxbanker.api.dto.CardDto;
import com.fluxbanker.api.entity.Card;
import com.fluxbanker.api.security.CustomUserDetails;
import com.fluxbanker.api.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @GetMapping
    public ResponseEntity<List<CardDto>> getMyCards(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null)
            return ResponseEntity.status(401).build();
        return ResponseEntity.ok(cardService.getMyCards(userDetails.getUserId()));
    }

    @PostMapping("/issue")
    public ResponseEntity<CardDto> issueCard(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody Map<String, String> request) {
        if (userDetails == null)
            return ResponseEntity.status(401).build();

        UUID accountId = UUID.fromString(request.get("accountId"));
        Card.Type type = Card.Type.valueOf(request.getOrDefault("type", "VIRTUAL").toUpperCase());

        return ResponseEntity.ok(cardService.issueCard(userDetails.getUserId(), accountId, type));
    }

    @PostMapping("/{cardId}/freeze")
    public ResponseEntity<CardDto> freezeCard(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID cardId) {
        if (userDetails == null)
            return ResponseEntity.status(401).build();
        return ResponseEntity.ok(cardService.freezeCard(userDetails.getUserId(), cardId));
    }

    @PostMapping("/{cardId}/unfreeze")
    public ResponseEntity<CardDto> unfreezeCard(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID cardId) {
        if (userDetails == null)
            return ResponseEntity.status(401).build();
        return ResponseEntity.ok(cardService.unfreezeCard(userDetails.getUserId(), cardId));
    }

    @PostMapping("/{cardId}/pin")
    public ResponseEntity<Void> setPin(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID cardId,
            @RequestBody Map<String, String> request) {
        if (userDetails == null)
            return ResponseEntity.status(401).build();

        String pin = request.get("pin");
        cardService.setPin(userDetails.getUserId(), cardId, pin);
        return ResponseEntity.ok().build();
    }
}
