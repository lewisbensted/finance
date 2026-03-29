package finance.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AmountDTO(
        @NotNull(message = "Amount is required.")
        @Positive(message = "Amount must be greater than zero")
        Double amount) {
}
