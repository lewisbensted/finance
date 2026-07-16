
const buyTotal = document.querySelector(".buy-total");
const sellTotal = document.querySelector(".sell-total");


const buyForm = document.querySelector("#buy-form");
const sellForm = document.querySelector("#sell-form");

const buyButton = document.querySelector(".buy-button");

const fetchPrices = async (symbols) => {
	const res = await fetch(`/api/prices?symbolsStr=${encodeURIComponent(symbols.join(","))}`);
	const data = await res.json().catch(() => null)
	if (!res.ok) {
		const error = new Error(data?.message || `Error ${res.status}: ${res.statusText}`);
		error.status = res.status;
		throw error;
	}
	if (!Array.isArray(data?.stocks)) {
		throw new Error("Invalid response from server.");
	}
	return data
};

const isValidStock = (expectedSymbol, stock) => {
	if (!stock || stock.symbol !== expectedSymbol || !stock.companyName) {
		console.warn((`Missing or invalid data for stock: ${expectedSymbol}`))
		return false
	}
	const price = stock.latestPrice;
	if (typeof price !== "number" || Number.isNaN(price) || price <= 0) {
		console.warn(`Invalid price received for stock: ${expectedSymbol}`);
		return false
	}
	return true
}

export const updatePrice = async (holding, balance, transactionInProgress, isFirstLoad = false) => {
	try {
		const symbol = holding.symbol;
		const response = await fetchPrices([symbol]);
		const stocks = response.stocks;
		if (stocks.length !== 1 || !isValidStock(symbol, stocks[0]))
			throw new Error(`Invalid stock response for ${symbol}`);

		const { latestPrice, companyName } = stocks[0];

		if (isFirstLoad) {
			holding.nameCell.textContent = companyName;
			buyTotal.textContent = "";
			sellTotal.textContent = "";
		}
		holding.priceCell.style.color = "";
		holding.priceCell.dataset.value = latestPrice;
		holding.priceCell.textContent = `$${latestPrice.toFixed(2)}`;
		sellForm.price.value = buyForm.price.value = latestPrice;
		if (balance !== null) {
			const availableShares = Math.floor(balance / latestPrice);
			holding.buyInput.max = availableShares;

			const available = availableShares > 0;
			if (!transactionInProgress) holding.buyInput.disabled = buyButton.disabled = available ? false : true;
			holding.buyInput.min = available ? 1 : 0;
		}
		return latestPrice;
	} catch (error) {
		console.error(error);
		if (isFirstLoad) throw error;
		holding.priceCell.style.color = holding.valueCell.style.color = "red";
		return null;
	}
};