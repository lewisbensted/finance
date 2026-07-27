import { Holding, HoldingItem } from "../types/Holding";
import { Stock } from "../types/Stock";

export const currentHoldingsMap = new Map<string, HoldingItem>();

export const resetHoldings = () => {
	currentHoldingsMap.clear();
};

export const createHolding = (
	row: HTMLTableRowElement,
	symbol: string,
	companyName?: string,
	shares?: number,
): HoldingItem => ({
	holding: {
		symbol: symbol,
		companyName: companyName,
		shares: shares,
		latestPrice: undefined,
		isPriceUpToDate: undefined,
		value: undefined,
	},
	row: {
		nameCell: row.querySelector(".name"),
		symbolCell: row.querySelector(".symbol"),
		sharesCell: row.querySelector(".current-shares"),
		priceCell: row.querySelector(".price"),
		valueCell: row.querySelector(".value"),
		buyInput: row.querySelector(".buy-input"),
		sellInput: row.querySelector(".sell-input"),
	},
});

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
