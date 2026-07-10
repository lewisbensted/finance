package finance.unit.services;

import finance.dtos.ItemErrorDTO;
import finance.dtos.StockDTO;
import finance.dtos.StockResultDTO;
import finance.services.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

import static finance.StockFixtures.APPLE_STOCK;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StockServiceTests {

    private StockService stockService;

    @BeforeEach
    void setUp() {
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        stockService = new StockService(mockRestTemplate);

        when(mockRestTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), eq(StockDTO.class), eq("AAPL"))).thenReturn(ResponseEntity.ok(APPLE_STOCK));
        when(mockRestTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), eq(StockDTO.class), eq("BANANA"))).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request"));
        when(mockRestTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), eq(StockDTO.class), eq("MSFT"))).thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error"));
        when(mockRestTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), eq(StockDTO.class), eq("ORCL"))).thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Other Error"));
    }

    @Nested
    class FetchPriceTests {
        @Test
        void testSuccess() {
            assertEquals(new StockResultDTO(APPLE_STOCK, null), stockService.fetchPrice("AAPL"));
        }

        @Test
        void testBadReq() {
            assertEquals(new StockResultDTO(null, new ItemErrorDTO("NOT_FOUND", "Stock symbol not found")), stockService.fetchPrice("BANANA"));
        }

        @Test
        void testServerError() {
            assertEquals(new StockResultDTO(null, new ItemErrorDTO("INTERNAL_ERROR", "Unexpected error")), stockService.fetchPrice("MSFT"));
        }

        @Test
        void testOtherError() {
            assertEquals(new StockResultDTO(null, new ItemErrorDTO("INTERNAL_ERROR", "Unexpected error")), stockService.fetchPrice("ORCL"));
        }
    }

    @Test
    void testFetchPrices() {
        Map<String, StockResultDTO> result = stockService.fetchPrices(new String[]{"AAPL ", "BANANA", "MSFT", "  "});
        assertEquals(3, result.size());
        assertTrue(result.containsKey("AAPL"));
        assertTrue(result.containsKey("BANANA"));
        assertTrue(result.containsKey("MSFT"));
    }
}
