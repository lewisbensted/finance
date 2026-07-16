
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

const calculateTotal = (holdings) => {
	let total = 0;
	holdings.forEach((holding) => {
		total += Number(holding.valueCell.dataset.value);
	});
	return Number(total.toFixed(2));
};

export const updatePrice = async (holding, balance, transactionInProgress, isFirstLoad = false) => {
	try {
		const symbol = holding.symbol;
		const res = await fetchPrices([symbol]);
		const stocks = res.stocks;
		if (stocks.length !== 1 || !isValidStock(symbol, stocks[0]))
			throw new Error(`Invalid stock response for ${symbol}`);

		const { latestPrice, companyName } = stocks[0];
		holding.latestPrice = latestPrice
		holding.companyName = companyName

		if (isFirstLoad) {
			holding.nameCell.textContent = companyName;
			buyTotal.textContent = "";
			sellTotal.textContent = "";
		}
		holding.priceCell.style.color = "";
		holding.priceCell.textContent = `$${latestPrice.toFixed(2)}`;
		sellForm.price.value = buyForm.price.value = latestPrice;
		if (balance === null) return

		const availableShares = Math.floor(balance / latestPrice);
		holding.buyInput.max = availableShares;

		const available = availableShares > 0;
		if (!transactionInProgress) holding.buyInput.disabled = buyButton.disabled = available ? false : true;
		holding.buyInput.min = available ? 1 : 0;

	} catch (error) {
		console.error(error);
		if (isFirstLoad) throw error;
		holding.priceCell.style.color = holding.valueCell.style.color = "red";
		return null;
	}
};

export const updatePrices = async (holdingsMap, balance, transactionInProgress, isFirstLoad = false, isSearch = true) => {
	const DOMupdates = [];
	try {
		let updated = 0;
		let available = 0;
		const res = await fetchPrices(Object.keys(holdingsMap));
		const stocks = res.stocks;
		if (isSearch && (stocks.length !== 1 || !isValidStock(Object.keys(holdingsMap)[0], stocks[0])))
			throw new Error(`Invalid stock response for ${Object.keys(holdingsMap)[0]}`);

		const stockMap = {}
		for (const stock of stocks) stockMap[stock.symbol] = stock

		const holdings = Object.values(holdingsMap)


		for (const holding of holdings) {
			const symbol = holding.symbol
			const stock = stockMap[symbol];
			const { shares, sharesCell, priceCell, valueCell, buyInput, sellInput } = holding;
			if (isValidStock(symbol, stock)) {
				holding.latestPrice = stock.latestPrice;
				holding.value = shares * holding.latestPrice;
				if (isSearch) holding.companyName = stock.companyName
			}
		}

		let buyTotal = 0;
		for (const holding of holdings) {
			const { priceCell, buyInput } = holding;
			buyTotal += Number(holding.buyInput.value || 0) * Number(holding.latestPrice || 0);
		}


		for (const holding of holdings) {
			const symbol = holding.symbol
			const stock = stockMap[symbol];
			const { shares, sharesCell, priceCell, valueCell, buyInput, sellInput } = holding;
			if (!isValidStock(symbol, stock)) {
				DOMupdates.push(() => {
					if (isFirstLoad) {
						priceCell.textContent = `$--`;
						valueCell.textContent = `$--`;
					}
					priceCell.style.color = "red";
					valueCell.style.color = "red";
					sellInput.disabled = true;
					buyInput.disabled = true;
				});
			} else {
				const { latestPrice, value } = holding

				DOMupdates.push(() => {
					if (isFirstLoad) {
						buyTotal.textContent = "";
						sellTotal.textContent = "";
					}

					priceCell.style.color = "";
					priceCell.textContent = `$${latestPrice.toFixed(2)}`;

					valueCell.dataset.value = value;
					valueCell.style.color = "";
					valueCell.textContent = `$${value.toFixed(2)}`;
				})
			}
		}
		if (balance === null) return

		for (const holding of holdings) {
			const { symbol, latestPrice } = holding;
			const stock = stockMap[symbol];
			const { shares, sharesCell, priceCell, valueCell, buyInput, sellInput } = holding;
			if (isValidStock(symbol, stock)) {
				const remaining = balance - buyTotal + latestPrice * (Number(buyInput.value) || 0);
				const availableShares = Math.floor(remaining / latestPrice);
				const availableSharesMax = Math.floor(balance / latestPrice);
				if (availableSharesMax > 0) available++;
				DOMupdates.push(() => {

					if (isFirstLoad) sellInput.disabled = false;

					buyInput.max = availableShares;
					if (!transactionInProgress) buyInput.disabled = availableSharesMax > 0 ? false : true;
					buyInput.min = availableShares > 0 ? 1 : 0;
				});
				updated++;
			}
		}



		const total = calculateTotal(holdings);
		DOMupdates.push(() => {
			totalCell.style.color = updated === holdings.length ? "" : "red";
			totalCell.textContent = isNaN(total) ? `$--` : `$${total.toFixed(2)}`;
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
			totalCell.style.color = "red";
			buyButton.disabled = sellButton.disabled = true;
			for (const mappedHolding of allHoldings) {
				const { sharesCell, priceCell, valueCell, buyInput, sellInput } = mappedHolding;
				priceCell.style.color = valueCell.style.color = "red";
				buyInput.disabled = true;
				sellInput.disabled = true;
				if (isFirstFetch) {
					priceCell.textContent = valueCell.textContent = totalCell.textContent = `$--`;
				}
			}
		});
		return null
	} finally {
		requestAnimationFrame(() => {
			for (const update of DOMupdates) {
				update();
			}
		});
	}
}