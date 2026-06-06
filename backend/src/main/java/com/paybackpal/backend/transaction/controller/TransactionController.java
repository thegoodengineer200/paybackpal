package com.paybackpal.backend.transaction.controller;

import com.paybackpal.backend.transaction.dto.CreateTransactionRequest;
import com.paybackpal.backend.transaction.dto.TransactionResponse;
import com.paybackpal.backend.transaction.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/cards/{cardId}/transactions")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse createTransaction(
            @PathVariable UUID cardId,
            @Valid @RequestBody CreateTransactionRequest request
    ) {
        return transactionService.createTransaction(cardId, request);
    }

    @GetMapping("/cards/{cardId}/transactions")
    public List<TransactionResponse> getTransactionsForCard(
            @PathVariable UUID cardId
    ) {
        return transactionService.getTransactionsForCard(cardId);
    }

    @GetMapping("/transactions/{transactionId}")
    public TransactionResponse getTransaction(
            @PathVariable UUID transactionId
    ) {
        return transactionService.getTransaction(transactionId);
    }
}