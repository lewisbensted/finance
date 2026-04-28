package finance.services;

import finance.dto.StockDTO;
import finance.dto.StockResultDTO;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
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

    public Map<String, StockResultDTO> fetchPrices(String[] symbols) {
        Map<String, StockResultDTO> stockMap = new HashMap<>();

        for (String symbol : symbols) {
            StockResultDTO price = fetchPrice(symbol);
            stockMap.put(symbol, price);
        }

        return stockMap;
    }

    StockResultDTO fetchPrice(String symbol) {
        try {
            ResponseEntity<StockDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null, StockDTO.class, symbol);
            return new StockResultDTO(response.getBody(), null);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                return new StockResultDTO(null, "Invalid Symbol");
            } else {
                return new StockResultDTO(null, "Unexpected Error");
            }
        } catch (Exception e) {
            return new StockResultDTO(null, "Unexpected Error");
        }
    }
}
