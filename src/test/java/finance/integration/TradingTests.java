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
        testUtils.mockStockPrice(mockServer, new StockDTO("Apple", "AAPL", BigDecimal.valueOf(5)));
        testUtils.mockStockPrice(mockServer, new StockDTO("Microsoft", "MSFT", BigDecimal.valueOf(10)));
        testUtils.mockStockPrice(mockServer, new StockDTO("Oracle", "ORCL", BigDecimal.valueOf(15)));

        testUtils.deposit(BigDecimal.valueOf(100), headers);
        ResponseEntity<TransactionResponseDTO> buyResponse = testUtils.buyShares(
                List.of(
                        new TransactionRequestDTO("AAPL", 2L),
                        new TransactionRequestDTO("MSFT", 2L),
                        new TransactionRequestDTO("ORCL", 2L)
                ), headers);
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

        ResponseEntity<PageResponse<HoldingDTO>> holdingsResponse = testUtils.getHoldings(headers);
        assertNotNull(holdingsResponse.getBody());
        List<HoldingDTO> holdings = holdingsResponse.getBody().content();

        assertEquals(3, holdings.size());
        assertThat(holdings)
                .containsExactlyInAnyOrder(
                        new HoldingDTO("AAPL", "Apple", 2L),
                        new HoldingDTO("MSFT", "Microsoft", 2L),
                        new HoldingDTO("ORCL", "Oracle", 2L)
                );

        ResponseEntity<UserDTO> balanceResponse = testUtils.getBalance(headers);
        assertNotNull(balanceResponse.getBody());
        assertEquals(0, balanceResponse.getBody().balance().compareTo(BigDecimal.valueOf(40)));
    }

    @Test
    void buySharesPartial() {
        testUtils.mockStockError(mockServer, "AAAPL", HttpStatus.BAD_REQUEST);
        testUtils.mockStockPrice(mockServer, new StockDTO("Microsoft", "MSFT", BigDecimal.valueOf(10)));
        testUtils.mockStockPrice(mockServer, new StockDTO("Oracle", "ORCL", BigDecimal.valueOf(15)));

        testUtils.deposit(BigDecimal.valueOf(100), headers);
        ResponseEntity<TransactionResponseDTO> buyResponse = testUtils.buyShares(
                List.of(
                        new TransactionRequestDTO("AAAPL", 2L),
                        new TransactionRequestDTO("MSFT", 2L),
                        new TransactionRequestDTO("ORCL", 2L)
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
                        new HoldingDTO("MSFT", "Microsoft", 2L),
                        new HoldingDTO("ORCL", "Oracle", 2L)
                );

        ResponseEntity<UserDTO> balanceResponse = testUtils.getBalance(headers);
        assertNotNull(balanceResponse.getBody());
        assertEquals(0, balanceResponse.getBody().balance().compareTo(BigDecimal.valueOf(50)));

    }

    @Test
    void sellShares() {
        testUtils.mockStockPrice(mockServer, new StockDTO("Apple", "AAPL", BigDecimal.valueOf(5)), ExpectedCount.times(2));
        testUtils.mockStockPrice(mockServer, new StockDTO("Microsoft", "MSFT", BigDecimal.valueOf(10)), ExpectedCount.times(2));
        testUtils.mockStockPrice(mockServer, new StockDTO("Oracle", "ORCL", BigDecimal.valueOf(15)), ExpectedCount.times(2));

        testUtils.deposit(BigDecimal.valueOf(100), headers);
        testUtils.buyShares(
                List.of(
                        new TransactionRequestDTO("AAPL", 2L),
                        new TransactionRequestDTO("MSFT", 2L),
                        new TransactionRequestDTO("ORCL", 2L)
                ), headers);

        ResponseEntity<TransactionResponseDTO> sellResponse = testUtils.sellShares(List.of(
                new TransactionRequestDTO("AAPL", 1L),
                new TransactionRequestDTO("MSFT", 1L),
                new TransactionRequestDTO("ORCL", 1L)
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
                        new HoldingDTO("AAPL", "Apple", 1L),
                        new HoldingDTO("MSFT", "Microsoft", 1L),
                        new HoldingDTO("ORCL", "Oracle", 1L)
                );

        ResponseEntity<UserDTO> balanceResponse = testUtils.getBalance(headers);
        assertNotNull(balanceResponse.getBody());
        assertEquals(0, balanceResponse.getBody().balance().compareTo(BigDecimal.valueOf(70)));
    }

    @Test
    void sellSharesPartial() {
        testUtils.mockStockPrice(mockServer, new StockDTO("Apple", "AAPL", BigDecimal.valueOf(5)));
        testUtils.mockStockPrice(mockServer, new StockDTO("Microsoft", "MSFT", BigDecimal.valueOf(10)), ExpectedCount.times(2));
        testUtils.mockStockPrice(mockServer, new StockDTO("Oracle", "ORCL", BigDecimal.valueOf(15)), ExpectedCount.times(2));
        testUtils.mockStockError(mockServer, "AAAPL", HttpStatus.BAD_REQUEST);

        testUtils.deposit(BigDecimal.valueOf(100), headers);
        testUtils.buyShares(
                List.of(
                        new TransactionRequestDTO("AAPL", 2L),
                        new TransactionRequestDTO("MSFT", 2L),
                        new TransactionRequestDTO("ORCL", 2L)
                ), headers);

        ResponseEntity<TransactionResponseDTO> sellResponse = testUtils.sellShares(List.of(
                new TransactionRequestDTO("AAAPL", 1L),
                new TransactionRequestDTO("MSFT", 1L),
                new TransactionRequestDTO("ORCL", 3L)
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
                        new HoldingDTO("AAPL", "Apple", 2L),
                        new HoldingDTO("MSFT", "Microsoft", 1L),
                        new HoldingDTO("ORCL", "Oracle", 2L)
                );

        ResponseEntity<UserDTO> balanceResponse = testUtils.getBalance(headers);
        assertNotNull(balanceResponse.getBody());
        assertEquals(0, balanceResponse.getBody().balance().compareTo(BigDecimal.valueOf(50)));
    }
}

