package com.paybackpal.backend.auth;

import com.paybackpal.backend.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;


public class AuthApiIntegrationTest extends BaseIntegrationTest {

    @Test
    void registerShouldCreateUserWithoutReturningPasswordHash() throws Exception {
        String requestBody = """
                {
                    "name": "Alice Bob",
                    "email": "alice@example.com",
                    "phoneNumber": "9876543210",
                    "upiId": "alice@upi",
                    "password": "strongPassword123"
                }
                """;

        String responseBody = mockMvc.perform(post("/api/v1/auth/register")
                .contentType("application/json")
                .content(requestBody))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isEqualTo(201))
                .andReturn()
                .getResponse().getContentAsString();

        JsonNode response = json(responseBody);

        assertThat(response.get("id").asText()).isNotBlank();
        assertThat(response.get("email").asText()).isEqualTo("alice@example.com");
        assertThat(response.has("password")).isFalse();
        assertThat(response.has("passwordHash")).isFalse();

    }
    
        @Test
    void registerShouldRejectDuplicateEmail() throws Exception {
        registerUser("alice@example.com", "9876543210");

        String duplicateRequestBody = """
                {
                  "name": "alice Duplicate",
                  "email": "alice@example.com",
                  "phoneNumber": "9876543211",
                  "upiId": "alice2@upi",
                  "password": "strongPassword123"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(duplicateRequestBody))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isEqualTo(409));
    }

    @Test
    void loginShouldReturnJwtToken() throws Exception {
        registerUser("alice@example.com", "9876543210");

        String requestBody = """
                {
                  "email": "alice@example.com",
                  "password": "strongPassword123"
                }
                """;

        String responseBody = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isEqualTo(200))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode response = json(responseBody);

        assertThat(response.get("accessToken").asText()).isNotBlank();
        assertThat(response.get("tokenType").asText()).isEqualTo("Bearer");
        assertThat(response.get("email").asText()).isEqualTo("alice@example.com");
    }

    @Test
    void loginShouldRejectWrongPassword() throws Exception {
        registerUser("alice@example.com", "9876543210");
        String requestBody = """
                {
                  "email": "alice@example.com",
                  "password": "wrongPassword"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isEqualTo(401));
    }

    @Test
    void protectedApiShouldRejectMissingJwt() throws Exception {
        mockMvc.perform(get("/api/v1/cards"))
                .andExpect(result ->
                        assertThat(result.getResponse().getStatus()).isIn(401, 403)
                );
    }
}
