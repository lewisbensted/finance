import { HoldingItem } from "./../types/Holding.js";
import { fetchShares } from "./fetchShares.js";

export const handleFetchShares = async (holding: HoldingItem) => {
	try {
		const symbol = holding.holding.symbol;
		const shares = await fetchShares(symbol);
		holding.holding.shares = shares;
	} catch (error) {
		console.error(error);
	}
};
