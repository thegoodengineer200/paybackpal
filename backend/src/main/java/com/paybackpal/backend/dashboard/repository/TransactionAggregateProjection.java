package com.paybackpal.backend.dashboard.repository;

import java.math.BigDecimal;

public interface TransactionAggregateProjection {
    BigDecimal getTotalCardSpendAmount();
    BigDecimal getOwnerExpenseAmount();
    BigDecimal getPersonalTransactionAmount();
    BigDecimal getBorrowedTransactionAmount();
    Long getTransactionCount();
    Long getPersonalTransactionCount();
    Long getBorrowedTransactionCount();
}
