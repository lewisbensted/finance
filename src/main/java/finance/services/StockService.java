package finance.services;

import finance.dtos.ItemErrorDTO;
import finance.dtos.StockDTO;
import finance.dtos.StockResultDTO;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static finance.dtos.ErrorCode.INTERNAL_ERROR;
import static finance.dtos.ErrorCode.NOT_FOUND;

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
            String trimmed = symbol.trim();
            if (trimmed.isEmpty()) continue;
            StockResultDTO price = fetchPrice(trimmed);
            stockMap.put(trimmed, price);
        }

        return stockMap;
    }

    public StockResultDTO fetchPrice(String symbol) {
        try {
            ResponseEntity<StockDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null, StockDTO.class, symbol);
            return new StockResultDTO(response.getBody(), null);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                return new StockResultDTO(null, new ItemErrorDTO(NOT_FOUND, "Stock symbol not found"));
            } else {
                return new StockResultDTO(null, new ItemErrorDTO(INTERNAL_ERROR, "Unexpected error"));
            }
        } catch (Exception e) {
            return new StockResultDTO(null, new ItemErrorDTO(INTERNAL_ERROR, "Unexpected error"));
        }
    }
}
