package finance.fixtures;

import finance.dtos.ItemErrorDTO;
import finance.dtos.StockDTO;
import finance.dtos.StockResultDTO;

import java.math.BigDecimal;

import static finance.dtos.ErrorCode.INTERNAL_ERROR;
import static finance.dtos.ErrorCode.NOT_FOUND;

public class StockFixtures {
    public static final StockDTO APPLE_STOCK = new StockDTO("Apple", "AAPL", BigDecimal.valueOf(5));
    public static final StockDTO MICROSOFT_STOCK = new StockDTO("Microsoft", "MSFT", BigDecimal.valueOf(10));
    public static final StockDTO ORACLE_STOCK = new StockDTO("Oracle", "ORCL", BigDecimal.valueOf(15));

    public static final StockResultDTO APPLE_STOCK_RESULT = new StockResultDTO(APPLE_STOCK, null);
    public static final StockResultDTO MICROSOFT_STOCK_RESULT = new StockResultDTO(MICROSOFT_STOCK, null);
    public static final StockResultDTO ORACLE_STOCK_RESULT = new StockResultDTO(ORACLE_STOCK, null);
    public static final StockResultDTO BANANA_STOCK_RESULT = new StockResultDTO(null, new ItemErrorDTO(NOT_FOUND, "Stock symbol not found"));
    public static final StockResultDTO ERROR_STOCK_RESULT = new StockResultDTO(null, new ItemErrorDTO(INTERNAL_ERROR, "Unexpected error"));

}
