package finance.dto;

import java.util.List;

public record TransactionResponseDTO (
        List<TransactionDTO> transactions,
        ErrorDTO error
) {
}