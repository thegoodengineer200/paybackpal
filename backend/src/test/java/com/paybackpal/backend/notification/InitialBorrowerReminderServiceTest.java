package com.paybackpal.backend.notification;

import com.paybackpal.backend.borrower.entity.Borrower;
import com.paybackpal.backend.card.entity.CreditCard;
import com.paybackpal.backend.notification.entity.NotificationType;
import com.paybackpal.backend.notification.service.InitialBorrowerReminderService;
import com.paybackpal.backend.notification.service.NotificationOutboxService;
import com.paybackpal.backend.transaction.entity.CardTransaction;
import com.paybackpal.backend.transaction.entity.TransactionSplit;
import com.paybackpal.backend.user.entity.AppUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class InitialBorrowerReminderServiceTest {

    @Mock
    private NotificationOutboxService notificationOutboxService;

    @Test
    void borrowedTransactionShouldQueueInitialReminderForEachSplit() {
        InitialBorrowerReminderService service =
                new InitialBorrowerReminderService(notificationOutboxService);

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

        service.enqueueInitialReminders(transaction);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        verify(notificationOutboxService).enqueueWhatsApp(
                eq(alexSplit),
                eq(NotificationType.INITIAL_PAYMENT_REQUEST),
                eq("9876500000"),
                messageCaptor.capture(),
                any(OffsetDateTime.class)
        );

        verify(notificationOutboxService).enqueueWhatsApp(
                eq(marieSplit),
                eq(NotificationType.INITIAL_PAYMENT_REQUEST),
                eq("9876500001"),
                any(String.class),
                any(OffsetDateTime.class)
        );

        String alexMessage = messageCaptor.getValue();

        assertThat(alexMessage).contains("Hi Alex");
        assertThat(alexMessage).contains("Alice Bob");
        assertThat(alexMessage).contains("₹2500.00");
        assertThat(alexMessage).contains("Pizza Express");
        assertThat(alexMessage).contains("alice@upi");
        assertThat(alexMessage).contains("Paid");
        assertThat(alexMessage).contains("Remind me later");
    }

    @Test
    void personalTransactionShouldNotQueueReminder() {
        InitialBorrowerReminderService service =
                new InitialBorrowerReminderService(notificationOutboxService);

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
    }
}