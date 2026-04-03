package finance.dto;

import java.util.Optional;

public record StockFetchDTO (StockDTO stock, String error) {
}
