package finance.services;

import finance.dto.StockDTO;
import finance.dto.StockResultDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StockServiceTest {

    private StockService stockService;
    private StockService spiedService;

    StockDTO apple = new StockDTO("Apple", "AAPL", new BigDecimal("150.0"));

    @BeforeEach
    void setUp() {
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        stockService = new StockService(mockRestTemplate);

        when(mockRestTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), eq(StockDTO.class), eq("AAPL"))).thenReturn(ResponseEntity.ok(apple));
        when(mockRestTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), eq(StockDTO.class), eq("BANANA"))).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request"));
        when(mockRestTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), eq(StockDTO.class), eq("MSFT"))).thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error"));
    }

    @Test
    void testFetchPrices() {
        Map<String, StockResultDTO> result = stockService.fetchPrices(new String[]{"AAPL", "BANANA", "MSFT"});
        assertEquals(3, result.size());
        assertEquals(new StockResultDTO(apple, null), result.get("AAPL"));
        assertEquals(new StockResultDTO(null, "Invalid Symbol"), result.get("BANANA"));
        assertEquals(new StockResultDTO(null, "Unexpected Error"), result.get("MSFT"));
    }
}
