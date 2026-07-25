import { Stock } from "../types/Stock.js";

import { fetchPrices } from "./fetchPrices.js";
import { updateTableUI } from "../utils/updateTableUI.js";

import { sumInputs } from "../utils/sumInputs.js";
import { currentHoldingsMap, updateHolding } from "../holding/holdingOperations.js";
import { Page } from "../types/Transaction.js";

const checkValidStock = (expectedSymbol: string, stock: unknown): stock is Stock => {
	if (
		typeof stock !== "object" ||
		stock === null ||
		!("symbol" in stock) ||
		!("companyName" in stock) ||
		!("latestPrice" in stock)
	) {
		console.warn(`Missing or invalid data for stock: ${expectedSymbol}`);
		return false;
	}

	if (stock.symbol !== expectedSymbol || !stock.companyName) {
		console.warn(`Missing or invalid data for stock: ${expectedSymbol}`);
		return false;
	}
	const price = stock.latestPrice;
	if (typeof price !== "number" || Number.isNaN(price) || price <= 0) {
		console.warn(`Invalid price received for stock: ${expectedSymbol}`);
		return false;
	}
	return true;
};

export const handleFetchPrices = async (page: Page, isFirstLoad = false) => {
	const symbols = [...currentHoldingsMap.keys()];
	const holdings = [...currentHoldingsMap.values()];
	try {
		const res = await fetchPrices(symbols);
		const stockMap = res.successful;
		const failed = res.failed;

		for (const [symbol, itemError] of Object.entries(failed))
			console.warn(`Failed to fetch price ${symbol}: ${itemError.message}`);

		if (page === "search") {
			const stock = stockMap.get(symbols[0]);
			if (stockMap.size !== 1 || !checkValidStock(symbols[0], stock)) {
				throw new Error(`Invalid stock response for ${symbols[0]}`);
			}
		}

		for (const holding of holdings) {
			const symbol = holding.holding.symbol;
			const stock = stockMap.get(symbol);
			if (checkValidStock(symbol, stock)) {
				updateHolding(holding.holding, stock);
			}
		}

		const buySum = sumInputs(holdings);

		updateTableUI(holdings, false, buySum);

		return { stockMap, failed };
	} catch (error) {
		console.error(error);
		if (isFirstLoad && page === "search") throw error;
		for (const holding of holdings) {
			holding.holding.isPriceUpToDate = false;
		}
	}
};
