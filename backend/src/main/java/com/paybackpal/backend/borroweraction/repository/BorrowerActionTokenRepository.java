package com.paybackpal.backend.borroweraction.repository;

import com.paybackpal.backend.borroweraction.entity.BorrowerActionToken;
import com.paybackpal.backend.borroweraction.entity.BorrowerActionType;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.UUID;

public interface BorrowerActionTokenRepository extends JpaRepository<BorrowerActionToken, UUID> {
    Optional<BorrowerActionToken> findByTokenHash(String tokenHash);

    Optional<BorrowerActionToken> findTopByTransactionSplit_IdAndActionTypeOrderByCreatedAtDesc(
            UUID transactionSplitId, BorrowerActionType actionType
    );
}
