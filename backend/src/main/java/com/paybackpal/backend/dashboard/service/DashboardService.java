package com.paybackpal.backend.dashboard.service;

import com.paybackpal.backend.auth.service.CurrentUserService;
import com.paybackpal.backend.borrower.entity.Borrower;
import com.paybackpal.backend.borrower.repository.BorrowerRepository;
import com.paybackpal.backend.card.entity.CreditCard;
import com.paybackpal.backend.card.repository.CreditCardRepository;
import com.paybackpal.backend.common.exception.ResourceNotFoundException;
import com.paybackpal.backend.dashboard.dto.BorrowerSummaryResponse;
import com.paybackpal.backend.dashboard.dto.CardSummaryResponse;
import com.paybackpal.backend.dashboard.dto.DashboardSummaryResponse;
import com.paybackpal.backend.dashboard.repository.DashboardRepository;
import com.paybackpal.backend.dashboard.repository.SplitStatusAggregateProjection;
import com.paybackpal.backend.dashboard.repository.TransactionAggregateProjection;
import com.paybackpal.backend.transaction.entity.RepaymentStatus;
import com.paybackpal.backend.user.entity.AppUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class DashboardService {
    private final DashboardRepository dashboardRepository;
    private final CreditCardRepository creditCardRepository;
    private final BorrowerRepository borrowerRepository;
    private final CurrentUserService currentUserService;

    public DashboardService(DashboardRepository dashboardRepository,CreditCardRepository creditCardRepository, BorrowerRepository borrowerRepository, CurrentUserService currentUserService) {
        this.dashboardRepository = dashboardRepository;
        this.creditCardRepository = creditCardRepository;
        this.borrowerRepository = borrowerRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary() {
        AppUser currentUser = currentUserService.getCurrentUser();
        TransactionAggregateProjection transactionAggregate = dashboardRepository.getTransactionAggregateForUser(currentUser.getId());
        SplitStatusAggregateProjection splitAggregate = dashboardRepository.getSplitAggregateForUser(
                        currentUser.getId(),
                        RepaymentStatus.PENDING,
                        RepaymentStatus.PAYMENT_REPORTED,
                        RepaymentStatus.CONFIRMED,
                        RepaymentStatus.CANCELLED
                );
        return new DashboardSummaryResponse(
                money(transactionAggregate.getTotalCardSpendAmount()),
                money(transactionAggregate.getOwnerExpenseAmount()),
                money(transactionAggregate.getPersonalTransactionAmount()),
                money(transactionAggregate.getBorrowedTransactionAmount()),
                money(splitAggregate.getTotalBorrowerShareAmount()),
                money(splitAggregate.getPendingAmount()),
                money(splitAggregate.getPaymentReportedAmount()),
                money(splitAggregate.getConfirmedAmount()),
                money(splitAggregate.getCancelledAmount()),
                count(transactionAggregate.getTransactionCount()),
                count(transactionAggregate.getPersonalTransactionCount()),
                count(transactionAggregate.getBorrowedTransactionCount()),
                count(splitAggregate.getSplitCount())
        );
    }

    @Transactional(readOnly = true)
    public CardSummaryResponse getCardSummary(UUID cardId) {
        AppUser currentUser = currentUserService.getCurrentUser();
        CreditCard card = creditCardRepository
                .findByIdAndUser_IdAndActiveTrue(cardId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));
        TransactionAggregateProjection transactionAggregate =
                dashboardRepository.getTransactionAggregateForCard(
                        currentUser.getId(),
                        card.getId()
                );
        SplitStatusAggregateProjection splitAggregate =
                dashboardRepository.getSplitAggregateForCard(
                        currentUser.getId(),
                        card.getId(),
                        RepaymentStatus.PENDING,
                        RepaymentStatus.PAYMENT_REPORTED,
                        RepaymentStatus.CONFIRMED,
                        RepaymentStatus.CANCELLED
                );

        return new CardSummaryResponse(
                card.getId(),
                card.getCardName(),
                card.getBankName(),
                card.getLastFourDigits(),
                money(transactionAggregate.getTotalCardSpendAmount()),
                money(transactionAggregate.getOwnerExpenseAmount()),
                money(transactionAggregate.getPersonalTransactionAmount()),
                money(transactionAggregate.getBorrowedTransactionAmount()),
                money(splitAggregate.getTotalBorrowerShareAmount()),
                money(splitAggregate.getPendingAmount()),
                money(splitAggregate.getPaymentReportedAmount()),
                money(splitAggregate.getConfirmedAmount()),
                money(splitAggregate.getCancelledAmount()),
                count(transactionAggregate.getTransactionCount()),
                count(transactionAggregate.getPersonalTransactionCount()),
                count(transactionAggregate.getBorrowedTransactionCount()),
                count(splitAggregate.getSplitCount())
        );
    }

    @Transactional(readOnly = true)
    public BorrowerSummaryResponse getBorrowerSummary(UUID borrowerId) {
        AppUser currentUser = currentUserService.getCurrentUser();
        Borrower borrower = borrowerRepository
                .findByIdAndOwnerUser_IdAndActiveTrue(borrowerId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Borrower not found"));
        SplitStatusAggregateProjection splitAggregate =
                dashboardRepository.getSplitAggregateForBorrower(
                        currentUser.getId(),
                        borrower.getId(),
                        RepaymentStatus.PENDING,
                        RepaymentStatus.PAYMENT_REPORTED,
                        RepaymentStatus.CONFIRMED,
                        RepaymentStatus.CANCELLED
                );

        return new BorrowerSummaryResponse(
                borrower.getId(),
                borrower.getName(),
                borrower.getPhoneNumber(),
                money(splitAggregate.getTotalBorrowerShareAmount()),
                money(splitAggregate.getPendingAmount()),
                money(splitAggregate.getPaymentReportedAmount()),
                money(splitAggregate.getConfirmedAmount()),
                money(splitAggregate.getCancelledAmount()),
                count(splitAggregate.getSplitCount())
        );
    }

    private BigDecimal money(BigDecimal value) {
        if (value == null) return BigDecimal.ZERO.setScale(2);
        return value.setScale(2);
    }

    private long count(Long value) {
        if (value == null) return 0L;
        return value;
    }
}