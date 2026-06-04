package com.paybackpal.backend.card.repository;

import com.paybackpal.backend.card.entity.CreditCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CreditCardRepository extends JpaRepository<CreditCard, UUID> {

    List<CreditCard> findByUser_IdAndActiveTrueOrderByCreatedAtDesc(UUID userId);

    Optional<CreditCard> findByIdAndUser_IdAndActiveTrue(UUID cardId, UUID userId);
}