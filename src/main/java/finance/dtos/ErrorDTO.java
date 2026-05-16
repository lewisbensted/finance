package finance.dtos;

import java.util.List;
import java.util.Map;

public record ErrorDTO(
        String code,
        String message,
        Map<String, List<ItemErrorDTO>> fields
) {
    public ErrorDTO(String code, String message) {
        this(code, message, null);
    }
}
