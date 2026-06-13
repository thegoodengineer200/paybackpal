package com.paybackpal.backend.transaction;

import com.paybackpal.backend.auth.service.CurrentUserService;
import com.paybackpal.backend.borrower.entity.Borrower;
import com.paybackpal.backend.borrower.repository.BorrowerRepository;
import com.paybackpal.backend.card.entity.CreditCard;
import com.paybackpal.backend.card.repository.CreditCardRepository;
import com.paybackpal.backend.common.exception.BusinessRuleViolationException;
import com.paybackpal.backend.notification.service.InitialBorrowerReminderService;
import com.paybackpal.backend.transaction.dto.CreateTransactionRequest;
import com.paybackpal.backend.transaction.dto.TransactionResponse;
import com.paybackpal.backend.transaction.dto.TransactionSplitRequest;
import com.paybackpal.backend.transaction.entity.CardTransaction;
import com.paybackpal.backend.transaction.repository.CardTransactionRepository;
import com.paybackpal.backend.transaction.service.TransactionService;
import com.paybackpal.backend.user.entity.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID CARD_ID = UUID.randomUUID();
    private static final UUID BORROWER_ONE_ID = UUID.randomUUID();
    private static final UUID BORROWER_TWO_ID = UUID.randomUUID();

    @Mock
    private CardTransactionRepository cardTransactionRepository;

    @Mock
    private CreditCardRepository creditCardRepository;

    @Mock
    private BorrowerRepository borrowerRepository;

    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private InitialBorrowerReminderService initialBorrowerReminderService;

    private TransactionService transactionService;

    private AppUser user;
    private CreditCard card;
    private Borrower borrowerOne;
    private Borrower borrowerTwo;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(
                cardTransactionRepository,
                creditCardRepository,
                borrowerRepository,
                currentUserService, initialBorrowerReminderService
        );

        user = new AppUser(
                "Rahul Sharma",
                "rahul@example.com",
                "9876543210",
                "rahul@upi",
                "hashed-password"
        );
        ReflectionTestUtils.setField(user, "id", USER_ID);

        card = new CreditCard(
                user,
                "HDFC Millennia",
                "HDFC Bank",
                "1234",
                15,
                5
        );
        ReflectionTestUtils.setField(card, "id", CARD_ID);

        borrowerOne = new Borrower(user, "Aman", "9876500000");
        ReflectionTestUtils.setField(borrowerOne, "id", BORROWER_ONE_ID);

        borrowerTwo = new Borrower(user, "Neha", "9876500001");
        ReflectionTestUtils.setField(borrowerTwo, "id", BORROWER_TWO_ID);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(creditCardRepository.findByIdAndUser_IdAndActiveTrue(CARD_ID, USER_ID))
                .thenReturn(Optional.of(card));
//        when(cardTransactionRepository.save(any(CardTransaction.class)))
//                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void personalTransactionShouldSetOwnerShareToFullAmount() {
        when(cardTransactionRepository.save(any(CardTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        CreateTransactionRequest request = baseRequest(new BigDecimal("2500.00"));
        request.setBorrowed(false);

        TransactionResponse response = transactionService.createTransaction(CARD_ID, request);

        assertThat(response.isBorrowed()).isFalse();
        assertThat(response.getAmount()).isEqualByComparingTo("2500.00");
        assertThat(response.getOwnerShareAmount()).isEqualByComparingTo("2500.00");
        assertThat(response.getSplits()).isEmpty();
    }

    @Test
    void personalTransactionShouldRejectSplits() {
        CreateTransactionRequest request = baseRequest(new BigDecimal("2500.00"));
        request.setBorrowed(false);
        request.setSplits(List.of(equalSplit(BORROWER_ONE_ID)));

        assertThatThrownBy(() -> transactionService.createTransaction(CARD_ID, request))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Splits are not allowed for personal transactions");
    }

    @Test
    void equalSplitShouldPutRemainderOnOwner() {
        when(cardTransactionRepository.save(any(CardTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(borrowerRepository.findByIdInAndOwnerUser_IdAndActiveTrue(anyList(), eq(USER_ID)))
                .thenReturn(List.of(borrowerOne, borrowerTwo));

        CreateTransactionRequest request = baseRequest(new BigDecimal("100.00"));
        request.setBorrowed(true);
        request.setSplits(List.of(
                equalSplit(BORROWER_ONE_ID),
                equalSplit(BORROWER_TWO_ID)
        ));

        TransactionResponse response = transactionService.createTransaction(CARD_ID, request);

        assertThat(response.isBorrowed()).isTrue();
        assertThat(response.getAmount()).isEqualByComparingTo("100.00");
        assertThat(response.getOwnerShareAmount()).isEqualByComparingTo("33.34");
        assertThat(response.getSplits()).hasSize(2);
        assertThat(response.getSplits().get(0).getSplitAmount()).isEqualByComparingTo("33.33");
        assertThat(response.getSplits().get(1).getSplitAmount()).isEqualByComparingTo("33.33");
    }

    @Test
    void percentageSplitShouldCalculateOwnerShare() {
        when(cardTransactionRepository.save(any(CardTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(borrowerRepository.findByIdInAndOwnerUser_IdAndActiveTrue(anyList(), eq(USER_ID)))
                .thenReturn(List.of(borrowerOne, borrowerTwo));

        CreateTransactionRequest request = baseRequest(new BigDecimal("10000.00"));
        request.setBorrowed(true);
        request.setSplits(List.of(
                percentageSplit(BORROWER_ONE_ID, "25.00"),
                percentageSplit(BORROWER_TWO_ID, "25.00")
        ));

        TransactionResponse response = transactionService.createTransaction(CARD_ID, request);

        assertThat(response.isBorrowed()).isTrue();
        assertThat(response.getAmount()).isEqualByComparingTo("10000.00");
        assertThat(response.getOwnerShareAmount()).isEqualByComparingTo("5000.00");
        assertThat(response.getSplits()).hasSize(2);
        assertThat(response.getSplits().get(0).getSplitAmount()).isEqualByComparingTo("2500.00");
        assertThat(response.getSplits().get(1).getSplitAmount()).isEqualByComparingTo("2500.00");
    }

    @Test
    void totalPercentageAboveHundredShouldFail() {
        when(borrowerRepository.findByIdInAndOwnerUser_IdAndActiveTrue(anyList(), eq(USER_ID)))
                .thenReturn(List.of(borrowerOne, borrowerTwo));

        CreateTransactionRequest request = baseRequest(new BigDecimal("10000.00"));
        request.setBorrowed(true);
        request.setSplits(List.of(
                percentageSplit(BORROWER_ONE_ID, "60.00"),
                percentageSplit(BORROWER_TWO_ID, "50.00")
        ));

        assertThatThrownBy(() -> transactionService.createTransaction(CARD_ID, request))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Total borrower split percentage must not exceed 100");
    }

    private CreateTransactionRequest baseRequest(BigDecimal amount) {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAmount(amount);
        request.setDescription("Test transaction");
        request.setMerchantName("Test merchant");
        request.setTransactionDate(LocalDate.of(2026, 6, 6));
        return request;
    }

    private TransactionSplitRequest equalSplit(UUID borrowerId) {
        TransactionSplitRequest splitRequest = new TransactionSplitRequest();
        splitRequest.setBorrowerId(borrowerId);
        return splitRequest;
    }

    private TransactionSplitRequest percentageSplit(UUID borrowerId, String percentage) {
        TransactionSplitRequest splitRequest = new TransactionSplitRequest();
        splitRequest.setBorrowerId(borrowerId);
        splitRequest.setSplitPercentage(new BigDecimal(percentage));
        return splitRequest;
    }
}