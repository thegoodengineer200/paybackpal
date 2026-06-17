package com.paybackpal.backend.borroweraction;

import com.fasterxml.jackson.databind.JsonNode;
import com.paybackpal.backend.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@TestPropertySource(properties = {
        "app.public-actions.base-url=https://paybackpal.test"
})
class BorrowerActionLinkIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void borrowedTransactionShouldQueueInitialReminderWithTokenizedActionLinks() throws Exception {
        String ownerToken = registerAndLogin("alice@example.com", "9876543210");

        String cardId = createCard(ownerToken);
        String borrowerId = createBorrower(ownerToken, "Alex", "9876500000");

        mockMvc.perform(post("/api/v1/cards/{cardId}/transactions", cardId)
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
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isEqualTo(201));

        String messageBody = jdbcTemplate.queryForObject(
                """
                SELECT message_body
                FROM notification_outbox
                WHERE notification_type = 'INITIAL_PAYMENT_REQUEST'
                LIMIT 1
                """,
                String.class
        );

        assertThat(messageBody).contains("Paid: https://paybackpal.test/api/v1/public/borrower-actions/");
        assertThat(messageBody).contains("/report-paid");
        assertThat(messageBody).contains("Remind me later: https://paybackpal.test/api/v1/public/borrower-actions/");
        assertThat(messageBody).contains("/remind-me-later");
        assertThat(messageBody).doesNotContain("Actions: Paid | Remind me later");

        Long tokenCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM borrower_action_tokens
                """,
                Long.class
        );

        assertThat(tokenCount).isEqualTo(2L);

        Long reportPaidTokenCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM borrower_action_tokens
                WHERE action_type = 'REPORT_PAID'
                """,
                Long.class
        );

        Long remindMeLaterTokenCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM borrower_action_tokens
                WHERE action_type = 'REMIND_ME_LATER'
                """,
                Long.class
        );

        assertThat(reportPaidTokenCount).isEqualTo(1L);
        assertThat(remindMeLaterTokenCount).isEqualTo(1L);
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

        JsonNode response = json(responseBody);
        return response.get("id").asText();
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

        JsonNode response = json(responseBody);
        return response.get("id").asText();
    }
}