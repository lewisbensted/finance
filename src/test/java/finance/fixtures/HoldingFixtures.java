package finance.fixtures;

import finance.dtos.HoldingDTO;

public class HoldingFixtures {
    public static HoldingDTO appleHolding (Long quantity) {
        return new HoldingDTO("AAPL", "Apple", quantity);
    }

    public static HoldingDTO microsoftHolding (Long quantity) {
        return new HoldingDTO("MSFT", "Microsoft", quantity);
    }

    public static HoldingDTO oracleHolding (Long quantity) {
        return new HoldingDTO("ORCL", "Oracle", quantity);
    }

}
