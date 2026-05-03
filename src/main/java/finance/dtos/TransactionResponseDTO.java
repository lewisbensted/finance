package finance.dtos;

import java.util.List;

public record TransactionResponseDTO (
        List<TransactionDTO> transactions,
        ErrorDTO error
) {
}