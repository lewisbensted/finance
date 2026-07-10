package finance.slice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import finance.controllers.TradingController;
import finance.dtos.*;
import finance.services.TradingService;
import finance.session.SessionUser;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static finance.dtos.ErrorCode.*;
import static finance.entities.TransactionType.BUY;
import static finance.fixtures.TradingFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TradingController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TradingControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TradingService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    SessionUser testSessionUser = new SessionUser(1L, "testuser");

    List<TransactionRequestDTO> requestSuccess = List.of(
            appleTransactionRequest(5L), microsoftTransactionRequest(10L)
    );

    List<TransactionRequestDTO> requestPartial = List.of(
            appleTransactionRequest(5L), microsoftTransactionRequest(10L), bananaTransactionRequest(10L), oracleTransactionRequest(-5L)
    );

    List<TransactionRequestDTO> requestFailure = List.of(
            bananaTransactionRequest(10L), oracleTransactionRequest(-5L)
    );

    TransactionResultDTO appleTransactionResult = new TransactionResultDTO(appleTransactionRequest(5L), null);
    TransactionResultDTO microsoftTransactionResult = new TransactionResultDTO(microsoftTransactionRequest(10L), null);
    TransactionResultDTO bananaTransactionResult = new TransactionResultDTO(bananaTransactionRequest(10L), new ItemErrorDTO(NOT_FOUND, "Stock symbol not found"));
    TransactionResultDTO oracleTransactionResult = new TransactionResultDTO(oracleTransactionRequest(-5L), new ItemErrorDTO(INVALID_REQUEST, "Invalid quantity"));

    @Nested
    class BuyTests {
        @Test
        void test400NullBody() throws Exception {
            mockMvc.perform(post("/api/buy")
                            .contentType(MediaType.APPLICATION_JSON)
                            .sessionAttr("USER_SESSION", testSessionUser))
                    .andExpect(status().is(400))
                    .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                    .andExpect(jsonPath("$.message").value("Empty or unreadable request body"));
        }

        @Test
        void test401Unauthenticated() throws Exception {
            mockMvc.perform(post("/api/buy")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestSuccess)))
                    .andExpect(status().is(401))
                    .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"))
                    .andExpect(jsonPath("$.message").value("Not logged in"));
        }

        @Test
        void test422AllFailed() throws Exception {
            when(transactionService.executeTransactions(any(), any(), any()))
                    .thenReturn(List.of(bananaTransactionResult, oracleTransactionResult));
            mockMvc.perform(post("/api/buy")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestSuccess))
                            .sessionAttr("USER_SESSION", testSessionUser))
                    .andExpect(status().is(422))
                    .andExpect(jsonPath("$.transactions").isEmpty())
                    .andExpect(jsonPath("$.error.code").value("OPERATION_FAILED"))
                    .andExpect(jsonPath("$.error.fields.BANANA.code")
                            .value("NOT_FOUND"))
                    .andExpect(jsonPath("$.error.fields.BANANA.message")
                            .value("Stock symbol not found"))
                    .andExpect(jsonPath("$.error.fields.ORCL.code")
                            .value("INVALID_REQUEST"))
                    .andExpect(jsonPath("$.error.fields.ORCL.message")
                            .value("Invalid quantity"));
        }

        @Test
        void test200PartialFailure() throws Exception {
            when(transactionService.executeTransactions(any(), any(), any()))
                    .thenReturn(List.of(appleTransactionResult, microsoftTransactionResult, bananaTransactionResult, oracleTransactionResult));
            mockMvc.perform(post("/api/buy")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestPartial))
                            .sessionAttr("USER_SESSION", testSessionUser))
                    .andExpect(status().is(200))
                    .andExpect(jsonPath("$.transactions[*].symbol")
                            .value(containsInAnyOrder("AAPL", "MSFT")))
                    .andExpect(jsonPath("$.error.code").value("OPERATION_PARTIALLY_FAILED"))
                    .andExpect(jsonPath("$.error.message")
                            .value("Failed to execute some transactions"))
                    .andExpect(jsonPath("$.error.fields.BANANA.code")
                            .value("NOT_FOUND"))
                    .andExpect(jsonPath("$.error.fields.BANANA.message")
                            .value("Stock symbol not found"))
                    .andExpect(jsonPath("$.error.fields.ORCL.code")
                            .value("INVALID_REQUEST"))
                    .andExpect(jsonPath("$.error.fields.ORCL.message")
                            .value("Invalid quantity"));
        }

        @Test
        void test200Success() throws Exception {
            when(transactionService.executeTransactions(any(), any(), any()))
                    .thenReturn(List.of(appleTransactionResult, microsoftTransactionResult));

            mockMvc.perform(post("/api/buy")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestFailure))
                            .sessionAttr("USER_SESSION", testSessionUser))
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
                            .sessionAttr("USER_SESSION", testSessionUser))
                    .andExpect(status().is(400))
                    .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                    .andExpect(jsonPath("$.message").value("Empty or unreadable request body"));
        }

        @Test
        void test401Unauthenticated() throws Exception {
            mockMvc.perform(post("/api/sell")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestSuccess)))
                    .andExpect(status().is(401))
                    .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"))
                    .andExpect(jsonPath("$.message").value("Not logged in"));
        }

        @Test
        void test422AllFailed() throws Exception {
            when(transactionService.executeTransactions(any(), any(), any()))
                    .thenReturn(List.of(
                            new TransactionResultDTO(appleTransactionRequest(5L), new ItemErrorDTO(INSUFFICIENT_SHARES, "Insufficient shares")),
                            oracleTransactionResult
                    ));
            mockMvc.perform(post("/api/sell")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestSuccess))
                            .sessionAttr("USER_SESSION", testSessionUser))
                    .andExpect(status().is(422))
                    .andExpect(jsonPath("$.transactions").isEmpty())
                    .andExpect(jsonPath("$.error.code").value("OPERATION_FAILED"))
                    .andExpect(jsonPath("$.error.fields.AAPL.code")
                            .value("INSUFFICIENT_SHARES"))
                    .andExpect(jsonPath("$.error.fields.AAPL.message")
                            .value("Insufficient shares"))
                    .andExpect(jsonPath("$.error.fields.ORCL.code")
                            .value("INVALID_REQUEST"))
                    .andExpect(jsonPath("$.error.fields.ORCL.message")
                            .value("Invalid quantity"));
        }

        @Test
        void test200PartialFailure() throws Exception {
            when(transactionService.executeTransactions(any(), any(), any()))
                    .thenReturn(List.of(appleTransactionResult, microsoftTransactionResult, oracleTransactionResult));

            mockMvc.perform(post("/api/sell")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestPartial))
                            .sessionAttr("USER_SESSION", testSessionUser))
                    .andExpect(status().is(200))
                    .andExpect(jsonPath("$.transactions[*].symbol")
                            .value(containsInAnyOrder("AAPL", "MSFT")))
                    .andExpect(jsonPath("$.error.code").value("OPERATION_PARTIALLY_FAILED"))
                    .andExpect(jsonPath("$.error.message")
                            .value("Failed to execute some transactions"))
                    .andExpect(jsonPath("$.error.fields.ORCL.code")
                            .value("INVALID_REQUEST"))
                    .andExpect(jsonPath("$.error.fields.ORCL.message")
                            .value("Invalid quantity"));
        }

        @Test
        void test200Success() throws Exception {
            when(transactionService.executeTransactions(any(), any(), any()))
                    .thenReturn(List.of(appleTransactionResult, microsoftTransactionResult));

            mockMvc.perform(post("/api/sell")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestFailure))
                            .sessionAttr("USER_SESSION", testSessionUser))
                    .andExpect(status().is(200))
                    .andExpect(jsonPath("$.transactions[*].symbol")
                            .value(containsInAnyOrder("AAPL", "MSFT")))
                    .andExpect(jsonPath("$.error").value(Matchers.nullValue()));
        }
    }

    @Nested
    class FetchTransactions {
        @Test
        void test200Success() throws Exception {
            TransactionDTO appleTransaction = new TransactionDTO("AAPL", "Apple", 5L, BigDecimal.valueOf(20), BUY, LocalDateTime.of(2024, 1, 1, 12, 0));
            TransactionDTO microsoftTransaction = new TransactionDTO("MSFT", "Microsoft", 5L, BigDecimal.valueOf(20), BUY, LocalDateTime.of(2024, 1, 1, 12, 1));
            when(transactionService.fetchTransactions(any(), any())).thenReturn(new PageImpl<>(List.of(appleTransaction, microsoftTransaction)));
            ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

            mockMvc.perform(get("/api/transactions?direction=ASC")
                            .contentType(MediaType.APPLICATION_JSON)
                            .sessionAttr("USER_SESSION", testSessionUser))
                    .andExpect(status().is(200))
                    .andExpect(jsonPath("$.content[*].symbol")
                            .value(containsInAnyOrder("AAPL", "MSFT")));

            verify(transactionService).fetchTransactions(
                    eq(testSessionUser.id()),
                    captor.capture()
            );

            Sort.Order order = captor.getValue()
                    .getSort()
                    .getOrderFor("id");

            assertThat(order).isNotNull();
            assertThat(order.getDirection())
                    .isEqualTo(Sort.Direction.ASC);
        }

        @Test
        void test401Unauthenticated() throws Exception {
            mockMvc.perform(get("/api/transactions")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().is(401))
                    .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"))
                    .andExpect(jsonPath("$.message").value("Not logged in"));
        }
    }
}
