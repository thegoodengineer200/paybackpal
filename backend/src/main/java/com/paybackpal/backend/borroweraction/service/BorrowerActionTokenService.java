package com.paybackpal.backend.borroweraction.service;

import com.paybackpal.backend.borroweraction.dto.GeneratedBorrowerActionToken;
import com.paybackpal.backend.borroweraction.entity.BorrowerActionToken;
import com.paybackpal.backend.borroweraction.entity.BorrowerActionType;
import com.paybackpal.backend.borroweraction.repository.BorrowerActionTokenRepository;
import com.paybackpal.backend.common.exception.BusinessRuleViolationException;
import com.paybackpal.backend.common.exception.ResourceNotFoundException;
import com.paybackpal.backend.transaction.entity.TransactionSplit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
public class BorrowerActionTokenService {
    private static final Duration DEFAULT_TOKEN_TTL = Duration.ofDays(14);

    private final BorrowerActionTokenRepository borrowerActionTokenRepository;
    private final SecureBorrowerActionTokenService secureBorrowerActionTokenService;

    public BorrowerActionTokenService(BorrowerActionTokenRepository borrowerActionTokenRepository, SecureBorrowerActionTokenService secureBorrowerActionTokenService) {
        this.borrowerActionTokenRepository = borrowerActionTokenRepository;
        this.secureBorrowerActionTokenService = secureBorrowerActionTokenService;
    }

    @Transactional
    public GeneratedBorrowerActionToken generateToken(
            TransactionSplit split, BorrowerActionType actionType
    ) {
        return generateToken(split, actionType, DEFAULT_TOKEN_TTL);
    }

    @Transactional
    public GeneratedBorrowerActionToken generateToken(
            TransactionSplit split, BorrowerActionType actionType, Duration ttl
    ) {
        String rawToken = secureBorrowerActionTokenService.generateRawToken();
        String tokenHash = secureBorrowerActionTokenService.hashToken(rawToken);

        BorrowerActionToken token = new BorrowerActionToken(
                split, actionType, tokenHash, OffsetDateTime.now(ZoneOffset.UTC).plus(ttl)
        );

        BorrowerActionToken savedToken = borrowerActionTokenRepository.save(token);
        return new GeneratedBorrowerActionToken(rawToken, savedToken);
    }

    @Transactional(readOnly = true)
    public BorrowerActionToken getValidToken(
            String rawToken,
            BorrowerActionType expectedActionType
    ) {
        String tokenHash = secureBorrowerActionTokenService.hashToken(rawToken);
        BorrowerActionToken token = borrowerActionTokenRepository.findByTokenHashWithSplitDetails(tokenHash).orElseThrow(() -> new ResourceNotFoundException("Invalid borrower action token"));

        if (token.getActionType() != expectedActionType) {
            throw new BusinessRuleViolationException("Borrower action token is not valid for this action");
        }

        if (!token.isActive(OffsetDateTime.now(ZoneOffset.UTC))) {
            throw new BusinessRuleViolationException("Borrower action token is expired or already used");
        }
        return token;
    }

    @Transactional
    public void markTokenUsed(BorrowerActionToken token) {
        token.markUsed();
        borrowerActionTokenRepository.save(token);
    }
}
