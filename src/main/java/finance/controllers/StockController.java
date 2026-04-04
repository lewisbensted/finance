package finance.controllers;

import finance.dto.ErrorDTO;
import finance.dto.StockDTO;
import finance.dto.StockFetchDTO;
import finance.dto.StockResponseDTO;
import finance.services.StockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping(value = "/api/prices")
    ResponseEntity<?> getPrice(@RequestParam(required = false) String symbolsStr) {
        if (symbolsStr == null || symbolsStr.isEmpty()) return ResponseEntity.status(400).body("No symbols provided.");
        String[] symbols = symbolsStr.split(",");

        Map<String, List<String>> errorFields = new HashMap<>();
        List<StockDTO> stocks = new ArrayList<>();

        Map<String, StockFetchDTO> prices = stockService.fetchPrices(symbols);

        for (Map.Entry<String, StockFetchDTO> entry : prices.entrySet()) {
            String symbol = entry.getKey();
            StockFetchDTO fetch = entry.getValue();
            if (fetch.error() != null) errorFields.put(symbol, List.of(fetch.error()));
            else stocks.add(fetch.stock());
        }

        boolean allFailed = stocks.isEmpty();
        return ResponseEntity.status(allFailed ? 422 : 200).body(new StockResponseDTO(stocks, errorFields.isEmpty() ? null : new ErrorDTO("PARTIAL_FAILURE", String.format("Failed to fetch %s prices", allFailed ? "all" : "some"), errorFields)));
    }
}
