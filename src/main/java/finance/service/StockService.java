package finance.service;

import finance.dto.StockDTO;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class StockService {

    private final RestTemplate restTemplate;

    public StockService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    String url = "https://finance.cs50.io/quote?symbol={symbol}";

    public List<StockDTO> fetchPrices(String[] symbols) {
        List<StockDTO> stocks = new ArrayList<>(symbols.length);

        for (String symbol : symbols) {
            stocks.add(fetchPrice(symbol));
        }

        return stocks;
    }

    StockDTO fetchPrice(String symbol) {
        try {
            ResponseEntity<StockDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null, StockDTO.class, symbol);
            return response.getBody();
        } catch (HttpClientErrorException err) {
            if (err.getStatusCode() == HttpStatus.BAD_REQUEST) {
                System.out.println("Invalid Symbol: " + symbol);
                return new StockDTO(symbol, "Invalid Symbol");  // 400 only
            } else {
                System.out.println("Error: " + err.getMessage());
                return new StockDTO(symbol, err.getMessage());
            }
        } catch (Exception err) {
            System.out.println("Unexpected error: " + err.getMessage());
            return new StockDTO(symbol, "Unexpected Error");
        }
    }
}
