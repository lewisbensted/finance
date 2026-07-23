import { Holding } from "../types/Holding.js";
import { Stock } from "../types/Stock.js";

export const updateHolding = (holding: Holding, stock: Stock) => {
	const { shares } = holding;

	holding.companyName ??= stock.companyName;

	holding.latestPrice = stock.latestPrice;
	holding.isPriceUpToDate = true;

	if (shares === undefined) {
		holding.value = undefined;
	} else if (shares === null) {
		holding.value = null;
	} else {
		holding.value = shares * stock.latestPrice;
	}
};
