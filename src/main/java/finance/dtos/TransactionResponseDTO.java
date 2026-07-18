package finance.dtos;

import java.util.Map;

public record TransactionResponseDTO (
        Map<String, TransactionRequestDTO> transactions,
        ErrorDTO error
) {
}