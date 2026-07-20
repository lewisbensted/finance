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

export const updateHoldingUI = (
	holding: HoldingItem,
	balance: number | null,
	buyInputsSum?: number,
) => {
	const { priceCell, valueCell, buyInput, sellInput, nameCell, sharesCell, symbolCell } =
		holding.row;
	const { isPriceUpToDate, latestPrice, value, companyName, shares, symbol } = holding.holding;

	return () => {
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
		} else {
			if (!isPriceUpToDate) {
				priceCell.style.color = "red";
			} else {
				priceCell.textContent = String(latestPrice);
				priceCell.style.color = "";
			}
		}

		const isSellDisabled =
			balance === null || latestPrice === undefined || !isPriceUpToDate || !shares;

		if (isSellDisabled) {
			sellInput.max = "0";
			sellInput.min = "1";
			sellInput.disabled = true;
		} else {
			sellInput.disabled = false;
			sellInput.min = "1";
			sellInput.max = String(shares);
		}

		const isBuyDisabled =
			balance === null ||
			latestPrice === undefined ||
			!isPriceUpToDate ||
			buyInputsSum === undefined;

		if (isBuyDisabled) {
			buyInput.max = "0";
			buyInput.min = "1";
			buyInput.disabled = true;
		} else {
			const remaining = balance - buyInputsSum + latestPrice * (Number(buyInput.value) || 0);
			const availableShares = Math.floor(remaining / latestPrice);
			const availableSharesMax = Math.floor(balance / latestPrice);
			buyInput.max = String(availableShares);
			buyInput.disabled = availableSharesMax > 0 ? false : true;
			buyInput.min = String(availableShares > 0 ? 1 : 0);
		}
	};
};
