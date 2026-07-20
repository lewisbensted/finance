import { fetchBalance } from "./balance.js";
import { updatePrices } from "./price.js";
import { handleShares } from "./shares.js";
import { CustomError } from "./types/CustomError.js";
import { createHolding, HoldingItem } from "./types/Holding.js";

const message = document.querySelector(".message");

const balanceCell = document.querySelector(".balance");
const tradingFooter = document.querySelector(".table-footer");

const quoteSpinner = document.querySelector(".quote-spinner");
const quoteButton = document.querySelector(".quote-button");
const quoteTable = document.querySelector("table");

const buyTotal = document.querySelector(".buy-total");
const sellTotal = document.querySelector(".sell-total");

let priceIntervalId: number | undefined = undefined;
const transactionInProgress = false;
let balance: number | null = null;
let holding: HoldingItem | null = null;
const INTERVAL = 3000;

try {
	balance = await fetchBalance();
	if (balance === null) {
		tradingFooter.style.display = "none";
	} else {
		balanceCell.style.color = "";
		balanceCell.textContent = `$${balance.toFixed(2)}`;
	}
} catch (error) {
	console.error(error);
	balanceCell.style.color = "red";
	balanceCell.textContent = "$--";
}

const setQuoteLoading = (loading: boolean) => {
	quoteButton.style.display = loading ? "none" : "";
	quoteSpinner.style.setProperty("display", loading ? "flex" : "none", "important");
	if (loading) {
		quoteTable.style.display = "none";
		message.textContent = "";
	}
};

const applyDomUpdates = (domUpdates: (() => void)[]) => {
	const updates = domUpdates.splice(0);
	requestAnimationFrame(() => {
		for (const update of updates) {
			update();
		}
	});
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

	holding = createHolding(shareSymbol);

	setQuoteLoading(true);

	const domUpdates: (() => void)[] = [];

	try {
		const holdingMap = new Map<string, HoldingItem>();
		holdingMap.set(shareSymbol, holding);

		await Promise.all([
			handleShares(holding, domUpdates),
			updatePrices(holdingMap, domUpdates, balance, transactionInProgress, true, true),
		]);

		applyDomUpdates(domUpdates);

		quoteTable.style.display = "";

		//add abortcontroller
		priceIntervalId = setInterval(() => {
			updatePrices(holdingMap, domUpdates, balance, transactionInProgress, true)
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
