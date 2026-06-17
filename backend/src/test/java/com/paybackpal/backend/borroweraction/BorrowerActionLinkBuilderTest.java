package com.paybackpal.backend.borroweraction;

import com.paybackpal.backend.borrower.entity.Borrower;
import com.paybackpal.backend.borroweraction.config.PublicActionLinkProperties;
import com.paybackpal.backend.borroweraction.dto.BorrowerActionLinks;
import com.paybackpal.backend.borroweraction.dto.GeneratedBorrowerActionToken;
import com.paybackpal.backend.borroweraction.entity.BorrowerActionToken;
import com.paybackpal.backend.borroweraction.entity.BorrowerActionType;
import com.paybackpal.backend.borroweraction.service.BorrowerActionLinkBuilder;
import com.paybackpal.backend.borroweraction.service.BorrowerActionTokenService;
import com.paybackpal.backend.card.entity.CreditCard;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BorrowerActionLinkBuilderTest {

    @Mock
    private BorrowerActionTokenService borrowerActionTokenService;

    @Test
    void buildLinksShouldGenerateReportPaidAndRemindMeLaterUrls() {
        PublicActionLinkProperties properties = new PublicActionLinkProperties();
        properties.setBaseUrl("https://paybackpal.com/");

        BorrowerActionLinkBuilder builder = new BorrowerActionLinkBuilder(
                borrowerActionTokenService,
                properties
        );

        TransactionSplit split = createSplit();

        when(borrowerActionTokenService.generateToken(split, BorrowerActionType.REPORT_PAID))
                .thenReturn(new GeneratedBorrowerActionToken(
                        "report-token",
                        createActionToken(split, BorrowerActionType.REPORT_PAID)
                ));

        when(borrowerActionTokenService.generateToken(split, BorrowerActionType.REMIND_ME_LATER))
                .thenReturn(new GeneratedBorrowerActionToken(
                        "remind-token",
                        createActionToken(split, BorrowerActionType.REMIND_ME_LATER)
                ));

        BorrowerActionLinks links = builder.buildLinks(split);

        assertThat(links.getReportPaidUrl())
                .isEqualTo("https://paybackpal.com/api/v1/public/borrower-actions/report-token/report-paid");

        assertThat(links.getRemindMeLaterUrl())
                .isEqualTo("https://paybackpal.com/api/v1/public/borrower-actions/remind-token/remind-me-later");
    }

    private BorrowerActionToken createActionToken(
            TransactionSplit split,
            BorrowerActionType actionType
    ) {
        return new BorrowerActionToken(
                split,
                actionType,
                "a".repeat(64),
                OffsetDateTime.now().plusDays(7)
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