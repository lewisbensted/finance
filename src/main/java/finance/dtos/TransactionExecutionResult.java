package finance.dtos;

import java.math.BigDecimal;
import java.util.List;

public record TransactionExecutionResult(
        List<TransactionResultDTO> transactions,
        BigDecimal balance) {
}
