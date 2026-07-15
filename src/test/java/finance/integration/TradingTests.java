package finance.integration;

import finance.dtos.*;
import finance.entities.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;


import static finance.fixtures.HoldingFixtures.*;
import static finance.fixtures.StockFixtures.*;
import static finance.fixtures.TradingFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class TradingTests {
    @Autowired
    private RestTemplate stockRestTemplate;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private HttpHeaders headers;

    private TestUtils testUtils;

    private MockRestServiceServer mockServer;

    private static final List<TransactionRequestDTO> initialBuy = List.of(
            appleTransactionRequest(2L),
            microsoftTransactionRequest(2L),
            oracleTransactionRequest(2L)
    );


    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM users");
        jdbcTemplate.execute("DELETE FROM holdings");
        jdbcTemplate.execute("DELETE FROM transactions");

        testUtils = new TestUtils(restTemplate);
        testUtils.register(testUtils.newUser);
        headers = testUtils.authenticateHeaders(testUtils.login(testUtils.loginUser));

        mockServer = MockRestServiceServer.createServer(stockRestTemplate);
    }

    @Test
    void buyShares() {
        testUtils.mockStockPrice(mockServer, APPLE_STOCK);
        testUtils.mockStockPrice(mockServer, MICROSOFT_STOCK);
        testUtils.mockStockPrice(mockServer, ORACLE_STOCK);

        testUtils.deposit(BigDecimal.valueOf(100), headers);
        ResponseEntity<TransactionResponseDTO> buyResponse = testUtils.buyShares(initialBuy, headers);
        assertEquals(HttpStatus.OK, buyResponse.getStatusCode());

        ResponseEntity<PageResponse<TransactionDTO>> transactionsResponse = testUtils.getTransactions(headers);
        assertNotNull(transactionsResponse.getBody());
        List<TransactionDTO> transactions = transactionsResponse.getBody().content();

        assertEquals(3, transactions.size());
        assertEquals("ORCL", transactions.get(0).symbol());
        assertEquals(TransactionType.BUY, transactions.get(0).transactionType());
        assertEquals("MSFT", transactions.get(1).symbol());
        assertEquals(TransactionType.BUY, transactions.get(1).transactionType());
        assertEquals("AAPL", transactions.get(2).symbol());
        assertEquals(TransactionType.BUY, transactions.get(2).transactionType());

        ResponseEntity<HoldingDTO> holdingResponse = restTemplate.exchange(
                "/api/holding?symbol=AAPL",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                HoldingDTO.class
        );
        assertEquals(appleHoldingDTO(2L), holdingResponse.getBody());

        ResponseEntity<PageResponse<HoldingDTO>> holdingsResponse = testUtils.getHoldings(headers);
        assertNotNull(holdingsResponse.getBody());
        List<HoldingDTO> holdings = holdingsResponse.getBody().content();

        assertEquals(3, holdings.size());
        assertThat(holdings)
                .containsExactlyInAnyOrder(
                        appleHoldingDTO(2L), microsoftHoldingDTO(2L), oracleHoldingDTO(2L)
                );

        ResponseEntity<BigDecimal> balanceResponse = testUtils.getBalance(headers);
        assertNotNull(balanceResponse.getBody());
        assertEquals(0, balanceResponse.getBody().compareTo(BigDecimal.valueOf(40)));
    }

    @Test
    void buySharesPartial() {
        testUtils.mockStockError(mockServer, "AAAPL", HttpStatus.BAD_REQUEST);
        testUtils.mockStockPrice(mockServer, MICROSOFT_STOCK);
        testUtils.mockStockPrice(mockServer, ORACLE_STOCK);

        testUtils.deposit(BigDecimal.valueOf(100), headers);
        ResponseEntity<TransactionResponseDTO> buyResponse = testUtils.buyShares(
                List.of(
                        new TransactionRequestDTO("AAAPL", 2L),
                        microsoftTransactionRequest(2L),
                        oracleTransactionRequest(2L)
                ), headers);
        assertEquals(HttpStatus.OK, buyResponse.getStatusCode());

        ResponseEntity<PageResponse<TransactionDTO>> transactionsResponse = testUtils.getTransactions(headers);
        assertNotNull(transactionsResponse.getBody());
        List<TransactionDTO> transactions = transactionsResponse.getBody().content();

        assertEquals(2, transactions.size());
        assertEquals("ORCL", transactions.get(0).symbol());
        assertEquals(TransactionType.BUY, transactions.get(0).transactionType());
        assertEquals("MSFT", transactions.get(1).symbol());
        assertEquals(TransactionType.BUY, transactions.get(1).transactionType());

        ResponseEntity<PageResponse<HoldingDTO>> holdingsResponse = testUtils.getHoldings(headers);
        assertNotNull(holdingsResponse.getBody());
        List<HoldingDTO> holdings = holdingsResponse.getBody().content();

        assertEquals(2, holdings.size());
        assertThat(holdings)
                .containsExactlyInAnyOrder(
                        microsoftHoldingDTO(2L), oracleHoldingDTO(2L)
                );

        ResponseEntity<BigDecimal> balanceResponse = testUtils.getBalance(headers);
        assertNotNull(balanceResponse.getBody());
        assertEquals(0, balanceResponse.getBody().compareTo(BigDecimal.valueOf(50)));

    }

    @Test
    void sellShares() {
        testUtils.mockStockPrice(mockServer, APPLE_STOCK, ExpectedCount.times(2));
        testUtils.mockStockPrice(mockServer, MICROSOFT_STOCK, ExpectedCount.times(2));
        testUtils.mockStockPrice(mockServer, ORACLE_STOCK, ExpectedCount.times(2));

        testUtils.deposit(BigDecimal.valueOf(100), headers);
        testUtils.buyShares(initialBuy, headers);

        ResponseEntity<TransactionResponseDTO> sellResponse = testUtils.sellShares(List.of(
                appleTransactionRequest(1L), microsoftTransactionRequest(1L), oracleTransactionRequest(1L)
        ), headers);
        assertEquals(HttpStatus.OK, sellResponse.getStatusCode());

        ResponseEntity<PageResponse<TransactionDTO>> transactionsResponse = testUtils.getTransactions(headers);
        assertNotNull(transactionsResponse.getBody());
        List<TransactionDTO> transactions = transactionsResponse.getBody().content();

        assertEquals(6, transactions.size());
        assertEquals("ORCL", transactions.get(0).symbol());
        assertEquals(TransactionType.SELL, transactions.get(0).transactionType());
        assertEquals("MSFT", transactions.get(1).symbol());
        assertEquals(TransactionType.SELL, transactions.get(1).transactionType());
        assertEquals("AAPL", transactions.get(2).symbol());
        assertEquals(TransactionType.SELL, transactions.get(2).transactionType());

        ResponseEntity<PageResponse<HoldingDTO>> holdingsResponse = testUtils.getHoldings(headers);
        assertNotNull(holdingsResponse.getBody());
        List<HoldingDTO> holdings = holdingsResponse.getBody().content();

        assertEquals(3, holdings.size());
        assertThat(holdings)
                .containsExactlyInAnyOrder(
                        appleHoldingDTO(1L), microsoftHoldingDTO(1L), oracleHoldingDTO(1L)
                );

        ResponseEntity<BigDecimal> balanceResponse = testUtils.getBalance(headers);
        assertNotNull(balanceResponse.getBody());
        assertEquals(0, balanceResponse.getBody().compareTo(BigDecimal.valueOf(70)));
    }

    @Test
    void sellSharesPartial() {
        testUtils.mockStockPrice(mockServer, APPLE_STOCK);
        testUtils.mockStockPrice(mockServer, MICROSOFT_STOCK, ExpectedCount.times(2));
        testUtils.mockStockPrice(mockServer, ORACLE_STOCK, ExpectedCount.times(2));
        testUtils.mockStockError(mockServer, "AAAPL", HttpStatus.BAD_REQUEST);

        testUtils.deposit(BigDecimal.valueOf(100), headers);
        testUtils.buyShares(initialBuy, headers);

        ResponseEntity<TransactionResponseDTO> sellResponse = testUtils.sellShares(List.of(
                new TransactionRequestDTO("AAAPL", 1L),
                microsoftTransactionRequest(1L),
                oracleTransactionRequest(3L)
        ), headers);
        assertEquals(HttpStatus.OK, sellResponse.getStatusCode());

        ResponseEntity<PageResponse<TransactionDTO>> transactionsResponse = testUtils.getTransactions(headers);
        assertNotNull(transactionsResponse.getBody());
        List<TransactionDTO> transactions = transactionsResponse.getBody().content();

        assertEquals(4, transactions.size());
        assertEquals("MSFT", transactions.get(0).symbol());
        assertEquals(TransactionType.SELL, transactions.get(0).transactionType());

        ResponseEntity<PageResponse<HoldingDTO>> holdingsResponse = testUtils.getHoldings(headers);
        assertNotNull(holdingsResponse.getBody());
        List<HoldingDTO> holdings = holdingsResponse.getBody().content();

        assertEquals(3, holdings.size());
        assertThat(holdings)
                .containsExactlyInAnyOrder(
                        appleHoldingDTO(2L), microsoftHoldingDTO(1L), oracleHoldingDTO(2L)
                );

        ResponseEntity<BigDecimal> balanceResponse = testUtils.getBalance(headers);
        assertNotNull(balanceResponse.getBody());
        assertEquals(0, balanceResponse.getBody().compareTo(BigDecimal.valueOf(50)));
    }
}

