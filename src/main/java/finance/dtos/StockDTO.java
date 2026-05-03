package finance.dtos;

import java.math.BigDecimal;

public record StockDTO(String companyName, String symbol, BigDecimal latestPrice) {
}