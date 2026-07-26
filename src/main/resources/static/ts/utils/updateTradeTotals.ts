import { currentHoldingsMap } from "../holding/holdingOperations.js";

const buyTotalCell = document.querySelector(".buy-total");
const sellTotalCell = document.querySelector(".sell-total");

export const updateTradeTotals = () => {
	let buyTotal = 0;
	let sellTotal = 0;
	let totalsUpToDate = true;

	for (const holding of currentHoldingsMap.values()) {
		const { latestPrice, value, isPriceUpToDate } = holding.holding;

		const buyValue = Number(holding.row.buyInput.value) * latestPrice;
		if (Number.isFinite(buyValue)) {
			buyTotal += buyValue;
		}

		const sellValue = Number(holding.row.sellInput.value) * latestPrice;
		if (Number.isFinite(sellValue)) {
			sellTotal += sellValue;
		}

		if (value === undefined || !isPriceUpToDate) {
			totalsUpToDate = false;
		}
	}

	buyTotalCell.style.color = totalsUpToDate ? "" : "red";
	sellTotalCell.style.color = totalsUpToDate ? "" : "red";

	buyTotalCell.textContent = `$${buyTotal.toFixed(2)}`;
	sellTotalCell.textContent = `$${sellTotal.toFixed(2)}`;
};
