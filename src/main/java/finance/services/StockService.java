package finance.services;

import finance.dto.ErrorDTO;
import finance.dto.StockDTO;
import finance.dto.StockResponseDTO;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StockService {

    private final RestTemplate restTemplate;

    public StockService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    String url = "https://finance.cs50.io/quote?symbol={symbol}";

    public StockResponseDTO fetchPrices(String[] symbols) {
        List<StockDTO> stocks = new ArrayList<>(symbols.length);
        Map<String, List<String>> fields = new HashMap<>();

        for (String symbol : symbols) {
            StockDTO price = fetchPrice(symbol, fields);
            if (price != null) stocks.add(price);
        }

        return new StockResponseDTO(stocks, fields.isEmpty() ? null : new ErrorDTO("PARTIAL_FAILURE", String.format("Failed to fetch %s prices", stocks.isEmpty() ? "all" : "some"), fields));
    }

    StockDTO fetchPrice(String symbol, Map<String, List<String>> fields) {
        try {
            ResponseEntity<StockDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null, StockDTO.class, symbol);
            return response.getBody();
        } catch (HttpClientErrorException.BadRequest e) {
            System.out.println("Invalid Symbol: " + symbol);
            fields.put(symbol, List.of("Invalid Symbol"));
            return null;
        } catch (Exception e) {
            System.out.println("Unexpected error for symbol " + symbol + ": " + e.getMessage());
            fields.put(symbol, List.of("Invalid Symbol"));
            return null;
        }
    }
}
