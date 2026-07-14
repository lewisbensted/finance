package finance.fixtures;

import finance.dtos.HoldingDTO;
import finance.entities.Holding;
import finance.entities.User;

public class HoldingFixtures {
    public static HoldingDTO appleHoldingDTO(Long quantity) {
        return new HoldingDTO("AAPL", "Apple", quantity);
    }

    public static HoldingDTO microsoftHoldingDTO(Long quantity) {
        return new HoldingDTO("MSFT", "Microsoft", quantity);
    }

    public static HoldingDTO oracleHoldingDTO(Long quantity) {
        return new HoldingDTO("ORCL", "Oracle", quantity);
    }

    public static Holding appleHolding(User user, Long quantity) {
        return new Holding(user, "AAPL", "Apple", quantity);
    }

    public static Holding microsoftHolding(User user, Long quantity) {
        return new Holding(user, "MSFT", "Microsoft", quantity);
    }
}
