import { balance, fetchBalance, handleFetchBalance } from "../balance.js";
import { createHolding, currentHoldingsMap } from "../holding.js";
import { handleFetchPrices } from "../prices/handleFetchPrices.js";

import { handleFetchShares } from "../shares/handleFetchShares.js";

import { transactionInProgress } from "../transactionInProgress.js";
import { CustomError } from "../types/CustomError.js";
import { applyDomUpdates, domUpdates } from "../utils/domUpdates.js";

const message = document.querySelector(".message");

const balanceCell = document.querySelector(".balance");
const tradingFooter = document.querySelector(".table-footer");

const quoteSpinner = document.querySelector(".quote-spinner");
const quoteButton = document.querySelector(".quote-button");
const quoteTable = document.querySelector("table");

const buyTotal = document.querySelector(".buy-total");
const sellTotal = document.querySelector(".sell-total");

let priceIntervalId: number | undefined = undefined;
const INTERVAL = 3000;

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
quoteForm.addEventListener("submit", async (e) => {
	e.preventDefault();

	const shareSymbol = document.querySelector(".quote-input").value.trim().toUpperCase();
	if (!shareSymbol) {
		message.textContent = "Invalid input - please enter a symbol.";
		return;
	}

	if (priceIntervalId) {
		clearInterval(priceIntervalId);
		priceIntervalId = undefined;
	}

	const holding = createHolding(shareSymbol);
	currentHoldingsMap.set(shareSymbol, holding);

	setQuoteLoading(true);

	try {
		await Promise.all([
			handleFetchShares(holding, domUpdates),
			handleFetchPrices(domUpdates, true, true),
		]);

		applyDomUpdates(domUpdates);

		quoteTable.style.display = "";

		//add abortcontroller
		priceIntervalId = setInterval(() => {
			handleFetchPrices(domUpdates, true)
				.then(() => {
					if (!transactionInProgress) {
						if (buyTotal?.textContent)
							holding.row.buyInput.dispatchEvent(new Event("input"));
						if (sellTotal?.textContent)
							holding.row.sellInput.dispatchEvent(new Event("input"));
					}
					applyDomUpdates(domUpdates);
				})
				.catch((error: unknown) => {
					console.error(error);
				});
		}, INTERVAL);
	} catch (error) {
		console.error(error);
		message.textContent =
			error instanceof CustomError && error.code === "OPERATION_FAILED"
				? "Symbol not found"
				: "Unexpected error";
	} finally {
		setQuoteLoading(false);
	}
});
