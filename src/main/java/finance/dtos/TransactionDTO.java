package finance.dtos;

import finance.entities.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionDTO(String symbol, String companyName, Long shares, BigDecimal price,
                             TransactionType transactionType, LocalDateTime createdAt) {
    public TransactionDTO {
        price = price == null ? null : price.stripTrailingZeros();
    }
}
