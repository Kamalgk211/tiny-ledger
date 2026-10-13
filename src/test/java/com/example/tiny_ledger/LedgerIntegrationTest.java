
package com.example.tiny_ledger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LedgerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateAccountDepositWithdrawAndRetrieveHistory()
            throws Exception {

        // 1. Create account
        String userId = UUID.randomUUID().toString();

        MvcResult createResult = mockMvc.perform(
                        post("/api/v1/accounts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                  "userId": "%s",
                                  "currency": "GBP"
                                }
                                """.formatted(userId))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").exists())
                .andExpect(jsonPath("$.balance").value(0))
                .andExpect(jsonPath("$.currency").value("GBP"))
                .andReturn();

        JsonNode accountResponse = objectMapper.readTree(
                createResult.getResponse().getContentAsString());

        String accountId = accountResponse.get("accountId").asText();

        // 2. Deposit £100
        mockMvc.perform(
                        post("/api/v1/accounts/{accountId}/deposits", accountId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                  "amount": 100.00,
                                  "currency": "GBP",
                                  "description": "Initial deposit"
                                }
                                """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("CREDIT"))
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.currency").value("GBP"));

        // 3. Withdraw £25
        mockMvc.perform(
                        post("/api/v1/accounts/{accountId}/withdrawals", accountId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                  "amount": 25.00,
                                  "currency": "GBP",
                                  "description": "Cash withdrawal"
                                }
                                """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("DEBIT"))
                .andExpect(jsonPath("$.amount").value(25.00));

        // 4. Verify final balance is £75
        mockMvc.perform(
                        get("/api/v1/accounts/{accountId}/balance", accountId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(75.00))
                .andExpect(jsonPath("$.currency").value("GBP"));

        // 5. Verify both transactions exist, newest first
        mockMvc.perform(
                        get("/api/v1/accounts/{accountId}/transactions", accountId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].type").value("DEBIT"))
                .andExpect(jsonPath("$[1].type").value("CREDIT"));
    }

    @Test
    void shouldReturn404WhenAccountDoesNotExist() throws Exception {

        String unknownAccountId = UUID.randomUUID().toString();

        mockMvc.perform(
                        get("/api/v1/accounts/{accountId}/balance",
                                unknownAccountId)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }
}
