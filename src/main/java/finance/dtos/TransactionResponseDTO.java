package finance.dtos;

import java.util.List;

public record TransactionResponseDTO (
        List<TransactionRequestDTO> transactions,
        ErrorDTO error
) {
}