package finance.dtos;

import java.util.Map;

public record StockResponseDTO(
        Map<String, StockDTO> stocks
) {
}