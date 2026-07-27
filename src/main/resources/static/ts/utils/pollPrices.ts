import { currentHoldingsMap } from "../holding/holdingOperations.js";
import { handleFetchPrices } from "../prices/handleFetchPrices.js";
import { applyDomUpdates, domUpdates } from "./domUpdates.js";
import { sumInputs } from "./sumInputs.js";
import { updateTableUI } from "./updateTableUI.js";

let priceIntervalId: number | undefined;
const INTERVAL = 3000;

export function stopPricePolling() {
	if (priceIntervalId !== undefined) {
		clearInterval(priceIntervalId);
		priceIntervalId = undefined;
	}
}

export function startPricePolling() {
	if (priceIntervalId !== undefined) {
		return;
	}

	priceIntervalId = window.setInterval(async () => {
		try {
			await handleFetchPrices();

			const currentHoldings = [...currentHoldingsMap.values()];

			const buySum = sumInputs(currentHoldings);
			updateTableUI(currentHoldings, false, buySum);
			applyDomUpdates(domUpdates);
		} catch (error) {
			console.error(error);
		}
	}, INTERVAL);
}
