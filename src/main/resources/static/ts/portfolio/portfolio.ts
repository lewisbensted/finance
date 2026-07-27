import { handleFetchBalance } from "../balance/balance.js";
import { createHolding, currentHoldingsMap, resetHoldings } from "../holding/holdingOperations.js";
import { handleFetchPrices } from "../prices/handleFetchPrices.js";
import { applyDomUpdates, domUpdates } from "../utils/domUpdates.js";
import { startPricePolling, stopPricePolling } from "../utils/pollPrices.js";
import { updateTableUI } from "../utils/updateTableUI.js";

resetHoldings();

const rows = document.querySelectorAll<HTMLTableRowElement>("tr.holding");
rows.forEach((row) => {
	const symbol = row.dataset.symbol!;
	const companyName = row.dataset.companyName;
	const shares = Number(row.dataset.shares);

	currentHoldingsMap.set(symbol, createHolding(row, symbol, companyName, shares));
});

void handleFetchBalance();

await handleFetchPrices();

updateTableUI([...currentHoldingsMap.values()], false);
applyDomUpdates(domUpdates);

stopPricePolling();

startPricePolling();
