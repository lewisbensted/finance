import { currentHoldingsMap } from "../holding.js";
import { setBalance } from "./../balance.js";
import { buyShares } from "./buyShares.js";
export const handleBuy = async (transactionRequests) => {
    const { successful, updatedBalance, failed } = await buyShares(transactionRequests);
    if (updatedBalance !== undefined)
        setBalance(updatedBalance);
    for (const [symbol, transaction] of successful) {
        const quantity = transaction.quantity;
        const holding = currentHoldingsMap.get(symbol);
        if (!holding)
            continue;
        if (quantity) {
            holding.holding.shares += quantity;
            holding.holding.value = holding.holding.latestPrice * holding.holding.shares;
        }
    }
    return { successful, failed };
};
