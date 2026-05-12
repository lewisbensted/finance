package finance.dtos;

public record TransactionResultDTO(TransactionRequestDTO transaction, String error) {
}
