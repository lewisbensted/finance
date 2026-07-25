import { currentHoldingsMap } from "../holding.js";
import { setBalance } from "../balance/balance.js";
import { executeTransaction } from "./executeTransaction.js";
export const handleBuy = async (transactionRequests) => {
    const { successful, updatedBalance, failed } = await executeTransaction(transactionRequests, "buy");
    if (updatedBalance !== undefined)
        setBalance(updatedBalance);
    for (const [symbol, transaction] of successful) {
        const quantity = transaction.quantity;
        const holding = currentHoldingsMap.get('h');
        if (!holding)
            throw new Error(`Holding for ${symbol} not found.`);
        ;
        if (quantity) {
            holding.holding.shares += quantity;
            holding.holding.value = holding.holding.latestPrice * holding.holding.shares;
        }
    }
    return { successful, failed };
};
