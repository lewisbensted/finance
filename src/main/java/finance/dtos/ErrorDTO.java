package finance.dtos;

import java.util.List;
import java.util.Map;

public record ErrorDTO(
        ErrorCode code,
        String message,
        Map<String, ItemErrorDTO> fields
) {
    public ErrorDTO(ErrorCode code, String message) {
        this(code, message, null);
    }
}
