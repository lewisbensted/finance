import { renderHolding, updateHolding } from "./holding.js";
import { CustomError } from "./types/CustomError.js";
const sellTotal = document.querySelector(".sell-total");
const buyTotal = document.querySelector(".buy-total");
const totalCell = document.querySelector(".total");
const buyButton = document.querySelector(".buy-button");
const sellButton = document.querySelector(".sell-button");
const fetchPrices = async (symbols) => {
    const res = await fetch(`/api/prices?symbolsStr=${encodeURIComponent(symbols.join(","))}`);
    const response = (await res.json());
    if (!res.ok)
        throw new CustomError(response.error?.message ?? `Request failed ${res.status}`, res.status, response.error?.code);
    if (!response.data?.stocks || typeof response.data.stocks !== "object") {
        throw new Error("Invalid response from server");
    }
    return {
        stocks: new Map(Object.entries(response.data.stocks)),
        error: response.error ?? null,
    };
};
const isValidStock = (expectedSymbol, stock) => {
    if (typeof stock !== "object" ||
        stock === null ||
        !("symbol" in stock) ||
        !("companyName" in stock) ||
        !("latestPrice" in stock)) {
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
const calculateTotal = (holdings) => {
    let total = 0;
    holdings.forEach((holding) => {
        total += Number(holding.valueCell?.dataset.value);
    });
    return Number(total.toFixed(2));
};
export const updatePrices = async (holdingsMap, domUpdates, balance, transactionInProgress, isSearch, isFirstLoad = false) => {
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
            const symbol = holding.holding.symbol;
            const stock = stockMap.get(symbol);
            if (isValidStock(symbol, stock)) {
                updateHolding(holding.holding, stock);
            }
        }
        let buySum = 0;
        for (const holding of holdings) {
            buySum +=
                Number(holding.row.buyInput.value || 0) * Number(holding.holding.latestPrice ?? 0);
        }
        for (const holding of holdings) {
            domUpdates.push(renderHolding(holding));
        }
        if (balance === null)
            return;
        // for (const holding of holdings) {
        // 	const { symbol, latestPrice } = holding;
        // 	const stock = stockMap.get(symbol);
        // 	const { buyInput, sellInput } = holding;
        // 	if (isValidStock(symbol, stock)) {
        // 		const remaining = balance - buySum + latestPrice * (Number(buyInput.value) || 0);
        // 		const availableShares = Math.floor(remaining / latestPrice);
        // 		const availableSharesMax = Math.floor(balance / latestPrice);
        // 		if (availableSharesMax > 0) available++;
        // 		domUpdates.push(() => {
        // 			if (isFirstLoad) sellInput.disabled = false;
        // 			buyInput.max = availableShares;
        // 			if (!transactionInProgress)
        // 				buyInput.disabled = availableSharesMax > 0 ? false : true;
        // 			buyInput.min = availableShares > 0 ? 1 : 0;
        // 		});
        // 		updated++;
        // 	}
        // }
        // const total = calculateTotal(holdings);
        // domUpdates.push(() => {
        // 	if (totalCell) totalCell.style.color = updated === holdings.length ? "" : "red";
        // 	if (totalCell) totalCell.textContent = isNaN(total) ? "$--" : `$${total.toFixed(2)}`;
        // 	if (isFirstLoad) {
        // 		sellButton.disabled = false;
        // 		if (balance && available > 0) buyButton.disabled = false;
        // 	} else {
        // 		if (!transactionInProgress) buyButton.disabled = available === 0;
        // 	}
        // });
    }
    catch (error) {
        console.error(error);
        if (isFirstLoad && isSearch)
            throw error;
        for (const holding of holdings) {
            holding.holding.isPriceUpToDate = false;
            domUpdates.push(renderHolding(holding));
            domUpdates.push(() => {
                if (totalCell)
                    totalCell.style.color = "red";
                buyButton.disabled = sellButton.disabled = true;
            });
        }
    }
};
