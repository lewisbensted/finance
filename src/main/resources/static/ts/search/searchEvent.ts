import { handleFetchBalance } from "../balance/balance.js";
import { createHolding, currentHoldingsMap, resetHoldings } from "../holding/holdingOperations.js";
import { handleFetchPrices } from "../prices/handleFetchPrices.js";
import { handleFetchShares } from "../shares/handleFetchShares.js";
import { updateSharesUI } from "../shares/updateSharesUI.js";

import { applyDomUpdates, domUpdates } from "../utils/domUpdates.js";
import {  startPollPrices, stopPollPrices } from "../utils/pollPrices.js";

import { sumInputs } from "../utils/sumInputs.js";
import { updateTableUI } from "../utils/updateTableUI.js";

const message = document.querySelector(".message")!;

const quoteSpinner = document.querySelector<HTMLElement>(".quote-spinner")!;
const quoteButton = document.querySelector<HTMLInputElement>(".quote-button")!;
const quoteTable = document.querySelector<HTMLElement>("table")!;

void handleFetchBalance();

const setQuoteLoading = (loading: boolean) => {
	quoteButton.style.display = loading ? "none" : "";
	quoteButton.disabled = false;
	quoteSpinner.style.setProperty("display", loading ? "flex" : "none", "important");
	if (loading) {
		quoteTable.style.display = "none";
		message.textContent = "";
	}
};

const quoteForm = document.getElementById("quote-form");
quoteForm!.addEventListener("submit", async (e) => {
	e.preventDefault();

	const shareSymbol = document.querySelector<HTMLInputElement>(".quote-input")!.value.trim().toUpperCase();
	if (!shareSymbol) {
		message.textContent = "Invalid input - please enter a symbol.";
		return;
	}

	stopPollPrices();

	resetHoldings();
	const row = document.querySelector<HTMLTableRowElement>("tr.holding")!;
	const holding = createHolding(row, shareSymbol);
	currentHoldingsMap.set(shareSymbol, holding);
	const currentHoldings = [...currentHoldingsMap.values()];

	setQuoteLoading(true);

	try {
		const [, { stockMap }] = await Promise.all([
			handleFetchShares(holding),
			handleFetchPrices(true),
		]);

		if (!stockMap.size) {
			message.textContent = "Symbol not found";
			return;
		}

		updateSharesUI(holding);

		const buySum = sumInputs(currentHoldings);
		updateTableUI(currentHoldings, false, buySum);
		applyDomUpdates(domUpdates);

		quoteTable.style.display = "";

		startPollPrices();
	} catch (error) {
		console.error(error);
		message.textContent = "Unexpected error";
	} finally {
		setQuoteLoading(false);
	}
});
