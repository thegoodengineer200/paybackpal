package com.paybackpal.backend.dashboard.repository;

import java.math.BigDecimal;

public interface SplitStatusAggregateProjection {
    BigDecimal getTotalBorrowerShareAmount();
    BigDecimal getPendingAmount();
    BigDecimal getPaymentReportedAmount();
    BigDecimal getConfirmedAmount();
    BigDecimal getCancelledAmount();
    Long getSplitCount();
}
