package finance.fixtures;

import finance.dtos.TransactionRequestDTO;

public class TradingFixtures {
    public static TransactionRequestDTO appleTransactionRequest (Long quantity) {
        return new TransactionRequestDTO("AAPL", quantity);
    }

    public static TransactionRequestDTO microsoftTransactionRequest (Long quantity) {
        return new TransactionRequestDTO("MSFT", quantity);
    }

    public static TransactionRequestDTO oracleTransactionRequest (Long quantity) {
        return new TransactionRequestDTO("ORCL", quantity);
    }

    public static TransactionRequestDTO bananaTransactionRequest (Long quantity) {
        return new TransactionRequestDTO("BANANA", quantity);
    }
}
