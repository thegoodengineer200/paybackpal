package com.paybackpal.backend.dashboard.repository;

import com.paybackpal.backend.transaction.entity.CardTransaction;
import com.paybackpal.backend.transaction.entity.RepaymentStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface DashboardRepository extends Repository<CardTransaction, UUID> {

    @Query("""
            SELECT
                SUM(ct.amount) AS totalCardSpendAmount,
                SUM(ct.ownerShareAmount) AS ownerExpenseAmount,
                SUM(CASE WHEN ct.borrowed = false THEN ct.amount ELSE 0 END) AS personalTransactionAmount,
                SUM(CASE WHEN ct.borrowed = true THEN ct.amount ELSE 0 END) AS borrowedTransactionAmount,
                COUNT(ct) AS transactionCount,
                SUM(CASE WHEN ct.borrowed = false THEN 1 ELSE 0 END) AS personalTransactionCount,
                SUM(CASE WHEN ct.borrowed = true THEN 1 ELSE 0 END) AS borrowedTransactionCount
            FROM CardTransaction ct
            WHERE ct.user.id = :userId
              AND ct.active = true
            """)
    TransactionAggregateProjection getTransactionAggregateForUser(
            @Param("userId") UUID userId
    );

    @Query("""
            SELECT
                SUM(split.splitAmount) AS totalBorrowerShareAmount,
                SUM(CASE WHEN split.repaymentStatus = :pending THEN split.splitAmount ELSE 0 END) AS pendingAmount,
                SUM(CASE WHEN split.repaymentStatus = :paymentReported THEN split.splitAmount ELSE 0 END) AS paymentReportedAmount,
                SUM(CASE WHEN split.repaymentStatus = :confirmed THEN split.splitAmount ELSE 0 END) AS confirmedAmount,
                SUM(CASE WHEN split.repaymentStatus = :cancelled THEN split.splitAmount ELSE 0 END) AS cancelledAmount,
                COUNT(split) AS splitCount
            FROM TransactionSplit split
            JOIN split.cardTransaction ct
            WHERE ct.user.id = :userId
              AND ct.active = true
            """)
    SplitStatusAggregateProjection getSplitAggregateForUser(
            @Param("userId") UUID userId,
            @Param("pending") RepaymentStatus pending,
            @Param("paymentReported") RepaymentStatus paymentReported,
            @Param("confirmed") RepaymentStatus confirmed,
            @Param("cancelled") RepaymentStatus cancelled
    );

    @Query("""
            SELECT
                SUM(ct.amount) AS totalCardSpendAmount,
                SUM(ct.ownerShareAmount) AS ownerExpenseAmount,
                SUM(CASE WHEN ct.borrowed = false THEN ct.amount ELSE 0 END) AS personalTransactionAmount,
                SUM(CASE WHEN ct.borrowed = true THEN ct.amount ELSE 0 END) AS borrowedTransactionAmount,
                COUNT(ct) AS transactionCount,
                SUM(CASE WHEN ct.borrowed = false THEN 1 ELSE 0 END) AS personalTransactionCount,
                SUM(CASE WHEN ct.borrowed = true THEN 1 ELSE 0 END) AS borrowedTransactionCount
            FROM CardTransaction ct
            WHERE ct.user.id = :userId
              AND ct.creditCard.id = :cardId
              AND ct.active = true
            """)
    TransactionAggregateProjection getTransactionAggregateForCard(
            @Param("userId") UUID userId,
            @Param("cardId") UUID cardId
    );

    @Query("""
            SELECT
                SUM(split.splitAmount) AS totalBorrowerShareAmount,
                SUM(CASE WHEN split.repaymentStatus = :pending THEN split.splitAmount ELSE 0 END) AS pendingAmount,
                SUM(CASE WHEN split.repaymentStatus = :paymentReported THEN split.splitAmount ELSE 0 END) AS paymentReportedAmount,
                SUM(CASE WHEN split.repaymentStatus = :confirmed THEN split.splitAmount ELSE 0 END) AS confirmedAmount,
                SUM(CASE WHEN split.repaymentStatus = :cancelled THEN split.splitAmount ELSE 0 END) AS cancelledAmount,
                COUNT(split) AS splitCount
            FROM TransactionSplit split
            JOIN split.cardTransaction ct
            WHERE ct.user.id = :userId
              AND ct.creditCard.id = :cardId
              AND ct.active = true
            """)
    SplitStatusAggregateProjection getSplitAggregateForCard(
            @Param("userId") UUID userId,
            @Param("cardId") UUID cardId,
            @Param("pending") RepaymentStatus pending,
            @Param("paymentReported") RepaymentStatus paymentReported,
            @Param("confirmed") RepaymentStatus confirmed,
            @Param("cancelled") RepaymentStatus cancelled
    );

    @Query("""
            SELECT
                SUM(split.splitAmount) AS totalBorrowerShareAmount,
                SUM(CASE WHEN split.repaymentStatus = :pending THEN split.splitAmount ELSE 0 END) AS pendingAmount,
                SUM(CASE WHEN split.repaymentStatus = :paymentReported THEN split.splitAmount ELSE 0 END) AS paymentReportedAmount,
                SUM(CASE WHEN split.repaymentStatus = :confirmed THEN split.splitAmount ELSE 0 END) AS confirmedAmount,
                SUM(CASE WHEN split.repaymentStatus = :cancelled THEN split.splitAmount ELSE 0 END) AS cancelledAmount,
                COUNT(split) AS splitCount
            FROM TransactionSplit split
            JOIN split.cardTransaction ct
            WHERE ct.user.id = :userId
              AND split.borrower.id = :borrowerId
              AND ct.active = true
            """)
    SplitStatusAggregateProjection getSplitAggregateForBorrower(
            @Param("userId") UUID userId,
            @Param("borrowerId") UUID borrowerId,
            @Param("pending") RepaymentStatus pending,
            @Param("paymentReported") RepaymentStatus paymentReported,
            @Param("confirmed") RepaymentStatus confirmed,
            @Param("cancelled") RepaymentStatus cancelled
    );
}