import { currentHoldingsMap } from "../holding.js";
import { displayToast } from "../utils/displayToast.js";
import { updateTableUI } from "../utils/updateTableUI.js";
import { setBalance } from "./../balance.js";
import { CustomError } from "./../types/CustomError.js";
import { buyShares } from "./buyShares.js";
export const handleBuy = async (transactionRequests, domUpdates = []) => {
    const holdings = [...currentHoldingsMap.values()];
    try {
        const { transactions, updatedBalance, error } = await buyShares(transactionRequests);
        setBalance(updatedBalance);
        for (const [symbol, transaction] of [...transactions.entries()]) {
            const quantity = transaction.quantity;
            const holding = currentHoldingsMap.get(symbol);
            if (!holding)
                continue;
            if (quantity) {
                holding.holding.shares += quantity;
                holding.holding.value = holding.holding.latestPrice * holding.holding.shares;
            }
        }
        updateTableUI(holdings, domUpdates, true);
        displayToast(transactions, error?.fields, "buy");
    }
    catch (error) {
        console.error(error);
        if (error instanceof CustomError && error.code === "OPERATION_FAILED" && error.fields) {
            displayToast(new Map(), error.fields, "buy");
        }
        else {
            console.log("all transactions failed");
        }
    }
};
