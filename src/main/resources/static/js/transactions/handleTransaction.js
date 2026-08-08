import { currentHoldingsMap } from "../holding/holdingOperations.js";
import { setBalance } from "../balance/balance.js";
import { executeTransaction } from "./executeTransaction.js";
export const handleTransaction = async (transactionRequests, type) => {
    const { successful, updatedBalance, failed } = await executeTransaction(transactionRequests, "BUY");
    if (updatedBalance !== undefined)
        setBalance(updatedBalance);
    for (const [symbol, quantity] of successful) {
        const holding = currentHoldingsMap.get(symbol);
        if (!holding)
            throw new Error(`Holding for ${symbol} not found.`);
        if (quantity) {
            const delta = type === "BUY" ? quantity : -quantity;
            holding.holding.shares = holding.holding.shares + delta;
            holding.holding.value = holding.holding.latestPrice * holding.holding.shares;
        }
    }
    return { successful, failed };
};
