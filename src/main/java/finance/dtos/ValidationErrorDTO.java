package finance.dtos;

import java.util.List;
import java.util.Map;

public record ValidationErrorDTO(
        ErrorCode code,
        String message,
        Map<String, List<String>> fields
) {}
