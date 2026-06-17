package com.paybackpal.backend.notification;

import com.paybackpal.backend.borrower.entity.Borrower;
import com.paybackpal.backend.borroweraction.dto.BorrowerActionLinks;
import com.paybackpal.backend.borroweraction.service.BorrowerActionLinkBuilder;
import com.paybackpal.backend.card.entity.CreditCard;
import com.paybackpal.backend.notification.entity.NotificationType;
import com.paybackpal.backend.notification.service.ManualReminderService;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManualReminderServiceTest {

    @Mock
    private NotificationOutboxService notificationOutboxService;

    @Mock
    private WhatsAppMessageTemplateBuilder whatsAppMessageTemplateBuilder;

    @Mock
    private BorrowerActionLinkBuilder borrowerActionLinkBuilder;

    @Test
    void enqueueManualReminderShouldQueueManualReminderWhatsAppNotification() {
        ManualReminderService service = new ManualReminderService(
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
                new BigDecimal("7500.00")
        );

        Borrower borrower = new Borrower(owner, "Alex", "9876500000");

        TransactionSplit split = new TransactionSplit(
                borrower,
                new BigDecimal("25.00"),
                new BigDecimal("2500.00")
        );

        transaction.addSplit(split);
        BorrowerActionLinks actionLinks = new BorrowerActionLinks(
                "https://paybackpal.com/paid",
            "https://paybackpal.com/later"
        );
        when(borrowerActionLinkBuilder.buildLinks(split)).thenReturn(actionLinks);

        when(whatsAppMessageTemplateBuilder.buildManualReminder(split, actionLinks)).thenReturn("Manual reminder message with Links");
        service.enqueueManualReminder(split);
        verify(notificationOutboxService).enqueueWhatsApp(
                eq(split),
                eq(NotificationType.MANUAL_REMINDER),
                eq("9876500000"),
                eq("Manual reminder message with Links"),
                any(OffsetDateTime.class)
        );
    }
}