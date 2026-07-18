import { ApiResponse } from "./types/ApiResponse";
import { CustomError } from "./types/CustomError";
import { Holding } from "./types/Holding";
import { Stock, StockResponse } from "./types/Stock";

const sellTotal = document.querySelector(".sell-total");
const buyTotal = document.querySelector(".buy-total");
const totalCell = document.querySelector(".total");
const buyButton = document.querySelector(".buy-button");
const sellButton = document.querySelector(".sell-button");

const fetchPrices = async (
	symbols: string[],
) => {
	const res = await fetch(
		`/api/prices?symbolsStr=${encodeURIComponent(symbols.join(","))}`,
	);
	const response = await res.json() as ApiResponse<StockResponse>;
	if (!res.ok) 
		throw new CustomError(response.error?.message ?? `Request failed ${res.status}`, res.status, response.error?.code);
	if (!response.data?.stocks || typeof response.data.stocks !== "object") {
		throw new Error("Invalid response from server");
	}
	return {
		stocks: new Map<string, Stock>(Object.entries(response.data.stocks)),
		error: response.error ?? null,
	};
};

const isValidStock = (
	expectedSymbol: string,
	stock: unknown,
): stock is Stock => {
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

const calculateTotal = (holdings: Holding[]) => {
	let total = 0;
	holdings.forEach((holding) => {
		total += Number(holding.valueCell?.dataset.value);
	});
	return Number(total.toFixed(2));
};

export const updatePrices = async (
	holdingsMap: Map<string, Holding>,
	balance: number | null,
	transactionInProgress: boolean,
	isSearch: boolean,
	isFirstLoad = false,
) => {
	const DOMupdates = [];
	const symbols = [...holdingsMap.keys()];
	const holdings = [...holdingsMap.values()];
	try {
		let updated = 0;
		let available = 0;
		const res = await fetchPrices(symbols);
		const stockMap = res.stocks;
		if (isSearch) {
			const stock = stockMap.get(symbols[0]);
			if (stockMap.size !== 1 || !isValidStock(symbols[0], stock)) {
				throw new Error(`Invalid stock response for ${symbols[0]}`);
			}
		}

		for (const holding of holdings) {
			const symbol = holding.symbol;
			const stock = stockMap.get(symbol);
			if (isValidStock(symbol, stock)) {
				holding.latestPrice = stock.latestPrice;
				holding.value =
					holding.shares === null ? null : holding.shares * holding.latestPrice;
				if (isSearch) {
					holding.companyName = stock.companyName;
					holding.nameCell.textContent = stock.companyName;
				}
			}
		}

		let buySum = 0;
		for (const holding of holdings) {
			buySum +=
				Number(holding.buyInput.value || 0) * Number(holding.latestPrice || 0);
		}

		for (const holding of holdings) {
			const symbol = holding.symbol;
			const stock = stockMap.get(symbol);
			const { priceCell, valueCell, buyInput, sellInput } = holding;

			if (!isValidStock(symbol, stock)) {
				DOMupdates.push(() => {
					if (isFirstLoad) {
						priceCell.textContent = "$--";
						valueCell.textContent = "$--";
					}
					priceCell.style.color = "red";
					valueCell.style.color = "red";
					sellInput.disabled = true;
					buyInput.disabled = true;
				});
			} else {
				const { latestPrice, value } = holding;

				DOMupdates.push(() => {
					if (isFirstLoad) {
						buyTotal.textContent = "";
						sellTotal.textContent = "";
					}

					priceCell.style.color = "";
					priceCell.textContent = `$${latestPrice.toFixed(2)}`;

					if (value) {
						valueCell.dataset.value = value;
						valueCell.style.color = "";
						valueCell.textContent = `$${value.toFixed(2)}`;
					}
				});
			}
		}
		if (balance === null) return;

		for (const holding of holdings) {
			const { symbol, latestPrice } = holding;
			const stock = stockMap.get(symbol);
			const { buyInput, sellInput } = holding;
			if (isValidStock(symbol, stock)) {
				const remaining =
					balance - buySum + latestPrice * (Number(buyInput.value) || 0);
				const availableShares = Math.floor(remaining / latestPrice);
				const availableSharesMax = Math.floor(balance / latestPrice);
				if (availableSharesMax > 0) available++;
				DOMupdates.push(() => {
					if (isFirstLoad) sellInput.disabled = false;

					buyInput.max = availableShares;
					if (!transactionInProgress)
						buyInput.disabled = availableSharesMax > 0 ? false : true;
					buyInput.min = availableShares > 0 ? 1 : 0;
				});
				updated++;
			}
		}

		const total = calculateTotal(holdings);
		DOMupdates.push(() => {
			if (totalCell)
				totalCell.style.color = updated === holdings.length ? "" : "red";
			if (totalCell)
				totalCell.textContent = isNaN(total) ? "$--" : `$${total.toFixed(2)}`;
			if (isFirstLoad) {
				sellButton.disabled = false;
				if (balance && available > 0) buyButton.disabled = false;
			} else {
				if (!transactionInProgress) buyButton.disabled = available === 0;
			}
		});
	} catch (error) {
		console.error(error);
		if (isFirstLoad) throw error;
		DOMupdates.push(() => {
			if (totalCell) totalCell.style.color = "red";
			buyButton.disabled = sellButton.disabled = true;
			for (const holding of holdings) {
				const { priceCell, valueCell, buyInput, sellInput } = holding;
				priceCell.style.color = valueCell.style.color = "red";
				buyInput.disabled = true;
				sellInput.disabled = true;
				if (isFirstLoad) {
					priceCell.textContent =
						valueCell.textContent =
							totalCell.textContent =
								"$--";
				}
			}
		});
		return null;
	} finally {
		requestAnimationFrame(() => {
			for (const update of DOMupdates) {
				update();
			}
		});
	}
};
