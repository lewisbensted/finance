package finance.integration;

import finance.dtos.*;
import finance.entities.User;
import finance.services.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

import static finance.StockFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class StockTests {
    @Autowired
    private RestTemplate stockRestTemplate;

    @Autowired
    private TestRestTemplate restTemplate;

    private TestUtils testUtils;
    private MockRestServiceServer mockServer;


    @BeforeEach
    void setUp() {
        testUtils = new TestUtils(restTemplate);
        mockServer = MockRestServiceServer.createServer(stockRestTemplate);
    }

    @Test
    void callsExternalAPI() {
        testUtils.mockStockPrice(mockServer, APPLE_STOCK);
        testUtils.mockStockPrice(mockServer, MICROSOFT_STOCK);
        testUtils.mockStockPrice(mockServer, ORACLE_STOCK);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<StockResponseDTO> response = restTemplate.exchange(
                "/api/prices?symbolsStr=AAPL,MSFT,ORCL",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                StockResponseDTO.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertThat(response.getBody().stocks())
                .containsExactlyInAnyOrder(APPLE_STOCK, MICROSOFT_STOCK, ORACLE_STOCK
                );
    }
}
