package finance.controllers;

import finance.dtos.*;
import finance.services.StockService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import static finance.dtos.ErrorCode.*;

@RestController
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping(value = "/api/prices")
    ResponseEntity<ApiResponse<StockResponseDTO>> getPrice(@RequestParam(required = false) String symbolsStr) {
        if (symbolsStr == null || symbolsStr.isEmpty())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(null, new ErrorDTO(INVALID_REQUEST, "No symbols provided")));
        String[] symbols = symbolsStr.split(",");

        Map<String, StockDTO> stocks = new HashMap<>();
        Map<String, ItemErrorDTO> errors = new HashMap<>();

        Map<String, StockResultDTO> prices = stockService.fetchPrices(symbols);

        for (Map.Entry<String, StockResultDTO> entry : prices.entrySet()) {
            String symbol = entry.getKey();
            StockResultDTO fetch = entry.getValue();
            if (fetch.error() != null)
                errors.put(symbol, fetch.error());
            else
                stocks.put(symbol, fetch.stock());
        }

        boolean allFailed = stocks.isEmpty();
        return ResponseEntity.status(allFailed ? HttpStatus.UNPROCESSABLE_ENTITY : HttpStatus.OK)
                .body(new ApiResponse<>(new StockResponseDTO(stocks),
                        errors.isEmpty() ? null
                                : new ErrorDTO(allFailed ? OPERATION_FAILED : OPERATION_PARTIALLY_FAILED,
                                        String.format("Failed to fetch %s prices", allFailed ? "all" : "some"),
                                        errors)));
    }
}
