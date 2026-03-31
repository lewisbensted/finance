package finance.dto;

import java.util.List;

public record StockResponseDTO(
        List<StockDTO> stocks,
        ErrorDTO error
) {
}