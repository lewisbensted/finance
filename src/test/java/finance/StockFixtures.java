package finance;

import finance.dtos.StockDTO;

import java.math.BigDecimal;

public class StockFixtures {

    public static final StockDTO APPLE_STOCK = new StockDTO("Apple", "AAPL", BigDecimal.valueOf(5));
    public static final StockDTO MICROSOFT_STOCK = new StockDTO("Microsoft", "MSFT", BigDecimal.valueOf(10));
    public static final StockDTO ORACLE_STOCK = new StockDTO("Oracle", "ORCL", BigDecimal.valueOf(15));
}
