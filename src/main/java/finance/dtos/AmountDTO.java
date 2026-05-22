package finance.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record AmountDTO(
        @NotNull(message = "Amount must be positive")
        @Positive(message = "Amount must be positive")
        BigDecimal amount
) {
}
