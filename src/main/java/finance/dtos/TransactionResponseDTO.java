package finance.dtos;

import java.math.BigDecimal;
import java.util.Map;

public record TransactionResponseDTO(
                Map<String, TransactionRequestDTO> transactions,
                BigDecimal balance) {
}