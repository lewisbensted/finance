const nameCell = document.querySelector(".name");
const buyTotal = document.querySelector(".buy-total");
const sellTotal = document.querySelector(".sell-total");
const priceCell = document.querySelector(".price");
const valueCell = document.querySelector(".value");

const buyForm = document.querySelector("#buy-form");
const sellForm = document.querySelector("#sell-form");

const buyInput = document.querySelector(".buy-input");
const buyButton = document.querySelector(".buy-button");

export const fetchPrice = async (symbol) => {
	const res = await fetch(`/api/prices?symbolsStr=${encodeURIComponent(symbol)}`);
	const data = await res.json().catch(() => null)
	if (!res.ok) {
		const error = new Error(data.message || `Error ${res.status}: ${res.statusText}`);
		error.status = res.status;
		throw error;
	}
	const stock = data?.stocks[0]
	if (!stock || stock.symbol !== symbol || !stock.companyName)
		throw new Error("Missing or invalid data");
	const price = stock.latestPrice;
	if (typeof price !== "number" || Number.isNaN(price) || price <= 0) {
		throw new Error(`Invalid price received for holding: ${symbol}`);
	}
	return stock;
};

export const updatePrice = async (symbol, balance, transactionInProgress, isFirstLoad = false) => {
	try {
		const { latestPrice, companyName } = await fetchPrice(symbol);

		if (isFirstLoad) {
			nameCell.textContent = companyName;
			buyTotal.textContent = "";
			sellTotal.textContent = "";
		}
		priceCell.style.color = "";
		priceCell.dataset.value = latestPrice;
		priceCell.textContent = `$${latestPrice.toFixed(2)}`;
		sellForm.price.value = buyForm.price.value = latestPrice;
		if (balance !== null) {
			const availableShares = Math.floor(balance / latestPrice);
			buyInput.max = availableShares;

			const available = availableShares > 0;
			if (!transactionInProgress) buyInput.disabled = buyButton.disabled = available ? false : true;
			buyInput.min = available ? 1 : 0;
		}

		return latestPrice;
	} catch (error) {
		console.error(error);
		if (isFirstLoad) throw error;
		priceCell.style.color = valueCell.style.color = "red";
		return null;
	}
};