package com.paybackpal.backend.borroweraction;

import com.paybackpal.backend.borroweraction.service.SecureBorrowerActionTokenService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SecureBorrowerActionTokenServiceTest {

    private final SecureBorrowerActionTokenService service =
            new SecureBorrowerActionTokenService();

    @Test
    void generateRawTokenShouldGenerateUrlSafeDifferentTokens() {
        String tokenOne = service.generateRawToken();
        String tokenTwo = service.generateRawToken();

        assertThat(tokenOne).isNotBlank();
        assertThat(tokenTwo).isNotBlank();
        assertThat(tokenOne).isNotEqualTo(tokenTwo);
        assertThat(tokenOne).doesNotContain("+");
        assertThat(tokenOne).doesNotContain("/");
        assertThat(tokenOne).doesNotContain("=");
    }

    @Test
    void hashTokenShouldReturnStableSha256HexHash() {
        String hashOne = service.hashToken("sample-token");
        String hashTwo = service.hashToken("sample-token");

        assertThat(hashOne).isEqualTo(hashTwo);
        assertThat(hashOne).hasSize(64);
        assertThat(hashOne).matches("^[0-9a-f]{64}$");
        assertThat(hashOne).isNotEqualTo("sample-token");
    }
}