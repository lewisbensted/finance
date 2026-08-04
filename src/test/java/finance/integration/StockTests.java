package finance.integration;

import finance.dtos.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;


import static finance.fixtures.StockFixtures.*;
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

        ResponseEntity<ApiResponse<StockResponseDTO>> response = restTemplate.exchange(
                "/api/prices?symbolsStr=AAPL,MSFT,ORCL",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<ApiResponse<StockResponseDTO>>(){}
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().error());
        assertThat(response.getBody().data().stocks())
                .containsEntry("AAPL", APPLE_STOCK)
                .containsEntry("MSFT", MICROSOFT_STOCK)
                .containsEntry("ORCL", ORACLE_STOCK);
    }
}
