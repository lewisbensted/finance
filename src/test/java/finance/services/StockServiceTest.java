package finance.services;

import finance.dto.StockDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StockServiceTest {

    private StockService stockService;
    private StockService spiedService;

//    StockDTO apple = new StockDTO("Apple", "AAPL", 150.0, null);
//    StockDTO banana = new StockDTO("Banana", "BAAN", 160.0, null);
//    StockDTO pear = new StockDTO("PEAR", "Invalid Symbol");
//    StockDTO err1 = new StockDTO("ERR1", "404 Not Found");
//    StockDTO err2 = new StockDTO("ERR2", "Unexpected Error");
//
//    @BeforeEach
//    void setUp() {
//        RestTemplate mockRestTemplate = mock(RestTemplate.class);
//        stockService = new StockService(mockRestTemplate);
//
//        spiedService = spy(stockService);
//        doReturn(apple).when(spiedService).fetchPrice("AAPL");
//        doReturn(banana).when(spiedService).fetchPrice("BAAN");
//        doReturn(pear).when(spiedService).fetchPrice("PEAR");
//
//        when(mockRestTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), eq(StockDTO.class), eq("AAPL"))).thenReturn(ResponseEntity.ok(apple));
//        when(mockRestTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), eq(StockDTO.class), eq("PEAR"))).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request"));
//        when(mockRestTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), eq(StockDTO.class), eq("ERR2"))).thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error"));
//        when(mockRestTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), eq(StockDTO.class), eq("ERR1"))).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not Found"));
//    }
//
//    @Test
//    void testFetchPrices() {
//        List<StockDTO> result = spiedService.fetchPrices(new String[]{"AAPL", "BAAN", "PEAR"});
//        assertEquals(3, result.size());
//        assertEquals(apple, result.get(0));
//        assertEquals(banana, result.get(1));
//        assertEquals(pear, result.get(2));
//    }
//
//    @Test
//    void testFetchPriceValid () {
//        assertEquals(apple, stockService.fetchPrice("AAPL"));
//    }
//
//    @Test
//    void testFetchPriceInvalid () {
//        assertEquals(pear, stockService.fetchPrice("PEAR"));
//    }
//
//    @Test
//    void testFetchPriceError1 () {
//        assertEquals(err1, stockService.fetchPrice("ERR1"));
//    }
//
//    @Test
//    void testFetchPriceError2 () {
//        assertEquals(err2, stockService.fetchPrice("ERR2"));
//    }
}
