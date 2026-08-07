import { balance } from "../balance/balance.js";
import { transactionInProgress } from "../transactionInProgress.js";
import { HoldingItem } from "./../types/Holding.js";
import { domUpdates } from "./domUpdates.js";
import { updateRowUI } from "./updateRowUI.js";

const totalCell = document.querySelector<HTMLElement>(".total");
const buyButton = document.querySelector<HTMLButtonElement>(".buy-button")!;
const sellButton = document.querySelector<HTMLButtonElement>(".sell-button")!;
const balanceCell = document.querySelector<HTMLElement>(".balance")!;
const buySpinner = document.querySelector<HTMLElement>(".buy-spinner")!;
const buyTotalCell = document.querySelector<HTMLElement>(".buy-total")!;
const sellTotalCell = document.querySelector<HTMLElement>(".sell-total")!;

export const updateTableUI = (holdings: HoldingItem[], resetInputs = false, buySum = 0) => {
	let buyButtonDisabled = true;
	let sellButtonDisabled = true;

	let totalValue = 0;
	let sellTotal = 0;
	let buyTotal = 0;

	let totalsUpToDate = true;

	for (const holding of holdings) {
		const { buyInput, sellInput } = holding.row;

		if (resetInputs) {
			buyInput.value = "";
			sellInput.value = "";
			buySpinner.style.setProperty("display", "none", "important");
			buyButton.style.display = "";
		}

		const { isSellDisabled, isBuyDisabled, domUpdate } = updateRowUI(holding, buySum);

		domUpdates.push(domUpdate);

		const { value, isPriceUpToDate, latestPrice } = holding.holding;

		totalValue += value ?? 0;

		if (value === undefined || !isPriceUpToDate) {
			totalsUpToDate = false;
		}

		if (!isSellDisabled) sellButtonDisabled = false;
		if (!isBuyDisabled) buyButtonDisabled = false;

		if (latestPrice === undefined) continue;

		const buyValue = Number(buyInput.value) * latestPrice;
		if (Number.isFinite(buyValue)) {
			buyTotal += buyValue;
		}

		const sellValue = Number(sellInput.value) * latestPrice;
		if (Number.isFinite(sellValue)) {
			sellTotal += sellValue;
		}
	}

	if (transactionInProgress) return;
	domUpdates.push(() => {
		buyButton.disabled = buyButtonDisabled;
		sellButton.disabled = sellButtonDisabled;

		if (totalCell) totalCell.style.color = totalsUpToDate ? "" : "red";
		if (totalCell && totalsUpToDate) totalCell.textContent = `$${totalValue.toFixed(2)}`;

		buyTotalCell.style.color = totalsUpToDate ? "" : "red";
		buyTotalCell.textContent = `$${buyTotal.toFixed(2)}`;

		sellTotalCell.style.color = totalsUpToDate ? "" : "red";
		sellTotalCell.textContent = `$${sellTotal.toFixed(2)}`;

		if (balance != null) {
			balanceCell.style.color = "";
			balanceCell.textContent = `$${balance.toFixed(2)}`;
		}
	});
};
