import { HoldingItem } from "./../types/Holding.js";
import { fetchShares } from "./fetchShares.js";
import { updateSharesUI } from "./updateSharesUI.js";

export const handleFetchShares = async (holding: HoldingItem, domUpdates: (() => void)[] = []) => {
	try {
		const symbol = holding.holding.symbol;
		const shares = await fetchShares(symbol);
		holding.holding.shares = shares;
		updateSharesUI(holding, domUpdates);
	} catch (error) {
		console.error(error);
	}
};
