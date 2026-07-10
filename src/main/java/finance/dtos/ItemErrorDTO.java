package finance.dtos;

public record ItemErrorDTO(
        ErrorCode code,
        String message
) {
}

