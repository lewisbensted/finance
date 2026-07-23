import { balance } from "./../balance.js";
import { HoldingItem } from "./../types/Holding.js";
import { updateRowUI } from "./updateRowUI.js";

const totalCell = document.querySelector(".total");
const buyButton = document.querySelector(".buy-button");
const sellButton = document.querySelector(".sell-button");
const balanceCell = document.querySelector(".balance");
const buySpinner = document.querySelector(".buy-spinner");

export const updateTableUI = (
	holdings: HoldingItem[],
	domUpdates: (() => void)[],
	resetInputs = false,
	buySum = 0,
) => {
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

		domUpdates.push(updateRowUI(holding, buySum));

		const { shares, latestPrice, value, isPriceUpToDate } = holding.holding;

		totalValue += value ?? 0;

		if (value === undefined || !isPriceUpToDate) {
			totalValueUpToDate = false;
		}

		if (typeof shares === "number" && shares > 0) sellButtonDisabled = false;

		if (balance !== null && typeof latestPrice === "number" && balance >= latestPrice)
			buyButtonDisabled = false;
	}

	domUpdates.push(() => {
		if (totalCell) totalCell.style.color = totalValueUpToDate ? "" : "red";
		if (totalCell) totalCell.textContent = totalValue.toFixed(2);

		buyButton.disabled = buyButtonDisabled;
		sellButton.disabled = sellButtonDisabled;

		if (balance != null) {
			balanceCell.style.color = "";
			balanceCell.textContent = `$${balance.toFixed(2)}`;
		}
	});
};
