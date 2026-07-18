package finance.dtos;

public record ApiResponse<T>(
    T data,
    ErrorDTO error
) {}
