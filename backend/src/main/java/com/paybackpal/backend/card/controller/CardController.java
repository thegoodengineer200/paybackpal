package com.paybackpal.backend.card.controller;

import com.paybackpal.backend.card.dto.CardResponse;
import com.paybackpal.backend.card.dto.CreateCardRequest;
import com.paybackpal.backend.card.dto.UpdateCardRequest;
import com.paybackpal.backend.card.service.CardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CardResponse createCard(@Valid @RequestBody CreateCardRequest request) {
        return cardService.createCard(request);
    }

    @GetMapping
    public List<CardResponse> getCurrentUserCards() {
        return cardService.getCurrentUserCards();
    }

    @GetMapping("/{cardId}")
    public CardResponse getCard(@PathVariable UUID cardId) {
        return cardService.getCard(cardId);
    }

    @PutMapping("/{cardId}")
    public CardResponse updateCard(
            @PathVariable UUID cardId,
            @Valid @RequestBody UpdateCardRequest request
    ) {
        return cardService.updateCard(cardId, request);
    }

    @DeleteMapping("/{cardId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCard(@PathVariable UUID cardId) {
        cardService.deleteCard(cardId);
    }
}