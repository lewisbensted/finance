package finance.dtos;

public record TransactionResultDTO(TransactionRequestDTO transaction, ItemErrorDTO error) {
}
