package com.paybackpal.backend.borroweraction.dto;

import com.paybackpal.backend.borroweraction.entity.BorrowerActionToken;

public class GeneratedBorrowerActionToken {

    private final String rawToken;
    private final BorrowerActionToken borrowerActionToken;

    public GeneratedBorrowerActionToken(String rawToken, BorrowerActionToken borrowerActionToken) {
        this.rawToken = rawToken;
        this.borrowerActionToken = borrowerActionToken;
    }

    public String getRawToken() {
        return rawToken;
    }

    public BorrowerActionToken getBorrowerActionToken() {
        return borrowerActionToken;
    }
}
