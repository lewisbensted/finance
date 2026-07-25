import { Holding, HoldingItem } from "../types/Holding";
import { Stock } from "../types/Stock";

export const currentHoldingsMap = new Map<string, HoldingItem>();

export const resetHoldings = () => {
	currentHoldingsMap.clear();
};

export const createHolding = (symbol: string): HoldingItem => ({
	holding: {
		symbol: symbol,
		companyName: undefined,
		shares: undefined,
		latestPrice: undefined,
		isPriceUpToDate: undefined,
		value: undefined,
	},
	row: {
		nameCell: document.querySelector(".name"),
		symbolCell: document.querySelector(".symbol"),
		sharesCell: document.querySelector(".current-shares"),
		priceCell: document.querySelector(".price"),
		valueCell: document.querySelector(".value"),
		buyInput: document.querySelector(".buy-input"),
		sellInput: document.querySelector(".sell-input"),
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