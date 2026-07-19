import { Holding, HoldingItem } from "./types/Holding";
import { Stock } from "./types/Stock";

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

//buy input needs refactoring - add max buy to holding

export const renderHolding = (holding: HoldingItem, domUpdates: (() => void)[]) => {
	const { isPriceUpToDate, latestPrice, value, companyName, shares, symbol } = holding.holding;
	const { priceCell, valueCell, buyInput, sellInput, nameCell, sharesCell, symbolCell } =
		holding.row;

	domUpdates.push(() => {
		nameCell.textContent = companyName;
		symbolCell.textContent = symbol;

		if (shares === null) {
			sharesCell.textContent = "";
		} else if (shares === undefined) {
			sharesCell.textContent = "$--";
			sharesCell.style.color = "red";
		} else {
			sharesCell.textContent = String(shares);
		}

		if (value === null) {
			valueCell.textContent = "";
		} else if (value === undefined) {
			valueCell.textContent = "$--";
			valueCell.style.color = "red";
		} else {
			if (isPriceUpToDate) {
				valueCell.style.color = "";
				valueCell.textContent = `$${value.toFixed(2)}`;
			} else {
				valueCell.style.color = "red";
			}
		}

		if (latestPrice === undefined) {
			priceCell.textContent = "$--";
			priceCell.style.color = "red";
			buyInput.disabled = true;
		} else {
			if (!isPriceUpToDate) {
				priceCell.style.color = "red";
				buyInput.disabled = true;
			} else {
				priceCell.textContent = String(latestPrice);
				priceCell.style.color = "";
				buyInput.disabled = false;
			}
		}

		const isSellDisabled =
			latestPrice === undefined ||
			!isPriceUpToDate ||
			typeof shares !== "number" ||
			shares <= 0;

		if (isSellDisabled) {
			sellInput.max = "0";
			sellInput.min = "1";
			sellInput.disabled = true;
		} else {
			sellInput.disabled = false;
			sellInput.min = "1";
			sellInput.max = String(shares);
		}
	});
};
