package finance.controllers;

import finance.dto.StockDTO;
import finance.service.StockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@RestController
public class StockController {

    private final StockService stockService;
    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping(value = "/prices")
    ResponseEntity<?> getPrice(@RequestParam(required = false) String symbolsStr) {
        if (symbolsStr == null || symbolsStr.isEmpty()) return ResponseEntity.status(400).body("No symbols provided");
        String[] symbols = symbolsStr.split(",");
        List<StockDTO> stocks = stockService.fetchPrices(symbols);

        if (symbols.length == 1 && stocks.get(0).error() != null)
            return ResponseEntity.status(404).body(stocks.get(0));
        return ResponseEntity.status(200).body(stocks);

    }
}
