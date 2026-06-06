package com.paybackpal.backend.transaction.repository;

import com.paybackpal.backend.transaction.entity.CardTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CardTransactionRepository extends JpaRepository<CardTransaction, UUID> {

    List<CardTransaction> findByCreditCard_IdAndUser_IdAndActiveTrueOrderByTransactionDateDescCreatedAtDesc(
            UUID creditCardId,
            UUID userId
    );

    Optional<CardTransaction> findByIdAndUser_IdAndActiveTrue(
            UUID transactionId,
            UUID userId
    );
}