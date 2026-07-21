import { balance } from "./balance.js";
import { updateHoldingUI, updateHolding } from "./holding.js";
import { CustomError } from "./types/CustomError.js";
const sellTotal = document.querySelector(".sell-total");
const buyTotal = document.querySelector(".buy-total");
const totalCell = document.querySelector(".total");
const buyButton = document.querySelector(".buy-button");
const sellButton = document.querySelector(".sell-button");
const balanceCell = document.querySelector(".balance");
const buySpinner = document.querySelector(".buy-spinner");
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
const sumBuyInputs = (holdings) => {
    let buySum = 0;
    for (const holding of holdings) {
        buySum +=
            Number(holding.row.buyInput.value || 0) * Number(holding.holding.latestPrice ?? 0);
    }
    return buySum;
};
export const updateUI = (holdings, domUpdates, resetInputs = false, buySum = 0) => {
    let buyButtonDisabled = true;
    let sellButtonDisabled = true;
    let totalValue = 0;
    let totalValueUpToDate = true;
    for (const holding of holdings) {
        if (resetInputs) {
            holding.row.buyInput.value = "";
            holding.row.sellInput.value = "";
            buySpinner.style.setProperty("display", "none", "important");
            buyButton.style.display = "";
        }
        domUpdates.push(updateHoldingUI(holding, buySum));
        const { shares, latestPrice, value, isPriceUpToDate } = holding.holding;
        totalValue += value ?? 0;
        if (value === undefined || !isPriceUpToDate) {
            totalValueUpToDate = false;
        }
        if (typeof shares === "number" && shares > 0)
            sellButtonDisabled = false;
        if (balance !== null && typeof latestPrice === "number" && balance >= latestPrice)
            buyButtonDisabled = false;
    }
    domUpdates.push(() => {
        if (totalCell)
            totalCell.style.color = totalValueUpToDate ? "" : "red";
        if (totalCell)
            totalCell.textContent = totalValue.toFixed(2);
        buyButton.disabled = buyButtonDisabled;
        sellButton.disabled = sellButtonDisabled;
        if (balance != null) {
            balanceCell.style.color = "";
            balanceCell.textContent = `$${balance.toFixed(2)}`;
        }
    });
};
export const handleFetchPrices = async (holdingsMap, domUpdates, isSearch, isFirstLoad = false) => {
    const symbols = [...holdingsMap.keys()];
    const holdings = [...holdingsMap.values()];
    try {
        const res = await fetchPrices(symbols);
        const stockMap = res.stocks;
        const failures = res.error?.fields;
        if (failures)
            for (const [symbol, itemError] of Object.entries(failures)) {
                console.warn(`Failed to fetch price ${symbol}: ${itemError.message}`);
            }
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
        const buySum = sumBuyInputs(holdings);
        updateUI(holdings, domUpdates, false, buySum);
    }
    catch (error) {
        console.error(error);
        if (isFirstLoad && isSearch)
            throw error;
        for (const holding of holdings) {
            holding.holding.isPriceUpToDate = false;
            domUpdates.push(updateHoldingUI(holding, balance));
            domUpdates.push(() => {
                if (totalCell)
                    totalCell.style.color = "red";
                buyButton.disabled = sellButton.disabled = true;
            });
        }
    }
};
