import { currentHoldingsMap } from "../holding/holdingOperations.js";
import { setBalanceState } from "../balance/balance.js";
import { executeTransaction } from "./executeTransaction.js";
export const handleTransaction = async (transactionRequests, type) => {
    const { successful, updatedBalance, failed } = await executeTransaction(transactionRequests, type);
    setBalanceState({ balance: updatedBalance, isStale: false });
    for (const [symbol, quantity] of successful) {
        const holding = currentHoldingsMap.get(symbol);
        if (!holding)
            throw new Error(`Holding for ${symbol} not found.`);
        console.log(holding.holding.shares);
        console.log(holding.holding.latestPrice);
        if (quantity &&
            holding.holding.shares != undefined &&
            holding.holding.latestPrice != undefined) {
            const delta = type === "BUY" ? quantity : -quantity;
            holding.holding.shares = holding.holding.shares + delta;
            holding.holding.value = holding.holding.latestPrice * holding.holding.shares;
        }
    }
    return { successful, failed };
};
