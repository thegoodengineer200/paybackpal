package com.paybackpal.backend.transaction.service;

import com.paybackpal.backend.auth.service.CurrentUserService;
import com.paybackpal.backend.card.entity.CreditCard;
import com.paybackpal.backend.card.repository.CreditCardRepository;
import com.paybackpal.backend.common.exception.BusinessRuleViolationException;
import com.paybackpal.backend.common.exception.ResourceNotFoundException;
import com.paybackpal.backend.transaction.dto.CreateTransactionRequest;
import com.paybackpal.backend.transaction.dto.TransactionResponse;
import com.paybackpal.backend.transaction.entity.CardTransaction;
import com.paybackpal.backend.transaction.repository.CardTransactionRepository;
import com.paybackpal.backend.user.entity.AppUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionService {

    private final CardTransactionRepository cardTransactionRepository;
    private final CreditCardRepository creditCardRepository;
    private final CurrentUserService currentUserService;

    public TransactionService(
            CardTransactionRepository cardTransactionRepository,
            CreditCardRepository creditCardRepository,
            CurrentUserService currentUserService
    ) {
        this.cardTransactionRepository = cardTransactionRepository;
        this.creditCardRepository = creditCardRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public TransactionResponse createTransaction(
            UUID cardId,
            CreateTransactionRequest request
    ) {
        AppUser currentUser = currentUserService.getCurrentUser();

        CreditCard creditCard = creditCardRepository
                .findByIdAndUser_IdAndActiveTrue(cardId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));

        boolean borrowed = Boolean.TRUE.equals(request.getBorrowed());

        if (borrowed) {
            throw new BusinessRuleViolationException(
                    "Borrowed transaction logging will be implemented in the next step"
            );
        }

        BigDecimal normalizedAmount = normalizeMoney(request.getAmount());

        CardTransaction transaction = new CardTransaction(
                currentUser,
                creditCard,
                normalizedAmount,
                normalizeOptional(request.getDescription()),
                normalizeOptional(request.getMerchantName()),
                request.getTransactionDate(),
                false,
                normalizedAmount
        );

        CardTransaction savedTransaction = cardTransactionRepository.save(transaction);

        return TransactionResponse.from(savedTransaction);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsForCard(UUID cardId) {
        AppUser currentUser = currentUserService.getCurrentUser();

        CreditCard creditCard = creditCardRepository
                .findByIdAndUser_IdAndActiveTrue(cardId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));

        return cardTransactionRepository
                .findByCreditCard_IdAndUser_IdAndActiveTrueOrderByTransactionDateDescCreatedAtDesc(
                        creditCard.getId(),
                        currentUser.getId()
                )
                .stream()
                .map(TransactionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransaction(UUID transactionId) {
        AppUser currentUser = currentUserService.getCurrentUser();

        CardTransaction transaction = cardTransactionRepository
                .findByIdAndUser_IdAndActiveTrue(transactionId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        return TransactionResponse.from(transaction);
    }

    private BigDecimal normalizeMoney(BigDecimal amount) {
        return amount.setScale(2);
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}