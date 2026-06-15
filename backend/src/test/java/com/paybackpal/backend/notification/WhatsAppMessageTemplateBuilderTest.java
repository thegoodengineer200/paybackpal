package com.paybackpal.backend.notification;

import com.paybackpal.backend.borrower.entity.Borrower;
import com.paybackpal.backend.card.entity.CreditCard;
import com.paybackpal.backend.notification.template.WhatsAppMessageTemplateBuilder;
import com.paybackpal.backend.transaction.entity.CardTransaction;
import com.paybackpal.backend.transaction.entity.TransactionSplit;
import com.paybackpal.backend.user.entity.AppUser;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class WhatsAppMessageTemplateBuilderTest {

    private final WhatsAppMessageTemplateBuilder templateBuilder =
            new WhatsAppMessageTemplateBuilder();

    @Test
    void initialPaymentRequestShouldIncludeBorrowerOwnerAmountMerchantUpiAndActions() {
        TransactionSplit split = createSplit();

        String message = templateBuilder.buildInitialPaymentRequest(split);

        assertThat(message).contains("Hi Alex");
        assertThat(message).contains("Alice Bob");
        assertThat(message).contains("₹2500.00");
        assertThat(message).contains("Pizza Express");
        assertThat(message).contains("alice@upi");
        assertThat(message).contains("Paid");
        assertThat(message).contains("Remind me later");
    }

    @Test
    void manualReminderShouldIncludeGentleReminderText() {
        TransactionSplit split = createSplit();

        String message = templateBuilder.buildManualReminder(split);

        assertThat(message).contains("gentle reminder");
        assertThat(message).contains("Alex");
        assertThat(message).contains("Alice Bob");
        assertThat(message).contains("₹2500.00");
        assertThat(message).contains("Pizza Express");
        assertThat(message).contains("alice@upi");
    }

    @Test
    void paymentReportedToOwnerShouldAskOwnerToConfirm() {
        TransactionSplit split = createSplit();

        String message = templateBuilder.buildPaymentReportedToOwner(split);

        assertThat(message).contains("Hi Alice Bob");
        assertThat(message).contains("Alex has reported payment");
        assertThat(message).contains("₹2500.00");
        assertThat(message).contains("Pizza Express");
        assertThat(message).contains("Please confirm");
    }

    @Test
    void paymentConfirmedToBorrowerShouldTellBorrowerPaymentIsConfirmed() {
        TransactionSplit split = createSplit();

        String message = templateBuilder.buildPaymentConfirmedToBorrower(split);

        assertThat(message).contains("Hi Alex");
        assertThat(message).contains("payment of ₹2500.00");
        assertThat(message).contains("Pizza Express");
        assertThat(message).contains("confirmed by Alice Bob");
    }

    @Test
    void remindMeLaterConfirmationShouldIncludeNextReminderTime() {
        TransactionSplit split = createSplit();
        OffsetDateTime nextReminderAt = OffsetDateTime.of(
                2026,
                6,
                15,
                10,
                30,
                0,
                0,
                ZoneOffset.UTC
        );

        String message = templateBuilder.buildRemindMeLaterConfirmation(
                split,
                nextReminderAt
        );

        assertThat(message).contains("Hi Alex");
        assertThat(message).contains("We will remind you again");
        assertThat(message).contains("₹2500.00");
        assertThat(message).contains("Pizza Express");
        assertThat(message).contains("15 Jun 2026");
    }

    @Test
    void shouldFallbackToDescriptionWhenMerchantNameIsMissing() {
        TransactionSplit split = createSplitWithoutMerchantName();

        String message = templateBuilder.buildInitialPaymentRequest(split);

        assertThat(message).contains("Dinner with friends");
    }

    @Test
    void shouldFallbackToGenericTransactionLabelWhenMerchantAndDescriptionAreMissing() {
        TransactionSplit split = createSplitWithoutMerchantNameAndDescription();

        String message = templateBuilder.buildInitialPaymentRequest(split);

        assertThat(message).contains("this transaction");
    }

    @Test
    void shouldSkipUpiLineWhenOwnerUpiIsMissing() {
        TransactionSplit split = createSplitWithoutOwnerUpi();

        String message = templateBuilder.buildInitialPaymentRequest(split);

        assertThat(message).doesNotContain("UPI:");
    }

    private TransactionSplit createSplit() {
        AppUser owner = createOwner("alice@upi");

        CreditCard creditCard = createCreditCard(owner);

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

        Borrower borrower = new Borrower(owner, "Alex", "9876500000");

        TransactionSplit split = new TransactionSplit(
                borrower,
                new BigDecimal("25.00"),
                new BigDecimal("2500.00")
        );

        transaction.addSplit(split);

        return split;
    }

    private TransactionSplit createSplitWithoutMerchantName() {
        AppUser owner = createOwner("alice@upi");

        CreditCard creditCard = createCreditCard(owner);

        CardTransaction transaction = new CardTransaction(
                owner,
                creditCard,
                new BigDecimal("10000.00"),
                "Dinner with friends",
                null,
                LocalDate.of(2026, 6, 6),
                true,
                new BigDecimal("5000.00")
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

    private TransactionSplit createSplitWithoutMerchantNameAndDescription() {
        AppUser owner = createOwner("alice@upi");

        CreditCard creditCard = createCreditCard(owner);

        CardTransaction transaction = new CardTransaction(
                owner,
                creditCard,
                new BigDecimal("10000.00"),
                null,
                null,
                LocalDate.of(2026, 6, 6),
                true,
                new BigDecimal("5000.00")
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

    private TransactionSplit createSplitWithoutOwnerUpi() {
        AppUser owner = createOwner(null);

        CreditCard creditCard = createCreditCard(owner);

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

        Borrower borrower = new Borrower(owner, "Alex", "9876500000");

        TransactionSplit split = new TransactionSplit(
                borrower,
                new BigDecimal("25.00"),
                new BigDecimal("2500.00")
        );

        transaction.addSplit(split);

        return split;
    }

    private AppUser createOwner(String upiId) {
        return new AppUser(
                "Alice Bob",
                "alice@example.com",
                "9876543210",
                upiId,
                "hashed-password"
        );
    }

    private CreditCard createCreditCard(AppUser owner) {
        return new CreditCard(
                owner,
                "HDFC Millennia",
                "HDFC Bank",
                "1234",
                15,
                5
        );
    }
}