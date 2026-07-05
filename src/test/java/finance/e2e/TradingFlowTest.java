package finance.e2e;

import finance.dtos.*;
import finance.entities.TransactionType;
import finance.services.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class TradingFlowTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private HttpHeaders headers;

    @MockBean
    private StockService stockService;


    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM users");
        jdbcTemplate.execute("DELETE FROM holdings");
        jdbcTemplate.execute("DELETE FROM transactions");

        TestUtils testUtils = new TestUtils(restTemplate);
        testUtils.register(testUtils.newUser);
        headers = testUtils.authenticateHeaders(testUtils.login(testUtils.loginUser));

        when(stockService.fetchPrices(any()))
                .thenReturn(Map.of(
                        "AAPL", new StockResultDTO(new StockDTO("Apple", "AAPL", BigDecimal.valueOf(5)), null),
                        "MSFT", new StockResultDTO(new StockDTO("Microsoft", "MSFT", BigDecimal.valueOf(10)), null),
                        "ORCL", new StockResultDTO(new StockDTO("Oracle", "ORCL", BigDecimal.valueOf(15)), null)
                ));
    }

    @Test
    void buyAndSell() {
        restTemplate.exchange(
                "/api/deposit",
                HttpMethod.POST,
                new HttpEntity<>(new AmountDTO(BigDecimal.valueOf(100)), headers),
                UserDTO.class
        );

        ResponseEntity<TransactionResponseDTO> buyResponse = restTemplate.exchange(
                "/api/buy",
                HttpMethod.POST,
                new HttpEntity<>(
                        List.of(
                                new TransactionRequestDTO("AAPL", 2L),
                                new TransactionRequestDTO("MSFT", 2L),
                                new TransactionRequestDTO("ORCL", 2L)
                        ), headers),
                TransactionResponseDTO.class
        );
        assertEquals(HttpStatus.OK, buyResponse.getStatusCode());

        ResponseEntity<TransactionResponseDTO> sellResponse = restTemplate.exchange(
                "/api/sell",
                HttpMethod.POST,
                new HttpEntity<>(
                        List.of(
                                new TransactionRequestDTO("AAPL", 1L),
                                new TransactionRequestDTO("MSFT", 1L),
                                new TransactionRequestDTO("ORCL", 1L)
                        ), headers),
                TransactionResponseDTO.class
        );
        assertEquals(HttpStatus.OK, sellResponse.getStatusCode());

        ResponseEntity<PageResponse<TransactionDTO>> transactionsResponse = restTemplate.exchange(
                "/api/transactions",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {}
        );
        assertNotNull(transactionsResponse.getBody());
        List<TransactionDTO> transactions = transactionsResponse.getBody().content();

        assertEquals(6, transactions.size());
        assertEquals(new TransactionDTO("ORCL", "Oracle", 1L, BigDecimal.valueOf(15), TransactionType.SELL, transactions.get(0).createdAt()), transactions.get(0));
        assertEquals(new TransactionDTO("MSFT", "Microsoft", 1L, BigDecimal.valueOf(10), TransactionType.SELL, transactions.get(1).createdAt()), transactions.get(1));
        assertEquals(new TransactionDTO("AAPL", "Apple", 1L, BigDecimal.valueOf(5), TransactionType.SELL, transactions.get(2).createdAt()), transactions.get(2));
        assertEquals(new TransactionDTO("ORCL", "Oracle", 2L, BigDecimal.valueOf(15), TransactionType.BUY, transactions.get(3).createdAt()), transactions.get(3));
        assertEquals(new TransactionDTO("MSFT", "Microsoft", 2L, BigDecimal.valueOf(10), TransactionType.BUY, transactions.get(4).createdAt()), transactions.get(4));
        assertEquals(new TransactionDTO("AAPL", "Apple", 2L, BigDecimal.valueOf(5), TransactionType.BUY, transactions.get(5).createdAt()), transactions.get(5));

        ResponseEntity<PageResponse<HoldingDTO>> holdingsResponse = restTemplate.exchange(
                "/api/holdings",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {}
        );
        assertNotNull(holdingsResponse.getBody());
        List<HoldingDTO> holdings = holdingsResponse.getBody().content();

        assertEquals(3, holdings.size());
        assertEquals(new HoldingDTO("AAPL", "Apple", 1L), holdings.get(0));
        assertEquals(new HoldingDTO("MSFT", "Microsoft", 1L), holdings.get(1));
        assertEquals(new HoldingDTO("ORCL", "Oracle", 1L), holdings.get(2));
    }
}
