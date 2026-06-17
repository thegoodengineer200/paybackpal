package com.paybackpal.backend.borroweraction;

import com.paybackpal.backend.borrower.entity.Borrower;
import com.paybackpal.backend.borroweraction.dto.GeneratedBorrowerActionToken;
import com.paybackpal.backend.borroweraction.entity.BorrowerActionToken;
import com.paybackpal.backend.borroweraction.entity.BorrowerActionType;
import com.paybackpal.backend.borroweraction.repository.BorrowerActionTokenRepository;
import com.paybackpal.backend.borroweraction.service.BorrowerActionTokenService;
import com.paybackpal.backend.borroweraction.service.SecureBorrowerActionTokenService;
import com.paybackpal.backend.card.entity.CreditCard;
import com.paybackpal.backend.common.exception.BusinessRuleViolationException;
import com.paybackpal.backend.common.exception.ResourceNotFoundException;
import com.paybackpal.backend.transaction.entity.CardTransaction;
import com.paybackpal.backend.transaction.entity.TransactionSplit;
import com.paybackpal.backend.user.entity.AppUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowerActionTokenServiceTest {

    @Mock
    private BorrowerActionTokenRepository borrowerActionTokenRepository;

    @Mock
    private SecureBorrowerActionTokenService secureBorrowerActionTokenService;

    @Test
    void generateTokenShouldStoreHashAndReturnRawToken() {
        BorrowerActionTokenService service = new BorrowerActionTokenService(
                borrowerActionTokenRepository,
                secureBorrowerActionTokenService
        );

        TransactionSplit split = createSplit();

        when(secureBorrowerActionTokenService.generateRawToken())
                .thenReturn("raw-token");

        when(secureBorrowerActionTokenService.hashToken("raw-token"))
                .thenReturn("hashed-token");

        when(borrowerActionTokenRepository.save(any(BorrowerActionToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        GeneratedBorrowerActionToken generatedToken = service.generateToken(
                split,
                BorrowerActionType.REPORT_PAID,
                Duration.ofDays(7)
        );

        ArgumentCaptor<BorrowerActionToken> captor =
                ArgumentCaptor.forClass(BorrowerActionToken.class);

        verify(borrowerActionTokenRepository).save(captor.capture());

        BorrowerActionToken savedToken = captor.getValue();

        assertThat(generatedToken.getRawToken()).isEqualTo("raw-token");
        assertThat(generatedToken.getBorrowerActionToken()).isSameAs(savedToken);
        assertThat(savedToken.getTransactionSplit()).isSameAs(split);
        assertThat(savedToken.getActionType()).isEqualTo(BorrowerActionType.REPORT_PAID);
        assertThat(savedToken.getTokenHash()).isEqualTo("hashed-token");
        assertThat(savedToken.getExpiresAt()).isAfter(OffsetDateTime.now().plusDays(6));
    }

    @Test
    void getValidTokenShouldReturnActiveTokenForExpectedAction() {
        BorrowerActionTokenService service = new BorrowerActionTokenService(
                borrowerActionTokenRepository,
                secureBorrowerActionTokenService
        );

        TransactionSplit split = createSplit();

        BorrowerActionToken token = new BorrowerActionToken(
                split,
                BorrowerActionType.REPORT_PAID,
                "hashed-token",
                OffsetDateTime.now().plusDays(1)
        );

        when(secureBorrowerActionTokenService.hashToken("raw-token"))
                .thenReturn("hashed-token");

        when(borrowerActionTokenRepository.findByTokenHashWithSplitDetails("hashed-token"))
                .thenReturn(Optional.of(token));

        BorrowerActionToken result = service.getValidToken(
                "raw-token",
                BorrowerActionType.REPORT_PAID
        );

        assertThat(result).isSameAs(token);
    }

    @Test
    void getValidTokenShouldRejectWrongActionType() {
        BorrowerActionTokenService service = new BorrowerActionTokenService(
                borrowerActionTokenRepository,
                secureBorrowerActionTokenService
        );

        BorrowerActionToken token = new BorrowerActionToken(
                createSplit(),
                BorrowerActionType.REMIND_ME_LATER,
                "hashed-token",
                OffsetDateTime.now().plusDays(1)
        );

        when(secureBorrowerActionTokenService.hashToken("raw-token"))
                .thenReturn("hashed-token");

        when(borrowerActionTokenRepository.findByTokenHashWithSplitDetails("hashed-token"))
                .thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.getValidToken(
                "raw-token",
                BorrowerActionType.REPORT_PAID
        )).isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void getValidTokenShouldRejectUnknownToken() {
        BorrowerActionTokenService service = new BorrowerActionTokenService(
                borrowerActionTokenRepository,
                secureBorrowerActionTokenService
        );

        when(secureBorrowerActionTokenService.hashToken("raw-token"))
                .thenReturn("hashed-token");

        when(borrowerActionTokenRepository.findByTokenHashWithSplitDetails("hashed-token"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getValidToken(
                "raw-token",
                BorrowerActionType.REPORT_PAID
        )).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getValidTokenShouldRejectExpiredToken() {
        BorrowerActionTokenService service = new BorrowerActionTokenService(
                borrowerActionTokenRepository,
                secureBorrowerActionTokenService
        );

        BorrowerActionToken token = new BorrowerActionToken(
                createSplit(),
                BorrowerActionType.REPORT_PAID,
                "hashed-token",
                OffsetDateTime.now().minusMinutes(1)
        );

        when(secureBorrowerActionTokenService.hashToken("raw-token"))
                .thenReturn("hashed-token");

        when(borrowerActionTokenRepository.findByTokenHashWithSplitDetails("hashed-token"))
                .thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.getValidToken(
                "raw-token",
                BorrowerActionType.REPORT_PAID
        )).isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void markTokenUsedShouldSetUsedAtAndSave() {
        BorrowerActionTokenService service = new BorrowerActionTokenService(
                borrowerActionTokenRepository,
                secureBorrowerActionTokenService
        );

        BorrowerActionToken token = new BorrowerActionToken(
                createSplit(),
                BorrowerActionType.REPORT_PAID,
                "hashed-token",
                OffsetDateTime.now().plusDays(1)
        );

        service.markTokenUsed(token);

        assertThat(token.getUsedAt()).isNotNull();
        verify(borrowerActionTokenRepository).save(token);
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
