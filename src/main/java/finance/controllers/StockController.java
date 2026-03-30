package finance.controllers;

import finance.dto.StockDTO;
import finance.dto.StockResponseDTO;
import finance.services.StockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
        StockResponseDTO response = stockService.fetchPrices(symbols);
        return ResponseEntity.status(200).body(response);
    }
}
