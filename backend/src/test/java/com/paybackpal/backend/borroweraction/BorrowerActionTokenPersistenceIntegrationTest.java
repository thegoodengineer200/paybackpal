package com.paybackpal.backend.borroweraction;

import com.paybackpal.backend.BaseIntegrationTest;
import com.paybackpal.backend.borroweraction.entity.BorrowerActionToken;
import com.paybackpal.backend.borroweraction.entity.BorrowerActionType;
import com.paybackpal.backend.borroweraction.repository.BorrowerActionTokenRepository;
import com.paybackpal.backend.transaction.entity.TransactionSplit;
import com.paybackpal.backend.transaction.repository.TransactionSplitRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

class BorrowerActionTokenPersistenceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private BorrowerActionTokenRepository borrowerActionTokenRepository;

    @Autowired
    private TransactionSplitRepository transactionSplitRepository;

    @Test
    void borrowerActionTokenShouldPersistForTransactionSplit() throws Exception {
        String token = registerAndLogin("alice@example.com", "9876543210");

        String cardId = createCard(token);
        String borrowerId = createBorrower(token, "Alex", "9876500000");

        String transactionResponseBody = mockMvc.perform(post("/api/v1/cards/{cardId}/transactions", cardId)
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content("""
                                {
                                  "amount": 10000,
                                  "description": "Dinner with friends",
                                  "merchantName": "Pizza Express",
                                  "transactionDate": "2026-06-06",
                                  "borrowed": true,
                                  "splits": [
                                    {
                                      "borrowerId": "%s",
                                      "splitPercentage": 25
                                    }
                                  ]
                                }
                                """.formatted(borrowerId)))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isEqualTo(201))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String splitId = json(transactionResponseBody)
                .get("splits")
                .get(0)
                .get("id")
                .asText();

        TransactionSplit split = transactionSplitRepository.findById(UUID.fromString(splitId))
                .orElseThrow();

        BorrowerActionToken actionToken = new BorrowerActionToken(
                split,
                BorrowerActionType.REPORT_PAID,
                "a".repeat(64),
                OffsetDateTime.now(ZoneOffset.UTC).plusDays(7)
        );

        BorrowerActionToken savedToken = borrowerActionTokenRepository.save(actionToken);

        assertThat(savedToken.getId()).isNotNull();
        assertThat(savedToken.getTransactionSplit().getId()).isEqualTo(split.getId());
        assertThat(savedToken.getActionType()).isEqualTo(BorrowerActionType.REPORT_PAID);
        assertThat(savedToken.getTokenHash()).isEqualTo("a".repeat(64));
    }

    private String createCard(String token) throws Exception {
        String responseBody = mockMvc.perform(post("/api/v1/cards")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content("""
                                {
                                  "cardName": "HDFC Millennia",
                                  "bankName": "HDFC Bank",
                                  "lastFourDigits": "1234",
                                  "billingCycleDay": 15,
                                  "dueDay": 5
                                }
                                """))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isEqualTo(201))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return json(responseBody).get("id").asText();
    }

    private String createBorrower(
            String token,
            String name,
            String phoneNumber
    ) throws Exception {
        String responseBody = mockMvc.perform(post("/api/v1/borrowers")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "%s",
                                  "phoneNumber": "%s"
                                }
                                """.formatted(name, phoneNumber)))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isEqualTo(201))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return json(responseBody).get("id").asText();
    }
}