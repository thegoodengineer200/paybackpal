package com.paybackpal.backend.transaction.repository;

import com.paybackpal.backend.transaction.entity.TransactionSplit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransactionSplitRepository extends JpaRepository<TransactionSplit, UUID> {
}
