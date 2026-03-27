package finance.dto;

import java.util.List;
import java.util.Map;

public record ErrorDTO(
        String code,
        String message,
        Map<String, List<String>> fields
) {
    public ErrorDTO(String code, String message) {
        this(code, message, null);
    }


}
