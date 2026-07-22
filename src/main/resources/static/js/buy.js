import { setBalance } from "./balance.js";
import { updateUI } from "./price.js";
import { CustomError } from "./types/CustomError.js";
const buyShares = async (buyRequests) => {
    const res = await fetch("/api/buy", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify(buyRequests),
    });
    const response = (await res.json());
    if (!res.ok) {
        throw new CustomError(response.error?.message ?? `Request failed ${res.status}`, res.status, response.error?.code);
    }
    if (!response.data?.transactions ||
        typeof response.data.transactions !== "object" ||
        typeof response.data.balance !== "number") {
        throw new Error("Invalid response from server");
    }
    return {
        transactions: new Map(Object.entries(response.data.transactions)),
        updatedBalance: response.data.balance,
        error: response.error ?? null,
    };
};
export const collectInputs = (holdings) => {
    let purchaseTotal = 0;
    const buyRequests = [];
    for (const holding of holdings) {
        const { symbol, latestPrice } = holding.holding;
        const { buyInput } = holding.row;
        const quantity = Number(buyInput.value);
        if (quantity < 0 || !Number.isInteger(quantity) || !buyInput.checkValidity()) {
            return { invalidInput: true, transactionRequests: null, purchaseTotal: null };
        }
        purchaseTotal += quantity * latestPrice;
        if (quantity > 0)
            buyRequests.push({ symbol: symbol, quantity: quantity });
    }
    return { invalidInput: false, transactionRequests: buyRequests, purchaseTotal: purchaseTotal };
};
export const handleBuy = async (holdingsMap, transactionRequests, domUpdates = []) => {
    const holdings = [...holdingsMap.values()];
    try {
        const { transactions, updatedBalance, error } = await buyShares(transactionRequests);
        const errorMessages = [];
        if (error?.fields)
            for (const [symbol, itemError] of Object.entries(error.fields)) {
                console.warn(`Failed transaction ${symbol}: ${itemError.message}`);
                errorMessages.push(`${symbol}: ${itemError.message}`);
            }
        setBalance(updatedBalance);
        const successMessage = [];
        for (const [symbol, transaction] of [...transactions.entries()]) {
            const quantity = transaction.quantity;
            const holding = holdingsMap.get(symbol);
            successMessage.push(symbol);
            if (!holding)
                continue;
            if (quantity) {
                holding.holding.shares += quantity;
                holding.holding.value = holding.holding.latestPrice * holding.holding.shares;
            }
        }
        //showtoast
        updateUI(holdings, domUpdates, true);
    }
    catch (error) {
        console.error(error);
        if (error instanceof CustomError && error.code === "OPERATION_FAILED") {
            const errorMessages = [];
            for (const [symbol, itemError] of Object.entries(error.fields)) {
                console.warn(`Failed transaction ${symbol}: ${itemError.message}`);
                errorMessages.push(`${symbol}: ${itemError.message}`);
            }
            //show toast
        }
        else {
            //toast "Unexpected error - all transactions failed"
        }
    }
};
