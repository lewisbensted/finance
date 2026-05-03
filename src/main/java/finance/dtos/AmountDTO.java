package finance.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record AmountDTO(
        @NotNull(message = "Amount is required.")
        @Positive(message = "Amount must be greater than zero")
        BigDecimal amount) {
}
