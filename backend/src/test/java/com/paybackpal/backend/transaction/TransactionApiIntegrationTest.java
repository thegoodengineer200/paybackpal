package com.paybackpal.backend.transaction;

import com.fasterxml.jackson.databind.JsonNode;
import com.paybackpal.backend.BaseIntegrationTest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

class TransactionApiIntegrationTest extends BaseIntegrationTest {

    @Test
    void borrowedPercentageSplitShouldCreateSplitsAndDashboardSummary() throws Exception {
        String token = registerAndLogin("alice@example.com", "9876543210");

        String cardId = createCard(token);
        String borrowerOneId = createBorrower(token, "Alex", "9876500000");
        String borrowerTwoId = createBorrower(token, "Marie", "9876500001");

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
                                    },
                                    {
                                      "borrowerId": "%s",
                                      "splitPercentage": 25
                                    }
                                  ]
                                }
                                """.formatted(borrowerOneId, borrowerTwoId)))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isEqualTo(201))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode transactionResponse = json(transactionResponseBody);

        assertMoney(transactionResponse.get("amount"), "10000.00");
        assertThat(transactionResponse.get("borrowed").asBoolean()).isTrue();
        assertMoney(transactionResponse.get("ownerShareAmount"), "5000.00");
        assertThat(transactionResponse.get("splits")).hasSize(2);

        String firstSplitId = transactionResponse.get("splits").get(0).get("id").asText();

        mockMvc.perform(post("/api/v1/transaction-splits/{splitId}/report-paid", firstSplitId)
                        .header("Authorization", bearer(token)))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isEqualTo(200));

        mockMvc.perform(post("/api/v1/transaction-splits/{splitId}/confirm", firstSplitId)
                        .header("Authorization", bearer(token)))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isEqualTo(200));

        String dashboardResponseBody = mockMvc.perform(get("/api/v1/dashboard/summary")
                        .header("Authorization", bearer(token)))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isEqualTo(200))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode dashboard = json(dashboardResponseBody);

        assertMoney(dashboard.get("totalCardSpendAmount"), "10000.00");
        assertMoney(dashboard.get("ownerExpenseAmount"), "5000.00");
        assertMoney(dashboard.get("borrowedTransactionAmount"), "10000.00");
        assertMoney(dashboard.get("totalBorrowerShareAmount"), "5000.00");
        assertMoney(dashboard.get("pendingAmount"), "2500.00");
        assertMoney(dashboard.get("confirmedAmount"), "2500.00");
    }

    @Test
    void equalSplitShouldPutRoundingRemainderOnOwner() throws Exception {
        String token = registerAndLogin("alice@example.com", "9876543210");

        String cardId = createCard(token);
        String borrowerOneId = createBorrower(token, "Alex", "9876500000");
        String borrowerTwoId = createBorrower(token, "Marie", "9876500001");

        String responseBody = mockMvc.perform(post("/api/v1/cards/{cardId}/transactions", cardId)
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content("""
                                {
                                  "amount": 100,
                                  "description": "Snacks",
                                  "merchantName": "Cafe",
                                  "transactionDate": "2026-06-06",
                                  "borrowed": true,
                                  "splits": [
                                    {
                                      "borrowerId": "%s"
                                    },
                                    {
                                      "borrowerId": "%s"
                                    }
                                  ]
                                }
                                """.formatted(borrowerOneId, borrowerTwoId)))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isEqualTo(201))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode response = json(responseBody);

        assertMoney(response.get("amount"), "100.00");
        assertMoney(response.get("ownerShareAmount"), "33.34");
        assertMoney(response.get("splits").get(0).get("splitAmount"), "33.33");
        assertMoney(response.get("splits").get(1).get("splitAmount"), "33.33");
    }

    @Test
    void mixedPercentageAndEqualSplitShouldReturnBadRequest() throws Exception {
        String token = registerAndLogin("alice@example.com", "9876543210");

        String cardId = createCard(token);
        String borrowerOneId = createBorrower(token, "Alex", "9876500000");
        String borrowerTwoId = createBorrower(token, "Marie", "9876500001");

        mockMvc.perform(post("/api/v1/cards/{cardId}/transactions", cardId)
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content("""
                                {
                                  "amount": 10000,
                                  "description": "Invalid split",
                                  "merchantName": "Test",
                                  "transactionDate": "2026-06-06",
                                  "borrowed": true,
                                  "splits": [
                                    {
                                      "borrowerId": "%s",
                                      "splitPercentage": 25
                                    },
                                    {
                                      "borrowerId": "%s"
                                    }
                                  ]
                                }
                                """.formatted(borrowerOneId, borrowerTwoId)))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isEqualTo(400));
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

    private void assertMoney(JsonNode actual, String expected) {
        assertThat(actual.decimalValue())
                .isEqualByComparingTo(new BigDecimal(expected));
    }
}