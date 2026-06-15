package com.paybackpal.backend.borroweraction;

import com.paybackpal.backend.borrower.entity.Borrower;
import com.paybackpal.backend.borroweraction.dto.BorrowerActionResponse;
import com.paybackpal.backend.borroweraction.entity.BorrowerActionToken;
import com.paybackpal.backend.borroweraction.entity.BorrowerActionType;
import com.paybackpal.backend.borroweraction.service.BorrowerActionTokenService;
import com.paybackpal.backend.borroweraction.service.PublicBorrowerActionService;
import com.paybackpal.backend.card.entity.CreditCard;
import com.paybackpal.backend.common.exception.BusinessRuleViolationException;
import com.paybackpal.backend.notification.service.PaymentReportedNotificationService;
import com.paybackpal.backend.transaction.entity.CardTransaction;
import com.paybackpal.backend.transaction.entity.RepaymentStatus;
import com.paybackpal.backend.transaction.entity.TransactionSplit;
import com.paybackpal.backend.transaction.repository.TransactionSplitRepository;
import com.paybackpal.backend.user.entity.AppUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublicBorrowerActionServiceTest {

    @Mock
    private BorrowerActionTokenService borrowerActionTokenService;

    @Mock
    private TransactionSplitRepository transactionSplitRepository;

    @Mock
    private PaymentReportedNotificationService paymentReportedNotificationService;

    @Test
    void reportPaidShouldMarkSplitPaymentReportedUseTokenAndNotifyOwner() {
        PublicBorrowerActionService service = new PublicBorrowerActionService(
                borrowerActionTokenService,
                transactionSplitRepository,
                paymentReportedNotificationService
        );

        TransactionSplit split = createSplit();

        BorrowerActionToken token = new BorrowerActionToken(
                split,
                BorrowerActionType.REPORT_PAID,
                "hashed-token",
                OffsetDateTime.now().plusDays(7)
        );

        when(borrowerActionTokenService.getValidToken(
                "raw-token",
                BorrowerActionType.REPORT_PAID
        )).thenReturn(token);

        when(transactionSplitRepository.save(split)).thenReturn(split);

        BorrowerActionResponse response = service.reportPaid("raw-token");

        assertThat(split.getRepaymentStatus()).isEqualTo(RepaymentStatus.PAYMENT_REPORTED);
        assertThat(response.getRepaymentStatus()).isEqualTo("PAYMENT_REPORTED");
        assertThat(response.getMessage()).contains("Waiting for owner confirmation");

        verify(transactionSplitRepository).save(split);
        verify(borrowerActionTokenService).markTokenUsed(token);
        verify(paymentReportedNotificationService).enqueuePaymentReportedToOwner(split);
    }

    @Test
    void reportPaidShouldRejectAlreadyConfirmedSplit() {
        PublicBorrowerActionService service = new PublicBorrowerActionService(
                borrowerActionTokenService,
                transactionSplitRepository,
                paymentReportedNotificationService
        );

        TransactionSplit split = createSplit();
        split.markPaymentReported();
        split.markConfirmed();

        BorrowerActionToken token = new BorrowerActionToken(
                split,
                BorrowerActionType.REPORT_PAID,
                "hashed-token",
                OffsetDateTime.now().plusDays(7)
        );

        when(borrowerActionTokenService.getValidToken(
                "raw-token",
                BorrowerActionType.REPORT_PAID
        )).thenReturn(token);

        assertThatThrownBy(() -> service.reportPaid("raw-token"))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("already confirmed");

        verify(transactionSplitRepository, never()).save(any());
        verify(borrowerActionTokenService, never()).markTokenUsed(any());
        verify(paymentReportedNotificationService, never()).enqueuePaymentReportedToOwner(any());
    }

    private TransactionSplit createSplit() {
        AppUser owner = new AppUser(
                "Alice Bob",
                "alice@example.com",
                "9876543210",
                "alice@upi",
                "hashed-password"
        );

        CreditCard creditCard = new CreditCard(
                owner,
                "HDFC Millennia",
                "HDFC Bank",
                "1234",
                15,
                5
        );

        CardTransaction transaction = new CardTransaction(
                owner,
                creditCard,
                new BigDecimal("10000.00"),
                "Dinner with friends",
                "Pizza Express",
                LocalDate.of(2026, 6, 6),
                true,
                new BigDecimal("7500.00")
        );

        Borrower borrower = new Borrower(owner, "Alex", "9876500000");

        TransactionSplit split = new TransactionSplit(
                borrower,
                new BigDecimal("25.00"),
                new BigDecimal("2500.00")
        );

        transaction.addSplit(split);

        return split;
    }
}