package com.paybackpal.backend.transaction.service;

import com.paybackpal.backend.auth.service.CurrentUserService;
import com.paybackpal.backend.borrower.entity.Borrower;
import com.paybackpal.backend.borrower.repository.BorrowerRepository;
import com.paybackpal.backend.card.entity.CreditCard;
import com.paybackpal.backend.card.repository.CreditCardRepository;
import com.paybackpal.backend.common.exception.BusinessRuleViolationException;
import com.paybackpal.backend.common.exception.ResourceNotFoundException;
import com.paybackpal.backend.transaction.dto.CreateTransactionRequest;
import com.paybackpal.backend.transaction.dto.TransactionResponse;
import com.paybackpal.backend.transaction.dto.TransactionSplitRequest;
import com.paybackpal.backend.transaction.entity.CardTransaction;
import com.paybackpal.backend.transaction.entity.TransactionSplit;
import com.paybackpal.backend.transaction.repository.CardTransactionRepository;
import com.paybackpal.backend.user.entity.AppUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TransactionService {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100.00");

    private final CardTransactionRepository cardTransactionRepository;
    private final CreditCardRepository creditCardRepository;
    private final BorrowerRepository borrowerRepository;
    private final CurrentUserService currentUserService;

    public TransactionService(
            CardTransactionRepository cardTransactionRepository,
            CreditCardRepository creditCardRepository,
            BorrowerRepository borrowerRepository,
            CurrentUserService currentUserService
    ) {
        this.cardTransactionRepository = cardTransactionRepository;
        this.creditCardRepository = creditCardRepository;
        this.borrowerRepository = borrowerRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public TransactionResponse createTransaction(
            UUID cardId,
            CreateTransactionRequest request
    ) {
        AppUser currentUser = currentUserService.getCurrentUser();

        CreditCard creditCard = creditCardRepository
                .findByIdAndUser_IdAndActiveTrue(cardId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));

        boolean borrowed = Boolean.TRUE.equals(request.getBorrowed());
        BigDecimal normalizedAmount = normalizeMoney(request.getAmount());

        if (!borrowed) {
            return createPersonalTransaction(
                    currentUser,
                    creditCard,
                    normalizedAmount,
                    request
            );
        }

        return createBorrowedTransaction(
                currentUser,
                creditCard,
                normalizedAmount,
                request
        );
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsForCard(UUID cardId) {
        AppUser currentUser = currentUserService.getCurrentUser();

        CreditCard creditCard = creditCardRepository
                .findByIdAndUser_IdAndActiveTrue(cardId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));

        return cardTransactionRepository
                .findByCreditCard_IdAndUser_IdAndActiveTrueOrderByTransactionDateDescCreatedAtDesc(
                        creditCard.getId(),
                        currentUser.getId()
                )
                .stream()
                .map(TransactionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransaction(UUID transactionId) {
        AppUser currentUser = currentUserService.getCurrentUser();

        CardTransaction transaction = cardTransactionRepository
                .findByIdAndUser_IdAndActiveTrue(transactionId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        return TransactionResponse.from(transaction);
    }

    private TransactionResponse createPersonalTransaction(
            AppUser currentUser,
            CreditCard creditCard,
            BigDecimal amount,
            CreateTransactionRequest request
    ) {
        if (request.getSplits() != null && !request.getSplits().isEmpty()) {
            throw new BusinessRuleViolationException(
                    "Splits are not allowed for personal transactions"
            );
        }

        CardTransaction transaction = new CardTransaction(
                currentUser,
                creditCard,
                amount,
                normalizeOptional(request.getDescription()),
                normalizeOptional(request.getMerchantName()),
                request.getTransactionDate(),
                false,
                amount
        );

        CardTransaction savedTransaction = cardTransactionRepository.save(transaction);

        return TransactionResponse.from(savedTransaction);
    }

    private TransactionResponse createBorrowedTransaction(
            AppUser currentUser,
            CreditCard creditCard,
            BigDecimal amount,
            CreateTransactionRequest request
    ) {
        List<TransactionSplitRequest> splitRequests = request.getSplits();

        if (splitRequests == null || splitRequests.isEmpty()) {
            throw new BusinessRuleViolationException(
                    "At least one borrower split is required for borrowed transaction"
            );
        }

        validateNoDuplicateBorrowers(splitRequests);

        List<UUID> borrowerIds = splitRequests.stream()
                .map(TransactionSplitRequest::getBorrowerId)
                .toList();

        List<Borrower> borrowers = borrowerRepository
                .findByIdInAndOwnerUser_IdAndActiveTrue(
                        borrowerIds,
                        currentUser.getId()
                );

        if (borrowers.size() != borrowerIds.size()) {
            throw new ResourceNotFoundException(
                    "One or more borrowers were not found"
            );
        }

        Map<UUID, Borrower> borrowerById = new HashMap<>();

        for (Borrower borrower : borrowers) {
            borrowerById.put(borrower.getId(), borrower);
        }

        SplitMode splitMode = determineSplitMode(splitRequests);

        CardTransaction transaction;

        if (splitMode == SplitMode.EQUAL) {
            transaction = buildEqualSplitTransaction(
                    currentUser,
                    creditCard,
                    amount,
                    request,
                    splitRequests,
                    borrowerById
            );
        } else {
            transaction = buildPercentageSplitTransaction(
                    currentUser,
                    creditCard,
                    amount,
                    request,
                    splitRequests,
                    borrowerById
            );
        }

        CardTransaction savedTransaction = cardTransactionRepository.save(transaction);

        return TransactionResponse.from(savedTransaction);
    }

    private CardTransaction buildEqualSplitTransaction(
            AppUser currentUser,
            CreditCard creditCard,
            BigDecimal amount,
            CreateTransactionRequest request,
            List<TransactionSplitRequest> splitRequests,
            Map<UUID, Borrower> borrowerById
    ) {
        int participantCount = splitRequests.size() + 1;

        long totalCents = toCents(amount);
        long borrowerShareCents = totalCents / participantCount;

        BigDecimal borrowerShareAmount = fromCents(borrowerShareCents);

        BigDecimal totalBorrowerAmount = borrowerShareAmount
                .multiply(BigDecimal.valueOf(splitRequests.size()))
                .setScale(2, RoundingMode.UNNECESSARY);

        BigDecimal ownerShareAmount = amount.subtract(totalBorrowerAmount)
                .setScale(2, RoundingMode.UNNECESSARY);

        CardTransaction transaction = new CardTransaction(
                currentUser,
                creditCard,
                amount,
                normalizeOptional(request.getDescription()),
                normalizeOptional(request.getMerchantName()),
                request.getTransactionDate(),
                true,
                ownerShareAmount
        );

        for (TransactionSplitRequest splitRequest : splitRequests) {
            Borrower borrower = borrowerById.get(splitRequest.getBorrowerId());

            TransactionSplit split = new TransactionSplit(
                    borrower,
                    null,
                    borrowerShareAmount
            );

            transaction.addSplit(split);
        }

        return transaction;
    }

    private CardTransaction buildPercentageSplitTransaction(
            AppUser currentUser,
            CreditCard creditCard,
            BigDecimal amount,
            CreateTransactionRequest request,
            List<TransactionSplitRequest> splitRequests,
            Map<UUID, Borrower> borrowerById
    ) {
        BigDecimal totalPercentage = BigDecimal.ZERO;

        for (TransactionSplitRequest splitRequest : splitRequests) {
            totalPercentage = totalPercentage.add(normalizePercentage(splitRequest.getSplitPercentage()));
        }

        if (totalPercentage.compareTo(ONE_HUNDRED) > 0) {
            throw new BusinessRuleViolationException(
                    "Total borrower split percentage must not exceed 100"
            );
        }

        BigDecimal totalBorrowerAmount = BigDecimal.ZERO;

        CardTransaction transaction = new CardTransaction(
                currentUser,
                creditCard,
                amount,
                normalizeOptional(request.getDescription()),
                normalizeOptional(request.getMerchantName()),
                request.getTransactionDate(),
                true,
                BigDecimal.ZERO
        );

        for (TransactionSplitRequest splitRequest : splitRequests) {
            Borrower borrower = borrowerById.get(splitRequest.getBorrowerId());
            BigDecimal splitPercentage = normalizePercentage(splitRequest.getSplitPercentage());

            BigDecimal splitAmount = amount
                    .multiply(splitPercentage)
                    .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);

            totalBorrowerAmount = totalBorrowerAmount.add(splitAmount);

            TransactionSplit split = new TransactionSplit(borrower, splitPercentage, splitAmount);

            transaction.addSplit(split);
        }

        BigDecimal ownerShareAmount = amount.subtract(totalBorrowerAmount).setScale(2, RoundingMode.UNNECESSARY);
        transaction.updateOwnerShareAmount(ownerShareAmount);
        return transaction;
    }

    private void validateNoDuplicateBorrowers(
            List<TransactionSplitRequest> splitRequests
    ) {
        HashSet<UUID> seenBorrowerIds = new HashSet<>();

        for (TransactionSplitRequest splitRequest : splitRequests) {
            if (!seenBorrowerIds.add(splitRequest.getBorrowerId())) {
                throw new BusinessRuleViolationException(
                        "Same borrower cannot be added more than once in a transaction"
                );
            }
        }
    }

    private SplitMode determineSplitMode(
            List<TransactionSplitRequest> splitRequests
    ) {
        boolean allPercentagesMissing = splitRequests.stream()
                .allMatch(split -> split.getSplitPercentage() == null);

        boolean allPercentagesPresent = splitRequests.stream()
                .allMatch(split -> split.getSplitPercentage() != null);

        if (allPercentagesMissing) {
            return SplitMode.EQUAL;
        }

        if (allPercentagesPresent) {
            return SplitMode.PERCENTAGE;
        }

        throw new BusinessRuleViolationException(
                "Either provide split percentage for all borrowers or for none"
        );
    }

    private BigDecimal normalizeMoney(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.UNNECESSARY);
    }

    private BigDecimal normalizePercentage(BigDecimal percentage) {
        return percentage.setScale(2, RoundingMode.UNNECESSARY);
    }

    private long toCents(BigDecimal amount) {
        return amount.movePointRight(2).longValueExact();
    }

    private BigDecimal fromCents(long cents) {
        return BigDecimal.valueOf(cents, 2);
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private enum SplitMode {
        EQUAL,
        PERCENTAGE
    }
}