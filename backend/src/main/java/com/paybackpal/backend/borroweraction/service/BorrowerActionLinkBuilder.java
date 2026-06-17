package com.paybackpal.backend.borroweraction.service;

import com.paybackpal.backend.borroweraction.config.PublicActionLinkProperties;
import com.paybackpal.backend.borroweraction.dto.BorrowerActionLinks;
import com.paybackpal.backend.borroweraction.dto.GeneratedBorrowerActionToken;
import com.paybackpal.backend.borroweraction.entity.BorrowerActionType;
import com.paybackpal.backend.transaction.entity.TransactionSplit;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class BorrowerActionLinkBuilder {

    private static final String PUBLIC_BORROWER_ACTIONS_PATH = "/api/v1/public/borrower-actions";

    private final BorrowerActionTokenService borrowerActionTokenService;
    private final PublicActionLinkProperties publicActionLinkProperties;

    public BorrowerActionLinkBuilder(
            BorrowerActionTokenService borrowerActionTokenService,
            PublicActionLinkProperties publicActionLinkProperties
    ) {
        this.borrowerActionTokenService = borrowerActionTokenService;
        this.publicActionLinkProperties = publicActionLinkProperties;
    }

    public BorrowerActionLinks buildLinks(TransactionSplit split) {
        GeneratedBorrowerActionToken reportPaidToken =
                borrowerActionTokenService.generateToken(split, BorrowerActionType.REPORT_PAID);
        GeneratedBorrowerActionToken remindMeLaterToken = borrowerActionTokenService.generateToken(split, BorrowerActionType.REMIND_ME_LATER);

        return new BorrowerActionLinks(
                buildActionUrl(reportPaidToken.getRawToken(), "/report-paid"),
                buildActionUrl(remindMeLaterToken.getRawToken(), "/remind-me-later")
        );
    }

    private String buildActionUrl(String rawToken, String actionPath) {
        return UriComponentsBuilder
                .fromUriString(publicActionLinkProperties.getNormalizedBaseUrl())
                .path(PUBLIC_BORROWER_ACTIONS_PATH)
                .pathSegment(rawToken)
                .path(actionPath)
                .build()
                .toUriString();
    }
}