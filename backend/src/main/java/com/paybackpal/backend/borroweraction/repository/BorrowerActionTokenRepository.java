package com.paybackpal.backend.borroweraction.repository;

import com.paybackpal.backend.borroweraction.entity.BorrowerActionToken;
import com.paybackpal.backend.borroweraction.entity.BorrowerActionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.UUID;

public interface BorrowerActionTokenRepository extends JpaRepository<BorrowerActionToken, UUID> {

    @Query("""
            SELECT token
            FROM BorrowerActionToken token
            JOIN FETCH token.transactionSplit split
            JOIN FETCH split.borrower borrower
            JOIN FETCH split.cardTransaction transaction
            JOIN FETCH transaction.user owner
            WHERE token.tokenHash = :tokenHash
            """)
    Optional<BorrowerActionToken> findByTokenHashWithSplitDetails(
            @Param("tokenHash") String tokenHash
    );

    Optional<BorrowerActionToken> findByTokenHash(String tokenHash);

    Optional<BorrowerActionToken> findTopByTransactionSplit_IdAndActionTypeOrderByCreatedAtDesc(
            UUID transactionSplitId, BorrowerActionType actionType
    );
}