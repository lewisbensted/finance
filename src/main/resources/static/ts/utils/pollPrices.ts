import { currentHoldingsMap } from "../holding/holdingOperations.js";
import { handleFetchPrices } from "../prices/handleFetchPrices.js";
import { applyDomUpdates, domUpdates } from "./domUpdates.js";
import { sumInputs } from "./sumInputs.js";
import { updateTableUI } from "./updateTableUI.js";

let priceIntervalId: number | undefined;
let isPolling = false;

const INTERVAL = 3000;

export const stopPollPrices = () => {
	isPolling = false;
	if (priceIntervalId !== undefined) {
		clearTimeout(priceIntervalId);
		priceIntervalId = undefined;
	}
};

export const startPollPrices = () => {
	isPolling = true;
	void pollPrices();
};

export const pollPrices = async () => {
	if (!isPolling) return;
	try {
		await handleFetchPrices();

		const currentHoldings = [...currentHoldingsMap.values()];
		const buySum = sumInputs(currentHoldings);

		updateTableUI(currentHoldings, false, buySum);
		applyDomUpdates(domUpdates);
	} catch (error) {
		console.error(error);
	}

	priceIntervalId = window.setTimeout(pollPrices, INTERVAL);
};
