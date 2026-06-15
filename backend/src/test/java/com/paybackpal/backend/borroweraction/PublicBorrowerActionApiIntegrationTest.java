package com.paybackpal.backend.borroweraction;

import com.fasterxml.jackson.databind.JsonNode;
import com.paybackpal.backend.BaseIntegrationTest;
import com.paybackpal.backend.borroweraction.dto.GeneratedBorrowerActionToken;
import com.paybackpal.backend.borroweraction.entity.BorrowerActionType;
import com.paybackpal.backend.borroweraction.service.BorrowerActionTokenService;
import com.paybackpal.backend.transaction.entity.TransactionSplit;
import com.paybackpal.backend.transaction.repository.TransactionSplitRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

class PublicBorrowerActionApiIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private BorrowerActionTokenService borrowerActionTokenService;

    @Autowired
    private TransactionSplitRepository transactionSplitRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void publicReportPaidShouldMarkSplitPaymentReportedUseTokenAndNotifyOwner() throws Exception {
        String ownerToken = registerAndLogin("alice@example.com", "9876543210");

        String cardId = createCard(ownerToken);
        String borrowerId = createBorrower(ownerToken, "Alex", "9876500000");

        String transactionResponseBody = mockMvc.perform(post("/api/v1/cards/{cardId}/transactions", cardId)
                        .header("Authorization", bearer(ownerToken))
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

        GeneratedBorrowerActionToken generatedToken = borrowerActionTokenService.generateToken(
                split,
                BorrowerActionType.REPORT_PAID
        );

        String responseBody = mockMvc.perform(post(
                        "/api/v1/public/borrower-actions/{token}/report-paid",
                        generatedToken.getRawToken()
                ))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isEqualTo(200))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode response = json(responseBody);

        assertThat(response.get("transactionSplitId").asText()).isEqualTo(splitId);
        assertThat(response.get("repaymentStatus").asText()).isEqualTo("PAYMENT_REPORTED");
        assertThat(response.get("message").asText())
                .contains("Waiting for owner confirmation");

        String repaymentStatus = jdbcTemplate.queryForObject(
                """
                SELECT repayment_status
                FROM transaction_splits
                WHERE id = ?::uuid
                """,
                String.class,
                splitId
        );

        assertThat(repaymentStatus).isEqualTo("PAYMENT_REPORTED");

        Long usedTokenCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM borrower_action_tokens
                WHERE used_at IS NOT NULL
                """,
                Long.class
        );

        assertThat(usedTokenCount).isEqualTo(1L);

        Long ownerNotificationCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM notification_outbox
                WHERE notification_type = 'PAYMENT_REPORTED_TO_OWNER'
                  AND channel = 'WHATSAPP'
                  AND status = 'PENDING'
                  AND recipient_phone_number = '9876543210'
                """,
                Long.class
        );

        assertThat(ownerNotificationCount).isEqualTo(1L);
    }

    @Test
    void publicReportPaidShouldRejectReusedToken() throws Exception {
        String ownerToken = registerAndLogin("alice@example.com", "9876543210");

        String cardId = createCard(ownerToken);
        String borrowerId = createBorrower(ownerToken, "Alex", "9876500000");

        String transactionResponseBody = mockMvc.perform(post("/api/v1/cards/{cardId}/transactions", cardId)
                        .header("Authorization", bearer(ownerToken))
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

        GeneratedBorrowerActionToken generatedToken = borrowerActionTokenService.generateToken(
                split,
                BorrowerActionType.REPORT_PAID
        );

        mockMvc.perform(post(
                        "/api/v1/public/borrower-actions/{token}/report-paid",
                        generatedToken.getRawToken()
                ))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isEqualTo(200));

        mockMvc.perform(post(
                        "/api/v1/public/borrower-actions/{token}/report-paid",
                        generatedToken.getRawToken()
                ))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isEqualTo(400));
    }

    @Test
    void publicReportPaidShouldRejectInvalidToken() throws Exception {
        mockMvc.perform(post(
                        "/api/v1/public/borrower-actions/{token}/report-paid",
                        "invalid-token"
                ))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isEqualTo(404));
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