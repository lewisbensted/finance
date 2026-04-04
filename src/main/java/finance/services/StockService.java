package finance.services;

import finance.dto.StockDTO;
import finance.dto.StockFetchDTO;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class StockService {

    private final RestTemplate restTemplate;

    public StockService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    String url = "https://finance.cs50.io/quote?symbol={symbol}";

    public Map<String, StockFetchDTO> fetchPrices(String[] symbols) {
        Map<String, StockFetchDTO> stockMap = new HashMap<>();

        for (String symbol : symbols) {
            StockFetchDTO price = fetchPrice(symbol);
            stockMap.put(symbol, price);
        }

        return stockMap;
    }

    StockFetchDTO fetchPrice(String symbol) {
        try {
            ResponseEntity<StockDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null, StockDTO.class, symbol);
            return new StockFetchDTO(response.getBody(), null);
        } catch (HttpClientErrorException.BadRequest e) {
            return new StockFetchDTO(null, "Invalid Symbol");
        } catch (Exception e) {
            return new StockFetchDTO(null, "Unexpected Error");
        }
    }
}
