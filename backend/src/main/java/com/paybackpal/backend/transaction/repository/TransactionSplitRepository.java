package com.paybackpal.backend.transaction.repository;

import com.paybackpal.backend.transaction.entity.TransactionSplit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionSplitRepository extends JpaRepository<TransactionSplit, UUID> {

    @Query("""
            SELECT split
            FROM TransactionSplit split
            JOIN FETCH split.borrower borrower
            JOIN FETCH split.cardTransaction transaction
            WHERE split.id = :splitId
              AND transaction.user.id = :userId
              AND transaction.active = true
            """)
    Optional<TransactionSplit> findActiveSplitForUser(
            @Param("splitId") UUID splitId,
            @Param("userId") UUID userId
    );

    @Query("""
            SELECT split
            FROM TransactionSplit split
            JOIN FETCH split.borrower borrower
            JOIN FETCH split.cardTransaction transaction
            WHERE transaction.id = :transactionId
              AND transaction.user.id = :userId
              AND transaction.active = true
            ORDER BY split.createdAt ASC
            """)
    List<TransactionSplit> findSplitsForTransactionAndUser(
            @Param("transactionId") UUID transactionId,
            @Param("userId") UUID userId
    );
}