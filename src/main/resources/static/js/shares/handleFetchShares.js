import { fetchShares } from "./fetchShares.js";
export const handleFetchShares = async (holding) => {
    try {
        const symbol = holding.holding.symbol;
        const shares = await fetchShares(symbol);
        holding.holding.shares = shares;
    }
    catch (error) {
        console.error(error);
    }
};
