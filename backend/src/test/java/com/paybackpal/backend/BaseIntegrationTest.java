package com.paybackpal.backend;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BaseIntegrationTest {
    private static final String DEFAULT_PASSWORD = "strongPassword123";

    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("paybackpal_test")
            .withUsername("test_user")
            .withPassword("test_password");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("""
                TRUNCATE TABLE
                    borrower_action_tokens,
                    notification_outbox,
                    transaction_splits,
                    card_transactions,
                    borrowers,
                    credit_cards,
                    users
                CASCADE
                """);
    }

    protected String registerAndLogin(String email, String phoneNumber) throws Exception  {
        registerUser(email, phoneNumber);
        return login(email);
    }

    protected void registerUser(String email, String phoneNumber) throws Exception {
                String requestBody = """
                {
                  "name": "Alice Bob",
                  "email": "%s",
                  "phoneNumber": "%s",
                  "upiId": "alice@upi",
                  "password": "%s"
                }
                """.formatted(email, phoneNumber, DEFAULT_PASSWORD);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isCreated());
    }

    protected String login(String email) throws Exception {
        String requestBody = """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(email, DEFAULT_PASSWORD);

        String responseBody = mockMvc.perform(post("/api/v1/auth/login")
                .contentType("application/json")
                .content(requestBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(responseBody)
                .get("accessToken")
                .asText();
    }

    protected String bearer(String token) {
        return "Bearer " + token;
    }

    protected JsonNode json(String responseBody) throws Exception {
        return objectMapper.readTree(responseBody);
    }

}
