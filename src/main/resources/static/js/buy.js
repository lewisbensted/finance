import { setBalance } from "./balance.js";
import { updateUI } from "./price.js";
import { CustomError } from "./types/CustomError.js";
const buyShares = async (buyRequests) => {
    const res = await fetch("/buy", {
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
    const buyRequests = {};
    for (const holding of holdings) {
        const { symbol, latestPrice } = holding.holding;
        const { buyInput } = holding.row;
        const quantity = Number(buyInput.value);
        if (quantity < 0 || !Number.isInteger(quantity) || !buyInput.checkValidity()) {
            return { invalidInput: true, transactionRequests: null, purchaseTotal: null };
        }
        purchaseTotal += quantity * latestPrice;
        if (quantity > 0)
            buyRequests[symbol] = quantity;
    }
    return { invalidInput: false, transactionRequests: buyRequests, purchaseTotal: purchaseTotal };
};
const calculateTotal = (holdings) => holdings.reduce((total, holding) => total + (holding.holding.value ?? 0), 0).toFixed(2);
export const handleBuy = async (holdingsMap, transactionRequests, domUpdates = []) => {
    const holdings = [...holdingsMap.values()];
    try {
        const { transactions, updatedBalance, error } = await buyShares(transactionRequests);
        if (error?.fields)
            for (const [symbol, itemError] of Object.entries(error.fields)) {
                console.warn(`Failed transaction ${symbol}: ${itemError.message}`);
            }
        setBalance(updatedBalance);
        for (const [symbol, transaction] of [...transactions.entries()]) {
            const quantity = transaction.quantity;
            const holding = holdingsMap.get(symbol);
            if (!holding)
                continue;
            if (quantity) {
                holding.holding.shares += quantity;
                holding.holding.value = holding.holding.latestPrice * holding.holding.shares;
            }
        }
        updateUI(holdings, domUpdates, true);
        // modal
    }
    catch (error) {
        console.error(error);
    }
};
