package com.paybackpal.backend.notification;

import com.paybackpal.backend.borrower.entity.Borrower;
import com.paybackpal.backend.borroweraction.dto.BorrowerActionLinks;
import com.paybackpal.backend.borroweraction.service.BorrowerActionLinkBuilder;
import com.paybackpal.backend.card.entity.CreditCard;
import com.paybackpal.backend.notification.entity.NotificationType;
import com.paybackpal.backend.notification.service.InitialBorrowerReminderService;
import com.paybackpal.backend.notification.service.NotificationOutboxService;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InitialBorrowerReminderServiceTest {

    @Mock
    private NotificationOutboxService notificationOutboxService;

    @Mock
    private WhatsAppMessageTemplateBuilder whatsAppMessageTemplateBuilder;

    @Mock
    private BorrowerActionLinkBuilder borrowerActionLinkBuilder;

    @Test
    void borrowedTransactionShouldQueueInitialReminderForEachSplit() {
        InitialBorrowerReminderService service =
                new InitialBorrowerReminderService(
                        notificationOutboxService,
                        whatsAppMessageTemplateBuilder,
                        borrowerActionLinkBuilder
                );

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
                new BigDecimal("5000.00")
        );

        Borrower alex = new Borrower(owner, "Alex", "9876500000");
        Borrower marie = new Borrower(owner, "Marie", "9876500001");

        TransactionSplit alexSplit = new TransactionSplit(
                alex,
                new BigDecimal("25.00"),
                new BigDecimal("2500.00")
        );

        TransactionSplit marieSplit = new TransactionSplit(
                marie,
                new BigDecimal("25.00"),
                new BigDecimal("2500.00")
        );

        transaction.addSplit(alexSplit);
        transaction.addSplit(marieSplit);
        BorrowerActionLinks alexLinks = new BorrowerActionLinks(
                "https://paybackpal.com/alex-paid",
                "https://paybackpal.com/alex-later"
        );
        BorrowerActionLinks marieLinks = new BorrowerActionLinks(
                "https://paybackpal.com/marie-paid",
                "https://paybackpal.com/marie-later"
        );

        when(borrowerActionLinkBuilder.buildLinks(alexSplit)).thenReturn(alexLinks);
        when(borrowerActionLinkBuilder.buildLinks(marieSplit)).thenReturn(marieLinks);

        when(whatsAppMessageTemplateBuilder.buildInitialPaymentRequest(alexSplit, alexLinks))
                .thenReturn("Message for Alex with Links");

        when(whatsAppMessageTemplateBuilder.buildInitialPaymentRequest(marieSplit, marieLinks))
                .thenReturn("Message for Marie with Links");

        service.enqueueInitialReminders(transaction);

        verify(notificationOutboxService).enqueueWhatsApp(
                eq(alexSplit),
                eq(NotificationType.INITIAL_PAYMENT_REQUEST),
                eq("9876500000"),
                eq("Message for Alex with Links"),
                any(OffsetDateTime.class)
        );

        verify(notificationOutboxService).enqueueWhatsApp(
                eq(marieSplit),
                eq(NotificationType.INITIAL_PAYMENT_REQUEST),
                eq("9876500001"),
                eq("Message for Marie with Links"),
                any(OffsetDateTime.class)
        );
    }

    @Test
    void personalTransactionShouldNotQueueReminder() {
        InitialBorrowerReminderService service =
                new InitialBorrowerReminderService(
                        notificationOutboxService,
                        whatsAppMessageTemplateBuilder,
                        borrowerActionLinkBuilder
                );

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
                new BigDecimal("1000.00"),
                "Personal coffee",
                "Cafe",
                LocalDate.of(2026, 6, 6),
                false,
                new BigDecimal("1000.00")
        );

        service.enqueueInitialReminders(transaction);

        verifyNoInteractions(notificationOutboxService);
        verifyNoInteractions(whatsAppMessageTemplateBuilder);
        verifyNoInteractions(borrowerActionLinkBuilder);
    }
}