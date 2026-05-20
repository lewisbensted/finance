package finance.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import finance.dtos.ItemErrorDTO;
import finance.dtos.TransactionRequestDTO;
import finance.dtos.TransactionResultDTO;
import finance.entities.User;
import finance.services.TransactionService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TransactionControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    User testUser = new User(
            "testuser",
            "testuser@test.com",
            "test",
            "user",
            "password_hash"
    );

    TransactionRequestDTO appleTransactionRequest = new TransactionRequestDTO("AAPL", 5L);
    TransactionRequestDTO microsoftTransactionRequest = new TransactionRequestDTO("MSFT", 10L);
    TransactionRequestDTO bananaTransactionRequest = new TransactionRequestDTO("BANANA", 10L);
    TransactionRequestDTO invalidTransactionRequest = new TransactionRequestDTO("ORCL", -5L);

    List<TransactionRequestDTO> requestSuccess = List.of(
            appleTransactionRequest, microsoftTransactionRequest
    );

    List<TransactionRequestDTO> requestPartial = List.of(
            appleTransactionRequest, microsoftTransactionRequest, bananaTransactionRequest, invalidTransactionRequest
    );

    List<TransactionRequestDTO> requestFailure = List.of(
            bananaTransactionRequest, invalidTransactionRequest
    );


    @Nested
    class BuyTests {
        @Test
        void test400NullBody() throws Exception {
            mockMvc.perform(post("/api/buy")
                            .contentType(MediaType.APPLICATION_JSON)
                            .sessionAttr("USER_SESSION", testUser))
                    .andExpect(status().is(400))
                    .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("Empty or unreadable request body"));
        }

        @Test
        void test401Unauthorised() throws Exception {
            mockMvc.perform(post("/api/buy")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestSuccess)))
                    .andExpect(status().is(401))
                    .andExpect(jsonPath("$.code").value("UNAUTHORISED"))
                    .andExpect(jsonPath("$.message").value("Not logged in"));
        }

        @Test
        void test422AllFailed() throws Exception {
            when(transactionService.executeTransactions(any(), any(), any()))
                    .thenReturn(List.of(
                            new TransactionResultDTO(bananaTransactionRequest, new ItemErrorDTO("NOT_FOUND", "Stock symbol not found")),
                            new TransactionResultDTO(invalidTransactionRequest, new ItemErrorDTO("BAD_REQUEST", "Invalid quantity"))
                    ));
            mockMvc.perform(post("/api/buy")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestSuccess))
                            .sessionAttr("USER_SESSION", testUser))
                    .andExpect(status().is(422))
                    .andExpect(jsonPath("$.transactions").isEmpty())
                    .andExpect(jsonPath("$.error.code").value("UNPROCESSABLE"))
                    .andExpect(jsonPath("$.error.fields.BANANA.code")
                            .value("NOT_FOUND"))
                    .andExpect(jsonPath("$.error.fields.BANANA.message")
                            .value("Stock symbol not found"))
                    .andExpect(jsonPath("$.error.fields.ORCL.code")
                            .value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.error.fields.ORCL.message")
                            .value("Invalid quantity"));
        }

        @Test
        void test200PartialFailure() throws Exception {
            when(transactionService.executeTransactions(any(), any(), any()))
                    .thenReturn(List.of(
                            new TransactionResultDTO(appleTransactionRequest, null),
                            new TransactionResultDTO(microsoftTransactionRequest, null),
                            new TransactionResultDTO(bananaTransactionRequest, new ItemErrorDTO("NOT_FOUND", "Stock symbol not found")),
                            new TransactionResultDTO(invalidTransactionRequest, new ItemErrorDTO("BAD_REQUEST", "Invalid quantity"))
                    ));

            mockMvc.perform(post("/api/buy")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestPartial))
                            .sessionAttr("USER_SESSION", testUser))
                    .andExpect(status().is(200))
                    .andExpect(jsonPath("$.transactions[*].symbol")
                            .value(containsInAnyOrder("AAPL", "MSFT")))
                    .andExpect(jsonPath("$.error.code").value("PARTIAL_FAILURE"))
                    .andExpect(jsonPath("$.error.message")
                            .value("Failed to execute some transactions"))
                    .andExpect(jsonPath("$.error.fields.BANANA.code")
                            .value("NOT_FOUND"))
                    .andExpect(jsonPath("$.error.fields.BANANA.message")
                            .value("Stock symbol not found"))
                    .andExpect(jsonPath("$.error.fields.ORCL.code")
                            .value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.error.fields.ORCL.message")
                            .value("Invalid quantity"));
        }

        @Test
        void test200Success() throws Exception {
            when(transactionService.executeTransactions(any(), any(), any()))
                    .thenReturn(List.of(
                            new TransactionResultDTO(appleTransactionRequest, null),
                            new TransactionResultDTO(microsoftTransactionRequest, null)
                    ));

            mockMvc.perform(post("/api/buy")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestFailure))
                            .sessionAttr("USER_SESSION", testUser))
                    .andExpect(status().is(200))
                    .andExpect(jsonPath("$.transactions[*].symbol")
                            .value(containsInAnyOrder("AAPL", "MSFT")))
                    .andExpect(jsonPath("$.error").value(Matchers.nullValue()));
        }
    }

    @Nested
    class SellTests {
        @Test
        void test400NullBody() throws Exception {
            mockMvc.perform(post("/api/sell")
                            .contentType(MediaType.APPLICATION_JSON)
                            .sessionAttr("USER_SESSION", testUser))
                    .andExpect(status().is(400))
                    .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("Empty or unreadable request body"));
        }

        @Test
        void test401Unauthorised() throws Exception {
            mockMvc.perform(post("/api/sell")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestSuccess)))
                    .andExpect(status().is(401))
                    .andExpect(jsonPath("$.code").value("UNAUTHORISED"))
                    .andExpect(jsonPath("$.message").value("Not logged in"));
        }

        @Test
        void test422AllFailed() throws Exception {
            when(transactionService.executeTransactions(any(), any(), any()))
                    .thenReturn(List.of(
                            new TransactionResultDTO(appleTransactionRequest, new ItemErrorDTO("UNPROCESSABLE", "Insufficient shares")),
                            new TransactionResultDTO(invalidTransactionRequest, new ItemErrorDTO("BAD_REQUEST", "Invalid quantity"))
                    ));
            mockMvc.perform(post("/api/sell")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestSuccess))
                            .sessionAttr("USER_SESSION", testUser))
                    .andExpect(status().is(422))
                    .andExpect(jsonPath("$.transactions").isEmpty())
                    .andExpect(jsonPath("$.error.code").value("UNPROCESSABLE"))
                    .andExpect(jsonPath("$.error.fields.AAPL.code")
                            .value("UNPROCESSABLE"))
                    .andExpect(jsonPath("$.error.fields.AAPL.message")
                            .value("Insufficient shares"))
                    .andExpect(jsonPath("$.error.fields.ORCL.code")
                            .value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.error.fields.ORCL.message")
                            .value("Invalid quantity"));
        }

        @Test
        void test200PartialFailure() throws Exception {
            when(transactionService.executeTransactions(any(), any(), any()))
                    .thenReturn(List.of(
                            new TransactionResultDTO(appleTransactionRequest, null),
                            new TransactionResultDTO(microsoftTransactionRequest, null),
                            new TransactionResultDTO(invalidTransactionRequest, new ItemErrorDTO("BAD_REQUEST", "Invalid quantity"))
                    ));

            mockMvc.perform(post("/api/sell")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestPartial))
                            .sessionAttr("USER_SESSION", testUser))
                    .andExpect(status().is(200))
                    .andExpect(jsonPath("$.transactions[*].symbol")
                            .value(containsInAnyOrder("AAPL", "MSFT")))
                    .andExpect(jsonPath("$.error.code").value("PARTIAL_FAILURE"))
                    .andExpect(jsonPath("$.error.message")
                            .value("Failed to execute some transactions"))
                    .andExpect(jsonPath("$.error.fields.ORCL.code")
                            .value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.error.fields.ORCL.message")
                            .value("Invalid quantity"));
        }

        @Test
        void test200Success() throws Exception {
            when(transactionService.executeTransactions(any(), any(), any()))
                    .thenReturn(List.of(
                            new TransactionResultDTO(appleTransactionRequest, null),
                            new TransactionResultDTO(microsoftTransactionRequest, null)
                    ));

            mockMvc.perform(post("/api/sell")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestFailure))
                            .sessionAttr("USER_SESSION", testUser))
                    .andExpect(status().is(200))
                    .andExpect(jsonPath("$.transactions[*].symbol")
                            .value(containsInAnyOrder("AAPL", "MSFT")))
                    .andExpect(jsonPath("$.error").value(Matchers.nullValue()));
        }
    }
}
