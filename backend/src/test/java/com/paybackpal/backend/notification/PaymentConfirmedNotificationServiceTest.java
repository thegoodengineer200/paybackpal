package com.paybackpal.backend.notification;

import com.paybackpal.backend.borrower.entity.Borrower;
import com.paybackpal.backend.card.entity.CreditCard;
import com.paybackpal.backend.notification.entity.NotificationType;
import com.paybackpal.backend.notification.service.NotificationOutboxService;
import com.paybackpal.backend.notification.service.PaymentConfirmedNotificationService;
import com.paybackpal.backend.notification.template.WhatsAppMessageTemplateBuilder;
import com.paybackpal.backend.transaction.entity.CardTransaction;
import com.paybackpal.backend.transaction.entity.TransactionSplit;
import com.paybackpal.backend.user.entity.AppUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentConfirmedNotificationServiceTest {

    @Mock
    private NotificationOutboxService notificationOutboxService;

    @Mock
    private WhatsAppMessageTemplateBuilder whatsAppMessageTemplateBuilder;

    @Test
    void enqueuePaymentConfirmedToBorrowerShouldQueueWhatsAppToBorrowerPhoneNumber() {
        PaymentConfirmedNotificationService service = new PaymentConfirmedNotificationService(
                notificationOutboxService,
                whatsAppMessageTemplateBuilder
        );

        TransactionSplit split = createSplit();

        when(whatsAppMessageTemplateBuilder.buildPaymentConfirmedToBorrower(split))
                .thenReturn("Payment confirmed message");

        service.enqueuePaymentConfirmedToBorrower(split);

        verify(notificationOutboxService).enqueueWhatsApp(
                eq(split),
                eq(NotificationType.PAYMENT_CONFIRMED_TO_BORROWER),
                eq("9876500000"),
                eq("Payment confirmed message"),
                any(OffsetDateTime.class)
        );
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