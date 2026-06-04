package com.paybackpal.backend.card.service;

import com.paybackpal.backend.auth.service.CurrentUserService;
import com.paybackpal.backend.card.dto.CardResponse;
import com.paybackpal.backend.card.dto.CreateCardRequest;
import com.paybackpal.backend.card.dto.UpdateCardRequest;
import com.paybackpal.backend.card.entity.CreditCard;
import com.paybackpal.backend.card.repository.CreditCardRepository;
import com.paybackpal.backend.common.exception.ResourceNotFoundException;
import com.paybackpal.backend.user.entity.AppUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CardService {

    private final CreditCardRepository creditCardRepository;
    private final CurrentUserService currentUserService;

    public CardService(
            CreditCardRepository creditCardRepository,
            CurrentUserService currentUserService
    ) {
        this.creditCardRepository = creditCardRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public CardResponse createCard(CreateCardRequest request) {
        AppUser currentUser = currentUserService.getCurrentUser();

        CreditCard card = new CreditCard(
                currentUser,
                normalizeRequired(request.getCardName()),
                normalizeRequired(request.getBankName()),
                request.getLastFourDigits().trim(),
                request.getBillingCycleDay(),
                request.getDueDay()
        );
        CreditCard savedCard = creditCardRepository.save(card);

        return CardResponse.from(savedCard);
    }

    @Transactional(readOnly = true)
    public List<CardResponse> getCurrentUserCards() {
        AppUser currentUser = currentUserService.getCurrentUser();

        return creditCardRepository
                .findByUser_IdAndActiveTrueOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(CardResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CardResponse getCard(UUID cardId) {
        AppUser currentUser = currentUserService.getCurrentUser();
        CreditCard card = getActiveCardForUser(cardId, currentUser.getId());

        return CardResponse.from(card);
    }

    @Transactional
    public CardResponse updateCard(UUID cardId, UpdateCardRequest request) {
        AppUser currentUser = currentUserService.getCurrentUser();
        CreditCard card = getActiveCardForUser(cardId, currentUser.getId());
        card.updateDetails(
                normalizeRequired(request.getCardName()),
                normalizeRequired(request.getBankName()),
                request.getLastFourDigits().trim(),
                request.getBillingCycleDay(),
                request.getDueDay()
        );

        CreditCard savedCard = creditCardRepository.save(card);
        return CardResponse.from(savedCard);
    }

    @Transactional
    public void deleteCard(UUID cardId) {
        AppUser currentUser = currentUserService.getCurrentUser();
        CreditCard card = getActiveCardForUser(cardId, currentUser.getId());
        card.deactivate();
        creditCardRepository.save(card);
    }

    private CreditCard getActiveCardForUser(UUID cardId, UUID userId) {
        return creditCardRepository
                .findByIdAndUser_IdAndActiveTrue(cardId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));
    }

    private String normalizeRequired(String value) {
        return value.trim();
    }
}